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
// Incluyendo las librerías necesarias para el proyecto
#include <Arduino.h>
#include <WiFi.h>
#include <esp_now.h>

// Direccion MAC del ESP32 #1 (campo)
uint8_t nodoMAC[] =
{
    0x00,0x4B,0x12,0x9B,0x30,0xC0
};

// Estructura de datos para recibir del nodo de campo y enviar al nodo de campo 
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

DatosSistema datosRecibidos;

// Estructura de datos para enviar al nodo de campo y recibir del nodo de campo
typedef struct
{
    bool modoAutomatico;
    bool bomba;
    bool ventilador;
    int pwmLuz;

} ComandoControl;

ComandoControl comando;
// Recepción de datos del nodo de campo (ESP32 #1 - ESP-NOW)
void onReceiveData(const uint8_t *mac, const uint8_t *incomingData, int len)
{
    memcpy(&datosRecibidos, incomingData, sizeof(DatosSistema));

    Serial.print("{");
    Serial.print("\"temp\":");
    Serial.print(datosRecibidos.temperatura);
    Serial.print(",\"humAire\":");
    Serial.print(datosRecibidos.humedadAire);
    Serial.print(",\"humSuelo\":");
    Serial.print(datosRecibidos.humedadSuelo);
    Serial.print(",\"lux\":");
    Serial.print(datosRecibidos.lux);
    Serial.print(",\"nivel\":");
    Serial.print(datosRecibidos.nivelTanque);
    Serial.print(",\"bomba\":");
    Serial.print(datosRecibidos.bomba);
    Serial.print(",\"vent\":");
    Serial.print(datosRecibidos.ventilador);
    Serial.print(",\"pwm\":");
    Serial.print(datosRecibidos.pwmLuz);
    Serial.println("}");
}

// Enviar comando al nodo de campo (ESP32 #1-ESPNOW)
void enviarComando()
{
    esp_now_send(
        nodoMAC,
        (uint8_t*)&comando,
        sizeof(comando));
}
void procesarLinea(String linea)
{
    linea.trim();
    
    if (linea.indexOf("AUTO") >= 0)
    {
        comando.modoAutomatico = true;
        enviarComando();
        Serial.println("{\"status\":\"AUTO_OK\"}");
        return;
    }
    
    if (linea.indexOf("MANUAL") >= 0)
    {
        comando.modoAutomatico = false;
        
        // Evaluamos presencia de subcadenas ignorando si hay espacios extra
        comando.bomba = (linea.indexOf("\"bomba\":1") >= 0);
        comando.ventilador = (linea.indexOf("\"vent\":1") >= 0);
        
        int pos = linea.indexOf("\"pwm\":");
        if (pos >= 0)
        {
            // Extrae desde el número en adelante y limpia caracteres extra
            String valor = linea.substring(pos + 6);
            valor.trim();
            comando.pwmLuz = valor.toInt();
        }
        
        enviarComando();
        Serial.println("{\"status\":\"MANUAL_OK\"}");
    }
}

// Configuración inicial del ESP32 #2 (Gateway)
void setup()
{
    Serial.begin(115200);
    WiFi.mode(WIFI_STA);
    if (esp_now_init() != ESP_OK)
    {
        Serial.println("ESP-NOW ERROR");
        return;
    }
    esp_now_register_recv_cb(onReceiveData);
    esp_now_peer_info_t peerInfo = {};
    memcpy(peerInfo.peer_addr,nodoMAC,6);
    peerInfo.channel = 0;
    peerInfo.encrypt = false;
    if (esp_now_add_peer(&peerInfo) != ESP_OK)
    {
        Serial.println("Peer Error");
        return;
    }
    comando.modoAutomatico = true;
    comando.bomba = false;
    comando.ventilador = false;
    comando.pwmLuz = 0;
    Serial.println("Gateway iniciado");
}

// Loop principal para procesar comandos recibidos por Serial
void loop()
{
    if (Serial.available())
    {
        String linea = Serial.readStringUntil('\n');
        procesarLinea(linea);
    }
}