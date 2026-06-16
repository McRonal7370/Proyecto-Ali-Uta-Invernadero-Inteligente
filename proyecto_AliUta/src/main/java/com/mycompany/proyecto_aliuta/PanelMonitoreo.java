package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PanelMonitoreo extends JPanel {
    // Colores del tema oscuro
    private final Color COLOR_FONDO = new Color(18, 18, 24);
    private final Color COLOR_PANELES = new Color(30, 30, 42);
    private final Color COLOR_TEXTO = new Color(255, 255, 255);
    private final Color COLOR_TEXTO_SEC = new Color(150, 150, 160);
    private final Color COLOR_EXITO = new Color(46, 204, 113);
    private final Color COLOR_ERROR = new Color(231, 76, 60);

    // Etiquetas globales que actualizará el GestorSerial
    public JLabel lblTemp, lblHum, lblHumAire, lblLuz, lblNivel; 
    private JTextArea txtConsola;

    public PanelMonitoreo() {
        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TÍTULO DEL PANEL ---
        JLabel lblTituloPanel = new JLabel("PANEL DE MONITOREO EN TIEMPO REAL");
        lblTituloPanel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTituloPanel.setForeground(COLOR_TEXTO);
        add(lblTituloPanel, BorderLayout.NORTH);

        // --- CONTENEDOR DE TARJETAS (GRID DE INDICADORES) ---
        // Usamos un Grid de 2 filas y 3 columnas estrictas
        JPanel panelGrid = new JPanel(new GridLayout(2, 3, 15, 15));
        panelGrid.setBackground(COLOR_FONDO);

        // Crear las 5 tarjetas de sensores independientes (Nombres blindados contra cruces)
        lblTemp = crearTarjetaSensor(panelGrid, "SYSTEM_TEMP", "TEMPERATURA","0.0 °C");                            
        lblHum = crearTarjetaSensor(panelGrid, "SYSTEM_HUM_SUELO", " HUMEDAD DEL SUELO", "0.0 %");
        lblHumAire = crearTarjetaSensor(panelGrid, "SYSTEM_HUM_DHT11", " HUMEDAD AMBIENTE", "0.0 %"); 
        lblLuz = crearTarjetaSensor(panelGrid, "SYSTEM_LUX", " LUMINOSIDAD", "0 lx");
        lblNivel = crearTarjetaSensor(panelGrid, "SYSTEM_NIVEL", " NIVEL DE AGUA", "0.0 %");
// Tarjeta de información del sistema para rellenar la celda 6 del Grid
        JPanel tarjetaInfo = new JPanel(new BorderLayout());
        tarjetaInfo.setBackground(COLOR_PANELES);
        tarjetaInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Estructura HTML limpia: Sin el título superior, directo a los integrantes
        String textoHTML = "<html><center>"
                + "<font color='#00e5ff' size='3'><b>INTEGRANTES:</b></font><br>"
                + "<font color='#ffffff'>" // Blanco puro para la lista de nombres
                + "Ronal Meza Alejo<br>"
                + "Kevin Cañari Marcos<br>"
                + "Aldair Crispin<br>"
                + "Caleb Carpio<br>"
                + "Ronny Melendez"
                + "</font></center></html>";

        JLabel lblInfo = new JLabel(textoHTML);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        
        tarjetaInfo.add(lblInfo, BorderLayout.CENTER);
        panelGrid.add(tarjetaInfo);

        add(panelGrid, BorderLayout.CENTER);

        // --- CONSOLA DE EVENTOS (INFERIOR) ---
        JPanel panelConsola = new JPanel(new BorderLayout(5, 5));
        panelConsola.setBackground(COLOR_PANELES);
        panelConsola.setPreferredSize(new Dimension(getWidth(), 160)); // Ajuste de altura para evitar solapamientos
        panelConsola.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_TEXTO_SEC, 1), 
                " Historial de Eventos Recientes ", 
                0, 0, 
                new Font("Segoe UI", Font.BOLD, 12), 
                COLOR_TEXTO_SEC
        ));

        txtConsola = new JTextArea();
        txtConsola.setBackground(new Color(22, 22, 30));
        txtConsola.setForeground(COLOR_EXITO);
        txtConsola.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtConsola.setEditable(false);
        txtConsola.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollConsola = new JScrollPane(txtConsola);
        scrollConsola.setBorder(BorderFactory.createEmptyBorder());
        panelConsola.add(scrollConsola, BorderLayout.CENTER);

        add(panelConsola, BorderLayout.SOUTH);
        
        registrarEvento("Panel de Monitoreo inicializado correctamente.", false);
    }

    // Método auxiliar optimizado - Asigna un nombre único al componente para evitar capturas erróneas de Layouts
    private JLabel crearTarjetaSensor(JPanel contenedor, String idComponente, String titulo, String valorInicial) {
        JPanel tarjeta = new JPanel(new BorderLayout(5, 5));
        tarjeta.setBackground(COLOR_PANELES);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tarjeta.setName("PANEL_" + idComponente); // Blindaje de contenedor

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(COLOR_TEXTO_SEC);

        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValor.setForeground(COLOR_TEXTO);
        lblValor.setHorizontalAlignment(SwingConstants.CENTER);
        lblValor.setName("LBL_" + idComponente); // Blindaje de etiqueta

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(lblValor, BorderLayout.CENTER);
        
        contenedor.add(tarjeta);
        return lblValor; 
    }

    public void registrarEvento(String mensaje, boolean esError) {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String prefijo = esError ? " [ERROR] " : " [INFO] ";
        
        txtConsola.append("[" + hora + "]" + prefijo + mensaje + "\n");
        txtConsola.setCaretPosition(txtConsola.getDocument().getLength()); 
    }

    public void mostrarDesconexion() {
        lblTemp.setText("--- °C");
        lblHum.setText("--- %");
        lblHumAire.setText("--- %"); 
        lblLuz.setText("--- lx");
        lblNivel.setText("--- %");
    }
}