package com.mycompany.proyecto_aliuta;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;
import javax.swing.Timer;
import java.awt.Color;
import javax.swing.SwingUtilities;

public class GestorSerial {
    private SerialPort puerto;
    private PanelMonitoreo panel;
    private PanelHistorial historial;    
    private PanelGraficas panelGraficas; 
    
    private long tiempoUltimoDato = 0;
    private Timer watchdog;
    private boolean enAlarmaDesconexion = false;

    public GestorSerial(PanelMonitoreo panel, PanelHistorial historial, PanelGraficas panelGraficas) { 
        this.panel = panel; 
        this.historial = historial;
        this.panelGraficas = panelGraficas; 
        iniciarWatchdog();
    }

    private void iniciarWatchdog() {
        watchdog = new Timer(1000, e -> {
            if (System.currentTimeMillis() - tiempoUltimoDato > 5000 && tiempoUltimoDato != 0) {
                if (!enAlarmaDesconexion) {
                    enAlarmaDesconexion = true;
                    panel.registrarEvento("Pérdida de comunicación (Timeout).", true);
                    panel.mostrarDesconexion(); 
                    SwingUtilities.invokeLater(() -> panelGraficas.actualizarDatoReal(0.0));
                }
            }
        });
        watchdog.start();
    }

    public void iniciarConexion(String portName) {
        if (portName == null || portName.isEmpty() || portName.equals("Sin puertos")) return;

        puerto = SerialPort.getCommPort(portName);
        puerto.setBaudRate(115200);
        puerto.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        
        if (puerto.openPort()) {
            panel.registrarEvento("Conexión abierta en " + portName, false);
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
                            panel.registrarEvento("Conexión recuperada.", false);
                        }
                        
                        // Lectura segura de valores
                        double tempVal = json.optDouble("temp", 0.0);
                        double humSueloVal = json.optDouble("humSuelo", 0.0);
                        double humAireVal = json.optDouble("humAire", 0.0); 
                        double luxVal = json.optDouble("lux", 0.0);
                        double nivelVal = json.optDouble("nivel", 0.0);
                        boolean riego = json.optBoolean("riego", false);
                        int pwm = json.optInt("pwm", 0);
                        int bomba = json.optInt("bomba", 0);
                        int vent = json.optInt("vent", 0);

                        SwingUtilities.invokeLater(() -> {
                            try {
                                panel.lblTemp.setText(String.format("%.1f °C", tempVal));
                                panel.lblHum.setText(String.format("%.1f %%", humSueloVal));
                                panel.lblHumAire.setText(String.format("%.1f %%", humAireVal)); 
                                panel.lblLuz.setText(String.format("%.0f lx", luxVal));
                                panel.lblNivel.setText(String.format("%.1f %%", nivelVal));
                                
                                panel.lblHum.setForeground(riego ? Color.CYAN : Color.WHITE);
                                
                                String var = panelGraficas.getVariableActual();
                                if (var.equals("Temperatura")) panelGraficas.actualizarDatoReal(tempVal);
                                else if (var.equals("Humedad")) panelGraficas.actualizarDatoReal(humSueloVal);
                                else if (var.equals("Nivel de Agua")) panelGraficas.actualizarDatoReal(nivelVal);
                                else if (var.equals("Luz")) panelGraficas.actualizarDatoReal(luxVal);
                                
                                // Registro en el historial con el nuevo método estructurado
                                historial.registrarDatosEstructurados("SENSOR", "Auto", tempVal, humAireVal, humSueloVal, nivelVal, luxVal, "AUTO", (bomba==1), 0, (vent==1), pwm);
                                
                            } catch (Exception ex) {
                                System.err.println("Error actualizando UI: " + ex.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        // Ignorar tramas vacías o corruptas
                    }
                }
            }).start();
        } else {
            panel.registrarEvento("Error al abrir puerto " + portName, true);
        }
    }

    public void enviarComando(String comando) {
        if (puerto != null && puerto.isOpen()) {
            puerto.writeBytes((comando + "\n").getBytes(), (comando + "\n").length());
            panel.registrarEvento("Orden enviada: " + comando, false);
        }
    }
}