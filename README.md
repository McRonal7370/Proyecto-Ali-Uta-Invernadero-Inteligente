# 🌱 Proyecto Ali Uta – Invernadero Inteligente

## Descripción General

Ali Uta es un sistema de monitoreo y control para un invernadero inteligente desarrollado como parte del curso de Programación Avanzada.

El proyecto utiliza dos módulos ESP32 que se comunican mediante el protocolo ESP-NOW para adquirir, transmitir y procesar información proveniente de diversos sensores ambientales. Los datos son visualizados y gestionados desde una aplicación desarrollada en Java.

---

## Objetivos

- Monitorear variables ambientales en tiempo real.
- Registrar información de sensores.
- Visualizar datos mediante una interfaz gráfica.
- Permitir el control remoto de dispositivos conectados al sistema mediante un control manual y un automatico
- Implementar comunicación inalámbrica eficiente entre microcontroladores.

---

# Arquitectura del Sistema

```text
Sensores
   │
   ▼
ESP32 Emisor
   │
   │ ESP-NOW
   ▼
ESP32 Receptor
   │
   │ Comunicación Serial
   ▼
Aplicación Java
   │
   ▼
Usuario
```

---

# Sensores Implementados

### 🌡️ DHT11

Permite medir:

- Temperatura ambiente
- Humedad relativa

### 🌱 Sensor Capacitivo de Humedad de Suelo V1.2

Permite determinar el nivel de humedad presente en el sustrato.

### ☀️ Sensor de Luminosidad BH1750

Permite medir la intensidad luminosa del entorno en lux.

### 📏 Sensor Ultrasónico HC-SR04

Permite medir distancias mediante reflexión de ondas ultrasónicas.

Puede utilizarse para:

- Medición de nivel de agua.
- Detección de objetos.
- Monitoreo de depósitos.

---

# Tecnologías Utilizadas

## Hardware

- ESP32 DevKit V1 (2 unidades)
- Sensor DHT11
- Sensor Capacitivo de Humedad de Suelo V1.2
- Sensor BH1750
- Sensor Ultrasónico HC-SR04
- Actuadores:
- Bomba de 5V
- Lampara de 12V
- Ventilador de 12V

## Software

- Visual Studio Code
- PlatformIO
- Java JDK 21
- Apache Maven
- NetBeans IDE
- Git
- GitHub

---

# Estructura del Repositorio

```text
Proyecto-Ali-Uta-Invernadero-Inteligente/

├── Proyecto_de_Programacion_Esp32_1/
│   └── Código del ESP32 Emisor
│
├── Proyecto_de_progracion_ESP32_2/
│   └── Código del ESP32 Receptor
│
└── proyecto_AliUta/
    └── Aplicación Java
```

---

# Instalación

## 1. Clonar el Repositorio

```bash
git clone https://github.com/McRonal7370/Proyecto-Ali-Uta-Invernadero-Inteligente.git
```

---

# Configuración de los ESP32

## ESP32 Emisor

1. Abrir la carpeta:

```text
Proyecto_de_Programacion_Esp32_1
```

2. Abrirla en Visual Studio Code.

3. Instalar PlatformIO si aún no está instalado.

4. Compilar el proyecto:

```text
PlatformIO → Build
```

5. Conectar el ESP32.

6. Cargar el firmware:

```text
PlatformIO → Upload
```

---

## ESP32 Receptor

1. Abrir la carpeta:

```text
Proyecto_de_progracion_ESP32_2
```
(la direccion MAC varia de acuerdo a cada esp32 , cambiala con la dirección de tu ESP32)
2. Compilar el proyecto.

3. Conectar el segundo ESP32.

4. Cargar el firmware.

---

# Configuración de la Aplicación Java

## Requisitos

- Java JDK 21
- Maven
- NetBeans (opcional)

---

## Abrir en NetBeans

1. Abrir NetBeans.
2. Seleccionar:

```text
Open Project
```

3. Elegir la carpeta:

```text
proyecto_AliUta
```

4. Ejecutar el proyecto.

---

## Compilar mediante Maven

Ubicarse dentro de:

```bash
cd proyecto_AliUta
```

Compilar:

```bash
mvn clean install
```

Ejecutar:

```bash
mvn exec:java
```

---

# Funcionamiento General

1. Los sensores adquieren información del entorno.
2. El ESP32 Emisor procesa las lecturas.
3. Los datos son enviados mediante ESP-NOW.
4. El ESP32 Receptor recibe la información.
5. Los datos son enviados por puerto serial a la aplicación Java.
6. La aplicación permite:

- Visualizar variables ambientales.
- Registrar información histórica.
- Mostrar gráficos.
- Enviar comandos hacia los ESP32.
- Controlar el sistema desde una interfaz gráfica.

---

# Integrantes

Proyecto desarrollado para la Universidad Nacional Mayor de San Marcos.
Alumnos: 
- MEZA ALEJO YOBER RONAL 
- ALDAIR ANTONIO CRISPI PIMENTEL 
- CALEB BERNABE CARPIO ARMAS 
- RONNY MELENDEZ RUIZ 
- KEVIN CAÑARI MARCOS

-Curso: Programación Avanzada
-Año: 2026
