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
// ESP32#2 (GATEWAY) - Código para recibir datos del nodo de campo y enviar comandos de control utilizando ESP-NOW
#include <Arduino.h>
#include <WiFi.h>
#include <esp_now.h>

uint8_t nodoMAC[] = {0x00, 0x4B, 0x12, 0x9B, 0x30, 0xC0};

typedef struct {
    float temp, humA, humS, lux, niv;
    bool bmb, vent, rig;
    int pwm;
} DatosSistema;

typedef struct {
    bool modoAutomatico;
    bool bomba;
    bool ventilador;
    int pwmLuz;
    int tiempoRiego;
} ComandoControl;

ComandoControl comando;

void onReceiveData(const uint8_t *mac, const uint8_t *incomingData, int len) {
    DatosSistema d;
    memcpy(&d, incomingData, sizeof(DatosSistema));
    Serial.printf("{\"temp\":%.1f,\"humAire\":%.1f,\"humSuelo\":%.1f,\"lux\":%.1f,\"nivel\":%.1f,\"bomba\":%d,\"vent\":%d,\"riego\":%d,\"pwm\":%d}\n", 
                  d.temp, d.humA, d.humS, d.lux, d.niv, d.bmb, d.vent, d.rig, d.pwm);
}

void procesarLinea(String linea) {
    linea.trim();
    if (linea.indexOf("AUTO") >= 0) {
        comando.modoAutomatico = true;
        esp_now_send(nodoMAC, (uint8_t*)&comando, sizeof(ComandoControl));
    } else if (linea.startsWith("M,")) {
        comando.modoAutomatico = false;
        int m, b, v, p, t;
        sscanf(linea.c_str(), "M,%d,%d,%d,%d,%d", &m, &b, &v, &p, &t);
        comando.bomba = (b == 1); comando.ventilador = (v == 1);
        comando.pwmLuz = p; comando.tiempoRiego = t;
        esp_now_send(nodoMAC, (uint8_t*)&comando, sizeof(ComandoControl));
    }
}

void setup() {
    Serial.begin(115200); WiFi.mode(WIFI_STA);
    esp_now_init();
    esp_now_register_recv_cb(onReceiveData);
    esp_now_peer_info_t p = {}; memcpy(p.peer_addr, nodoMAC, 6);
    esp_now_add_peer(&p);
}

void loop() {
    if (Serial.available()) procesarLinea(Serial.readStringUntil('\n'));
}