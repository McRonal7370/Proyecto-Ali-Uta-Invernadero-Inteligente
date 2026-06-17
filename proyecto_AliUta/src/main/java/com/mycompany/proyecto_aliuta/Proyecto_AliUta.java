package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;

public class Proyecto_AliUta extends JFrame {
    static {
        System.setProperty("os.arch", "amd64");
    }
    
    // Colores del tema centralizado para consistencia
    private final Color COLOR_FONDO = new Color(18, 18, 24);
    private final Color COLOR_PANELES = new Color(30, 30, 42);
    private final Color COLOR_TEXTO = new Color(255, 255, 255);

    private JPanel panelCentral;
    private CardLayout cardLayout;

    // Instancias de los paneles
    private GestorSerial gestorSerial;
    private PanelMonitoreo panelMonitoreo;
    private PanelControl panelControl;
    private PanelHistorial panelHistorial; 
    private PanelGraficas panelGraficas; 

    public Proyecto_AliUta(String puertoSeleccionado) {
        setTitle("ALIuTA - Invernadero Inteligente");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Instanciación en orden
        panelMonitoreo = new PanelMonitoreo();
        panelHistorial = new PanelHistorial(); 
        panelGraficas = new PanelGraficas(); 

        // 2. Inicializar Gestor con las instancias creadas
        gestorSerial = new GestorSerial(panelMonitoreo, panelHistorial, panelGraficas); 

        // 3. Vincular panel de control con el gestor configurado
        panelControl = new PanelControl(gestorSerial);

        // --- Menú Lateral ---
        JPanel menuLateral = new JPanel(new GridLayout(7, 1, 0, 15));
        menuLateral.setPreferredSize(new Dimension(220, getHeight()));
        menuLateral.setBackground(COLOR_PANELES);
        menuLateral.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Carga de Logo
        JLabel logoLateral = new JLabel();
        logoLateral.setHorizontalAlignment(SwingConstants.CENTER);
        java.net.URL imgURL = getClass().getResource("/Imagenes/logo_menu.png");
        if (imgURL != null) {
            ImageIcon iconoLogo = new ImageIcon(imgURL);
            Image imgLogo = iconoLogo.getImage().getScaledInstance(140, 65, Image.SCALE_SMOOTH);
            logoLateral.setIcon(new ImageIcon(imgLogo));
        }
        menuLateral.add(logoLateral);

        // Botones de navegación
        menuLateral.add(crearBotonMenu("INICIO", e -> cambiarPestana("Inicio")));
        menuLateral.add(crearBotonMenu("MONITOREO", e -> cambiarPestana("Monitoreo")));
        menuLateral.add(crearBotonMenu("CONTROL", e -> cambiarPestana("Control")));
        menuLateral.add(crearBotonMenu("GRÁFICAS", e -> cambiarPestana("Graficas")));
        menuLateral.add(crearBotonMenu("HISTORIAL", e -> cambiarPestana("Historial")));
        
        // --- Panel Central ---
        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setBackground(COLOR_FONDO);

        panelCentral.add(new PanelInicio(), "Inicio");
        panelCentral.add(panelMonitoreo, "Monitoreo");
        panelCentral.add(panelControl, "Control");
        panelCentral.add(panelGraficas, "Graficas"); 
        panelCentral.add(panelHistorial, "Historial"); 
        
        add(menuLateral, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);

        // Conexión inicial
        if (puertoSeleccionado != null && !puertoSeleccionado.isEmpty()) {
            gestorSerial.iniciarConexion(puertoSeleccionado);
        }
    } 
    
    private void cambiarPestana(String nombrePestana) {
        cardLayout.show(panelCentral, nombrePestana);
    }
        
    private JButton crearBotonMenu(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(COLOR_TEXTO);
        boton.setBackground(COLOR_PANELES);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addActionListener(accion);
        return boton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SelectorPuerto selector = new SelectorPuerto(null);
            selector.setVisible(true);
            String puerto = selector.getPuertoSeleccionado();
            if (puerto != null) new Proyecto_AliUta(puerto).setVisible(true);
        });
    }
}