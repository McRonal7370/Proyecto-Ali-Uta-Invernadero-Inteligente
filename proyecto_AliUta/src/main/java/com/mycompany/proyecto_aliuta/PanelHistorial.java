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
    private final String RUTA_ARCHIVO = "C:/Users/yrona/OneDrive/Desktop/proyecto_ali_uta/Historial_Sensores.csv";

    public PanelHistorial() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(18, 18, 24));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("HISTORIAL DE DATOS Y EVENTOS");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        add(lblTitulo, BorderLayout.NORTH);

        // --- 1. Configurar la Tabla Visual ---
        String[] columnas = {"Hora", "Tipo de Evento", "Detalles"};
        modeloTabla = new DefaultTableModel(columnas, 0); 
        
        tabla = new JTable(modeloTabla);
        tabla.setBackground(new Color(30, 30, 42));
        tabla.setForeground(Color.WHITE);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setRowHeight(30);
        tabla.getTableHeader().setBackground(new Color(0, 229, 255));
        tabla.getTableHeader().setForeground(Color.BLACK);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(new Color(18, 18, 24));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 95)));
        
        add(scrollPane, BorderLayout.CENTER);

        // --- 2. Panel de Botones de Gestión (Añadido abajo) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setBackground(new Color(18, 18, 24));

        // Botón para abrir la ubicación del Excel
        JButton btnAbrirCarpeta = new JButton("📂 Abrir Carpeta Excel");
        btnAbrirCarpeta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAbrirCarpeta.setForeground(Color.BLACK);
        btnAbrirCarpeta.setBackground(new Color(0, 229, 255));
        btnAbrirCarpeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAbrirCarpeta.addActionListener(e -> abrirCarpetaContenedora());

        // Botón para borrar los datos
        JButton btnBorrarDatos = new JButton("🗑️ Borrar Historial");
        btnBorrarDatos.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBorrarDatos.setForeground(Color.WHITE);
        btnBorrarDatos.setBackground(new Color(231, 76, 60)); // Color rojo
        btnBorrarDatos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBorrarDatos.addActionListener(e -> reiniciarHistorial());

        panelBotones.add(btnAbrirCarpeta);
        panelBotones.add(btnBorrarDatos);
        add(panelBotones, BorderLayout.SOUTH);

        // --- 3. Preparar el archivo CSV ---
        inicializarArchivoCSV();
    }

    private void inicializarArchivoCSV() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            File carpeta = archivo.getParentFile();
            if (carpeta != null && !carpeta.exists()) {
                carpeta.mkdirs(); 
                System.out.println("Carpeta del proyecto creada en el Escritorio.");
            }

            if (!archivo.exists()) {
                PrintWriter writer = new PrintWriter(new FileWriter(archivo));
                writer.println("Hora,Tipo de Evento,Detalles");
                writer.close();
                System.out.println("Archivo CSV creado por primera vez.");
            }
        } catch (Exception e) {
            System.err.println("Error al crear el archivo CSV: " + e.getMessage());
        }
    }

    // --- Guarda en la tabla y en el Excel a la vez ---
    public void agregarRegistro(String tipoEvento, String detalles) {
        String horaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

        SwingUtilities.invokeLater(() -> {
            modeloTabla.insertRow(0, new Object[]{horaActual, tipoEvento, detalles}); 
        });

        try {
            FileWriter fw = new FileWriter(RUTA_ARCHIVO, true); 
            PrintWriter pw = new PrintWriter(fw);
            
            String detalleLimpio = detalles.replace(",", ";"); 
            
            pw.println(horaActual + "," + tipoEvento + "," + detalleLimpio);
            pw.close();
            
        } catch (Exception e) {
            System.err.println("No se pudo guardar en el Excel: " + e.getMessage());
        }
    }

    // 🔥 NUEVO MÉTODO: Dirige al usuario directamente a la carpeta del archivo Excel
    private void abrirCarpetaContenedora() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            File carpeta = archivo.getParentFile(); // Obtiene la carpeta 'proyecto_ali_uta'
            
            if (carpeta != null && carpeta.exists()) {
                // Comando del sistema para abrir la carpeta directamente en el Explorador de Windows
                Desktop.getDesktop().open(carpeta);
            } else {
                JOptionPane.showMessageDialog(this, "La carpeta aún no se ha creado. Registra algún dato primero.", "Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la carpeta automáticamente: " + e.getMessage(), "Error del Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 🔥 NUEVO MÉTODO: Borra los datos visuales de la tabla y formatea el Excel
    private void reiniciarHistorial() {
        // Ventana de confirmación para que el usuario no borre todo por accidente
        int confirmar = JOptionPane.showConfirmDialog(
            this, 
            "¿Estás seguro de que deseas borrar permanentemente todo el historial tanto en pantalla como en el archivo Excel?", 
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmar == JOptionPane.YES_OPTION) {
            // 1. Limpiar filas de la tabla visual
            modeloTabla.setRowCount(0);

            // 2. Sobreescribir el archivo CSV borrando los datos viejos
            try {
                File archivo = new File(RUTA_ARCHIVO);
                // Al NO pasarle "true" al FileWriter, Java sobreescribe el archivo completamente
                PrintWriter writer = new PrintWriter(new FileWriter(archivo, false));
                writer.println("Hora,Tipo de Evento,Detalles"); // Dejamos solo las cabeceras básicas
                writer.close();
                
                JOptionPane.showMessageDialog(this, "El historial ha sido vaciado exitosamente.", "Historial Borrado", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al vaciar el archivo Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}