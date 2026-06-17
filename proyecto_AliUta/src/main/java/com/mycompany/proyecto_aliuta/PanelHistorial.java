package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PanelHistorial extends JPanel {

    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private final String RUTA_ARCHIVO = "D:\\Proyectos de Ronal\\Historial_Sensores.csv";

    public PanelHistorial() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(18, 18, 24));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel("HISTORIAL DE DATOS Y EVENTOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        add(lblTitulo, BorderLayout.NORTH);

        // Definición de las 13 columnas 
        String[] columnas = {"Hora", "Tipo", "Evento", "Temperatura", "Hum. Amb.", "Hum. Suelo", 
                             "Nivel", "Luz", "Modo", "Bomba", "Tiempo", "Ventilador", "PWM"};
        
        modeloTabla = new DefaultTableModel(columnas, 0); 
        tabla = new JTable(modeloTabla);
        
        // Estilos de tabla
        tabla.setBackground(new Color(30, 30, 42));
        tabla.setForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(new Color(0, 229, 255));
        tabla.getTableHeader().setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(new Color(18, 18, 24));
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setBackground(new Color(18, 18, 24));
        
        JButton btnAbrirCarpeta = new JButton(" Abrir Carpeta Excel");
        btnAbrirCarpeta.addActionListener(e -> abrirCarpetaContenedora());
        
        JButton btnBorrarDatos = new JButton(" Borrar historial");
        btnBorrarDatos.setBackground(new Color(231, 76, 60));
        btnBorrarDatos.setForeground(Color.WHITE);
        btnBorrarDatos.addActionListener(e -> reiniciarHistorial());

        panelBotones.add(btnAbrirCarpeta);
        panelBotones.add(btnBorrarDatos);
        add(panelBotones, BorderLayout.SOUTH);

        inicializarArchivoCSV();
    }

    private void inicializarArchivoCSV() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null) archivo.getParentFile().mkdirs();
            if (!archivo.exists()) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(archivo))) {
                    writer.println("Hora,Tipo,Evento,Temperatura,HumAmb,HumSuelo,Nivel,Luz,Modo,Bomba,Tiempo,Ventilador,PWM");
                }
            }
        } catch (Exception e) { System.err.println("Error inicial CSV: " + e.getMessage()); }
    }

    public void registrarDatosEstructurados(String tipo, String evento, double temp, double humAmb, 
                                            double humSuelo, double nivel, double lux, String modo, 
                                            boolean bomba, int tiempoBomba, boolean vent, int pwm) {
        
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String sBomba = bomba ? "ENCENDIDO" : "APAGADO";
        String sVent = vent ? "ENCENDIDO" : "APAGADO";
        String sTiempo = (tiempoBomba > 0) ? String.valueOf(tiempoBomba) : "0";

        // Actualización Visual
        SwingUtilities.invokeLater(() -> {
            modeloTabla.insertRow(0, new Object[]{hora, tipo, evento, temp, humAmb, humSuelo, nivel, lux, modo, sBomba, sTiempo, sVent, pwm});
        });

        // Guardado CSV (Línea estructurada)
        try (PrintWriter pw = new PrintWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            pw.printf("%s,%s,%s,%.1f,%.1f,%.1f,%.1f,%.0f,%s,%s,%s,%s,%d%n", 
                      hora, tipo, evento, temp, humAmb, humSuelo, nivel, lux, modo, sBomba, sTiempo, sVent, pwm);
        } catch (Exception e) { System.err.println("Error al guardar: " + e.getMessage()); }
    }

    private void abrirCarpetaContenedora() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile().exists()) Desktop.getDesktop().open(archivo.getParentFile());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "No se pudo abrir la carpeta."); }
    }

    private void reiniciarHistorial() {
        if (JOptionPane.showConfirmDialog(this, "¿Borrar todo el historial?") == JOptionPane.YES_OPTION) {
            modeloTabla.setRowCount(0);
            try (PrintWriter writer = new PrintWriter(new FileWriter(RUTA_ARCHIVO, false))) {
                writer.println("Hora,Tipo,Evento,Temperatura,HumAmb,HumSuelo,Nivel,Luz,Modo,Bomba,Tiempo,Ventilador,PWM");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al vaciar archivo."); }
        }
    }
}