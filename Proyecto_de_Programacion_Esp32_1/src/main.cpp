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

// Configuración de pines
#define DHTPIN          4
#define DHTTYPE         DHT11
#define PIN_SUELO       34
#define PIN_TRIG        18
#define PIN_ECHO        19
#define PIN_BOMBA       25
#define PIN_VENTILADOR  26
#define PIN_LAMPARA     27

// Definción de objetos y variables globales 
DHT dht(DHTPIN, DHTTYPE);
BH1750 lightMeter;

// Dirección MAC del gateway (ESP32 #2)
uint8_t gatewayMAC[] = {
    0x00, 0x4B, 0x12, 0x9A, 0x14, 0x8C
};

// Puntos de consigna (setpoints) "Recodemos que estos valores varian para cada planta" 
float SP_HUMEDAD_SUELO = 55.0;
float SP_TEMPERATURA = 28.0;
float SP_LUX = 600.0;

// Parámetros PID para el control de iluminación (ajustar dependiendo de la respuesta del sistema)
float Kp = 2 ;
float Ki = 0.05;
float Kd = 0.01;
float errorLux = 0;
float errorAnterior = 0;
float integral = 0;
int pwmLux = 0;

// Modos de control del sistema 
bool modoAutomatico = true;

// Variables para control manual
bool bombaManual = false;
bool ventiladorManual = false;
int pwmManual = 0;

// Variables para almacenar las lecturas de los sensores
float temperatura = 0;
float humedadAire = 0;
int humedadSuelo = 0;
float humedadSueloPorcentaje = 0;
float lux = 0;
float distancia = 0;
float nivelTanque = 0;

// ESTRUCTURA ESP-NOW: Esto servira para enviar los datos del nodo de campo al gateway y recibir los comandos de control desde el gateway
typedef struct
{
    float temperatura;
    float humedadAire;
    float humedadSuelo;
    float lux;
    float nivelTanque;
    bool bomba;
    bool ventilador;
    int pwmLuz;

} DatosSistema;

DatosSistema datos;
// Comando de control que se recibirá desde el gateway para controlar el sistema en modo manual o automático, y controlar la bomba, ventilador y lámpara.
typedef struct
{
    bool modoAutomatico;
    bool bomba;
    bool ventilador;
    int pwmLuz;

} ComandoControl;

ComandoControl comando;

// Callback de recepción de datos desde el gateway, en el que se actualizan las variables de control manual y el modo de operación del sistema dependiendo de los comandos recibidos.
void onReceiveData(const uint8_t *mac, const uint8_t *incomingData, int len)
{
    memcpy(&comando, incomingData, sizeof(ComandoControl));

    modoAutomatico = comando.modoAutomatico;

    if (!modoAutomatico)
    {
        bombaManual = comando.bomba;
        ventiladorManual = comando.ventilador;
        pwmManual = comando.pwmLuz;
    }
}


// Callback de envío de datos al gateway, en el que se puede verificar si el envío fue exitoso o no.
void onDataSent(const uint8_t *mac_addr, esp_now_send_status_t status)
{
    if (status != ESP_NOW_SEND_SUCCESS) {
        Serial.println(">> ¡Alerta! Fallo de enlace ESP-NOW con el Gateway.");
    }
}


// Lectura de sensores 
//Lectura de humedad del suelo utilizando un sensor resistivo, en el que se mapea la lectura analógica a un porcentaje de humedad que va desde un 0 al 100.

float leerHumedadSuelo()
{
    int lectura = analogRead(PIN_SUELO);
    float porcentaje = map(lectura,3200,1200,0,100);
    porcentaje = constrain(porcentaje, 0, 100);
    return porcentaje;
}

// Nivel de tanque utilizando un sensor ultrasónico HC-SR04, en el que se mide la distancia desde el sensor hasta la superficie del agua y se mapea a un porcentaje de nivel del tanque, considerando una distancia mínima de 5 cm (tanque lleno) y una distancia máxima de 25 cm (tanque vacío).
float leerNivelTanque()
{
    digitalWrite(PIN_TRIG, LOW);
    delayMicroseconds(2);
    digitalWrite(PIN_TRIG, HIGH);
    delayMicroseconds(10);
    digitalWrite(PIN_TRIG, LOW);
    long duracion = pulseIn(PIN_ECHO, HIGH, 30000);
    distancia = duracion * 0.0343 / 2.0;
    float nivel = ((distancia - 5.0) /(25.0 - 5.0))* 100.0;
    nivel = 100 - nivel;
    nivel = constrain(nivel, 0, 100);
    return nivel;
}

// PID para control de iluminación, en el que se calcula el error entre el punto de consigna de lux y la lectura actual, se acumula la integral del error y se calcula la derivada del error para obtener una salida que se suma a la señal PWM actual de la lámpara, y se constriñe el resultado para que esté entre 0 y 255.
int calcularPID(float luxActual)
{
    errorLux = SP_LUX - luxActual;
    integral += errorLux;
    float derivada =
        errorLux - errorAnterior;
    float salida =
        Kp * errorLux + Ki * integral + Kd * derivada;
    errorAnterior = errorLux;
    pwmLux += salida;
    pwmLux = constrain(pwmLux,0,255);
    return pwmLux;
}

// Control automático del sistema, en el que se controla la bomba de agua dependiendo de la humedad del suelo y el nivel del tanque, se controla el ventilador dependiendo de la temperatura, y se controla la lámpara utilizando el PID para mantener un nivel de lux adecuado.
void controlAutomatico()
{
    // Humedad del suelo y nivel del tanque
    static bool estadoBomba = false;
    if (humedadSueloPorcentaje < 45)
        estadoBomba = true;
    if (humedadSueloPorcentaje > 65)
        estadoBomba = false;
    if (nivelTanque < 10)
        estadoBomba = false;
    digitalWrite(PIN_BOMBA,estadoBomba);
    // Temperatura
    static bool estadoVent = false;
    if (temperatura > 30)
        estadoVent = true;
    if (temperatura < 26)
        estadoVent = false;
    digitalWrite(PIN_VENTILADOR,estadoVent);
    // Pid para control de iluminación
    pwmLux = calcularPID(lux);
    ledcWrite(0, pwmLux);
}
// Control manual del sistema, en el que se controlan la bomba, el ventilador y la lámpara dependiendo de los comandos recibidos desde el gateway (ESP32#2)
void controlManual()
{
    digitalWrite(PIN_BOMBA,bombaManual);
    digitalWrite(PIN_VENTILADOR,ventiladorManual);
    ledcWrite(0,pwmManual);
}

// Función para enviar los datos del nodo de campo al gateway utilizando ESP-NOW, en el que se llena una estructura con las lecturas de los sensores y el estado de los actuadores, y se envía al gateway utilizando la función esp_now_send.
void enviarDatos()
{
    datos.temperatura = temperatura;
    datos.humedadAire = humedadAire;
    datos.humedadSuelo = humedadSueloPorcentaje;
    datos.lux = lux;
    datos.nivelTanque = nivelTanque;
    datos.bomba = digitalRead(PIN_BOMBA);
    datos.ventilador = digitalRead(PIN_VENTILADOR);
    datos.pwmLuz =pwmLux;

    esp_now_send(gatewayMAC,(uint8_t*)&datos,sizeof(datos));
}

// Función setup, en la que se inicializan los sensores, se configura el ESP-NOW para la comunicación con el gateway, y se establece el modo de operación del sistema.
void setup()
{
    Serial.begin(115200);
    WiFi.mode(WIFI_STA);
    pinMode(PIN_BOMBA, OUTPUT);
    pinMode(PIN_VENTILADOR, OUTPUT);
    pinMode(PIN_TRIG, OUTPUT);
    pinMode(PIN_ECHO, INPUT);
    Wire.begin();
    dht.begin();
    lightMeter.begin(BH1750::CONTINUOUS_HIGH_RES_MODE);
    ledcSetup(0,5000,8);
    ledcAttachPin(PIN_LAMPARA,0);
    if (esp_now_init() != ESP_OK)
    {
        Serial.println(
            "Error ESP-NOW");
        return;
    }
    esp_now_register_recv_cb(onReceiveData);
    esp_now_register_send_cb(onDataSent);
    esp_now_peer_info_t peerInfo = {};
    memcpy(peerInfo.peer_addr,gatewayMAC,6);
    peerInfo.channel = 0;
    peerInfo.encrypt = false;
    esp_now_add_peer(&peerInfo);
    Serial.println("Nodo de Campo iniciado");
}
// Función loop, en la que se leen los sensores, se controla el sistema dependiendo del modo de operación, se envían los datos al gateway, y se imprimen las lecturas en el monitor serial para monitoreo local.
void loop()
{
    humedadAire = dht.readHumidity();
    temperatura = dht.readTemperature();
    humedadSueloPorcentaje = leerHumedadSuelo();
    lux = lightMeter.readLightLevel();
    nivelTanque = leerNivelTanque();
    if (modoAutomatico)
    {
        controlAutomatico();
    }
    else
    {
        controlManual();
    }
    enviarDatos();
    Serial.println("---------------");
    Serial.print("Temp: ");
    Serial.println(temperatura);
    Serial.print("Hum Aire: ");
    Serial.println(humedadAire);
    Serial.print("Hum Suelo: ");
    Serial.println(humedadSueloPorcentaje);
    Serial.print("Lux: ");
    Serial.println(lux);
    Serial.print("Nivel: ");
    Serial.println(nivelTanque);
    Serial.print("Modo: ");

    if (modoAutomatico){
        Serial.println("AUTO");
    }else { 
        Serial.println("MANUAL");
    }
    delay(2000);
    
}