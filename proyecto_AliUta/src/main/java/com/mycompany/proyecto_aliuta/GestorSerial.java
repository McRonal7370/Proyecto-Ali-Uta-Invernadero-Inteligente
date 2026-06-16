package com.mycompany.proyecto_aliuta;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;
import javax.swing.Timer;

public class GestorSerial {
    private SerialPort puerto;
    private PanelMonitoreo panel;
    private PanelHistorial historial;    
    private PanelGraficas panelGraficas; // 🔥 NUEVO: Enlace directo con las gráficas
    
    // Variables para vigilar la conexión
    private long tiempoUltimoDato = 0;
    private Timer watchdog;
    private boolean enAlarmaDesconexion = false;

    // Se actualizó el constructor para recibir el panel de gráficas
    public GestorSerial(PanelMonitoreo panel, PanelHistorial historial, PanelGraficas panelGraficas) { 
        this.panel = panel; 
        this.historial = historial;
        this.panelGraficas = panelGraficas; // 🔥 Inicializado
        iniciarWatchdog();
    }

    private void iniciarWatchdog() {
        watchdog = new Timer(1000, e -> {
            if (System.currentTimeMillis() - tiempoUltimoDato > 5000 && tiempoUltimoDato != 0) {
                if (!enAlarmaDesconexion) {
                    enAlarmaDesconexion = true;
                    panel.registrarEvento("Pérdida de comunicación con el dispositivo (Timeout).", true);
                    panel.mostrarDesconexion(); 
                    
                    // 📉 Si se desconecta el hardware, forzamos la gráfica a caer a CERO inmediatamente
                    javax.swing.SwingUtilities.invokeLater(() -> panelGraficas.actualizarDatoReal(0.0));
                }
            }
        });
        watchdog.start();
    }

    public void iniciarConexion(String portName) {
        if (portName == null || portName.isEmpty() || portName.equals("Sin puertos")) {
            panel.registrarEvento("No se seleccionó ningún puerto. Iniciando en modo de prueba.", true);
            return;
        }

        puerto = SerialPort.getCommPort(portName);
        puerto.setBaudRate(115200);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        
        if (puerto.openPort()) {
            panel.registrarEvento("Conexión abierta exitosamente en " + portName, false);
            tiempoUltimoDato = System.currentTimeMillis(); 
            
            new Thread(() -> {
                java.util.Scanner scanner = new java.util.Scanner(puerto.getInputStream());
                while (scanner.hasNextLine()) {
                    String linea = scanner.nextLine().trim();
                    
                    try {
                        JSONObject json = new JSONObject(linea);
                        tiempoUltimoDato = System.currentTimeMillis();
                        
                        if (enAlarmaDesconexion) {
                            enAlarmaDesconexion = false;
                            panel.registrarEvento("Conexión recuperada. Recibiendo datos...", false);
                        }
                        
                        double tempVal = json.optDouble("temp", 0.0);
                        double humSueloVal = json.optDouble("humSuelo", 0.0);
                        double humAireVal = json.optDouble("humAire", 0.0); 
                        double luxVal = json.optDouble("lux", 0.0);
                        double nivelVal = json.optDouble("nivel", 0.0);

                        javax.swing.SwingUtilities.invokeLater(() -> {
                            try {
                                // 1. Actualizar textos en PanelMonitoreo
                                panel.lblTemp.setText(String.format("%.1f °C", tempVal));
                                panel.lblHum.setText(String.format("%.1f %%", humSueloVal));
                                panel.lblHumAire.setText(String.format("%.1f %%", humAireVal)); 
                                panel.lblLuz.setText(String.format("%.0f lx", luxVal));
                                panel.lblNivel.setText(String.format("%.1f %%", nivelVal));
                                
                                // 2. 🔥 ACTUALIZACIÓN EN TIEMPO REAL DE LAS GRÁFICAS DEL SCADA 🔥
                                // Dependiendo de qué botón esté seleccionado en el menú superior, inyectamos el dato correspondiente
                                if (panelGraficas.getVariableActual().equals("Temperatura")) {
                                    panelGraficas.actualizarDatoReal(tempVal);
                                } else if (panelGraficas.getVariableActual().equals("Humedad")) {
                                    panelGraficas.actualizarDatoReal(humSueloVal); // O humAireVal según prefieran graficar primero
                                } else if (panelGraficas.getVariableActual().equals("Nivel de Agua")) {
                                    panelGraficas.actualizarDatoReal(nivelVal);
                                } else if (panelGraficas.getVariableActual().equals("Luz")) {
                                    panelGraficas.actualizarDatoReal(luxVal);
                                }
                                
                                String detalle = "T:" + String.format("%.1f", tempVal) + "C|H.Suelo:" + String.format("%.1f", humSueloVal) + "%";
                                historial.agregarRegistro("LECTURA_DATOS", detalle);
                                
                                if (nivelVal < 10.0) { 
                                   panel.registrarEvento("Nivel de agua críticamente bajo en tanque.", true);
                                }
                            } catch (Exception ex) {
                                // Evitar excepciones concurrentes de Swing
                            }
                        });
                    } catch (Exception e) {
                        // Ignorar tramas corruptas
                    }
                }
            }).start();
        } else {
            panel.registrarEvento("Error crítico: No se pudo abrir el puerto " + portName + ". ¿Está en uso por otro programa?", true);
        }
    }

    public void enviarComando(String comando) {
        if (puerto != null && puerto.isOpen()) {
            byte[] bytes = (comando + "\n").getBytes();
            puerto.writeBytes(bytes, bytes.length);
            panel.registrarEvento("Orden enviada hardware: " + comando, false);
        } else {
            panel.registrarEvento("Intento de envío fallido: " + comando, true);
        }
    }
}