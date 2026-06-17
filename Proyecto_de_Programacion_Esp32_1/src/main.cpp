/* Proyecto : Ali Uta
La finalidad del proyecto es utilizar el ESP32 para controlar un sistema de riego y ventilación automatizado
En el cual se va a dividir en lo siguiente :
ESP32_#1 : Nodo de Campo
En el modo de campo se van a leer los sensores de temperatura, humedad del aire, humedad del suelo, nivel de luz y nivel del tanque de agua
y se va a controlar la bomba de agua, el ventilador y la lámpara de crecimiento utilizando un PID para la iluminación.
ESP32_#2 (GATEWAY)
En el modo GATEWAY se va a recibir la información del nodo de campo utilizando ESP-NOW y de ahi se conectara por medio de UART a una pc, en el que se va a mostrar en una interfaz diseñada en JAVA (Netbeans), además se va a enviar comandos de control al nodo de campo para cambiar entre modo automático y manual, controlar la bomba, el ventilador y la lámpara.
Autores : Ronal, Caleb , Ronny, Kevin, Aldair 
Curso :Programación Avanzada (7° Ciclo-UNMSM-FIEE)
 */
// ESP32_#1 (Nodo de Campo): Codigo para controlar el sistema de riego y ventilación automatizado, en el que se leen los sensores de temperatura, humedad del aire, humedad del suelo, nivel de luz y nivel del tanque de agua, y se controla la bomba de agua, el ventilador y la lámpara de crecimiento utilizando un PID para la iluminación. Además, se envían los datos al gateway utilizando ESP-NOW y se reciben comandos de control desde el gateway para cambiar entre modo automático y manual, y controlar los actuadores.
// Definción de librerias a utilizar
#include <Arduino.h>
#include <WiFi.h>
#include <esp_now.h>
#include <Wire.h>
#include <BH1750.h>
#include <DHT.h>

#define DHTPIN          4
#define DHTTYPE         DHT11
#define PIN_SUELO       34
#define PIN_TRIG        18
#define PIN_ECHO        19
#define PIN_BOMBA       25
#define PIN_VENTILADOR  26
#define PIN_LAMPARA     27

DHT dht(DHTPIN, DHTTYPE);
BH1750 lightMeter;

uint8_t gatewayMAC[] = {0x00, 0x4B, 0x12, 0x9A, 0x14, 0x8C};

float SP_HUMEDAD_SUELO = 55.0, SP_TEMPERATURA = 28.0, SP_LUX = 600.0;
float Kp = 2, Ki = 0.05, Kd = 0.01, errorLux = 0, errorAnterior = 0, integral = 0;
int pwmLux = 0;

volatile bool modoAutomatico = true;
volatile bool bombaManual = false;
volatile bool ventiladorManual = false;
volatile int pwmManual = 0;
volatile int tiempoRiegoManual = 10;

float temperatura = 0, humedadAire = 0, humedadSueloPorcentaje = 0, lux = 0, nivelTanque = 0;

typedef struct {
    float temperatura, humedadAire, humedadSuelo, lux, nivelTanque;
    bool bomba, ventilador, riegoActivo;
    int pwmLuz;
} DatosSistema;
DatosSistema datos;

typedef struct {
    bool modoAutomatico;
    bool bomba;
    bool ventilador;
    int pwmLuz;
    int tiempoRiego;
} ComandoControl;

volatile ComandoControl comando = {true, false, false, 0, 10};
unsigned long tiempoInicioRiego = 0;
bool riegoActivo = false;

void onReceiveData(const uint8_t *mac, const uint8_t *incomingData, int len) {
    memcpy((void*)&comando, incomingData, sizeof(ComandoControl));
    modoAutomatico = comando.modoAutomatico;
    bombaManual = comando.bomba;
    ventiladorManual = comando.ventilador;
    pwmManual = comando.pwmLuz;
    tiempoRiegoManual = comando.tiempoRiego;
}

void onDataSent(const uint8_t *mac_addr, esp_now_send_status_t status) {}

float leerHumedadSuelo() {
    return constrain(map(analogRead(PIN_SUELO), 3200, 1200, 0, 100), 0, 100);
}

float leerNivelTanque() {
    digitalWrite(PIN_TRIG, LOW); delayMicroseconds(2); digitalWrite(PIN_TRIG, HIGH);
    delayMicroseconds(10); digitalWrite(PIN_TRIG, LOW);
    long duracion = pulseIn(PIN_ECHO, HIGH, 30000);
    float nivel = 100 - constrain((( (duracion * 0.0343 / 2.0) - 5.0) / 20.0) * 100.0, 0, 100);
    return nivel;
}

int calcularPID(float luxActual) {
    errorLux = SP_LUX - luxActual;
    integral += errorLux;
    float salida = Kp * errorLux + Ki * integral + Kd * (errorLux - errorAnterior);
    errorAnterior = errorLux;
    pwmLux = constrain(pwmLux + (int)salida, 0, 255);
    return pwmLux;
}

void controlAutomatico() {
    static bool estadoBomba = false;
    if (humedadSueloPorcentaje < 45) estadoBomba = true;
    if (humedadSueloPorcentaje > 65 || nivelTanque < 10) estadoBomba = false;
    digitalWrite(PIN_BOMBA, estadoBomba);
    
    digitalWrite(PIN_VENTILADOR, temperatura > 30);
    ledcWrite(0, calcularPID(lux));
}

void controlManual() {
    digitalWrite(PIN_VENTILADOR, ventiladorManual);
    ledcWrite(0, pwmManual);
    if (bombaManual) {
        if (!riegoActivo) { digitalWrite(PIN_BOMBA, HIGH); tiempoInicioRiego = millis(); riegoActivo = true; }
        else if (millis() - tiempoInicioRiego >= (tiempoRiegoManual * 1000UL)) { digitalWrite(PIN_BOMBA, LOW); riegoActivo = false; }
    } else { digitalWrite(PIN_BOMBA, LOW); riegoActivo = false; }
}

void enviarDatos() {
    datos = {temperatura, humedadAire, humedadSueloPorcentaje, lux, nivelTanque, (bool)digitalRead(PIN_BOMBA), (bool)digitalRead(PIN_VENTILADOR), riegoActivo, pwmLux};
    esp_now_send(gatewayMAC, (uint8_t*)&datos, sizeof(DatosSistema));
}

void setup() {
    Serial.begin(115200); WiFi.mode(WIFI_STA);
    pinMode(PIN_BOMBA, OUTPUT); pinMode(PIN_VENTILADOR, OUTPUT);
    pinMode(PIN_TRIG, OUTPUT); pinMode(PIN_ECHO, INPUT);
    Wire.begin(); dht.begin();
    lightMeter.begin(BH1750::CONTINUOUS_HIGH_RES_MODE);
    ledcSetup(0, 5000, 8); ledcAttachPin(PIN_LAMPARA, 0);
    if (esp_now_init() == ESP_OK) {
        esp_now_register_recv_cb(onReceiveData);
        esp_now_peer_info_t peerInfo = {};
        memcpy(peerInfo.peer_addr, gatewayMAC, 6);
        esp_now_add_peer(&peerInfo);
    }
}

void loop() {
    temperatura = dht.readTemperature(); humedadAire = dht.readHumidity();
    humedadSueloPorcentaje = leerHumedadSuelo(); lux = lightMeter.readLightLevel();
    nivelTanque = leerNivelTanque();
    
    if (modoAutomatico) controlAutomatico(); else controlManual();
    enviarDatos();
    delay(2000);
}