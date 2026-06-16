package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;

public class Proyecto_AliUta extends JFrame {
    static {
        System.setProperty("os.arch", "amd64");
    }
    private final Color COLOR_FONDO = new Color(18, 18, 24);
    private final Color COLOR_PANELES = new Color(30, 30, 42);
    private final Color COLOR_TEXTO = new Color(255, 255, 255);

    private JPanel panelCentral;
    private CardLayout cardLayout;

    // Variables globales de interfaz
    private GestorSerial gestorSerial;
    private PanelMonitoreo panelMonitoreo;
    private PanelControl panelControl;
    private PanelHistorial panelHistorial; 
    private PanelGraficas panelGraficas; 

    public Proyecto_AliUta(String puertoSeleccionado) {
        setTitle("ALIuTA - Sistema de Automatización");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Instanciar los paneles de forma INDEPENDIENTE primero
        panelMonitoreo = new PanelMonitoreo();
        panelHistorial = new PanelHistorial(); 
        panelGraficas = new PanelGraficas(); 

        // 2. Inicializar el Gestor Serial pasándole las tres instancias limpias
        gestorSerial = new GestorSerial(panelMonitoreo, panelHistorial, panelGraficas); 

        // 3. Vincular el panel de control una SOLA VEZ
        panelControl = new PanelControl(gestorSerial);

        // --- Menú Lateral ---
        JPanel menuLateral = new JPanel(new GridLayout(7, 1, 0, 15));
        menuLateral.setPreferredSize(new Dimension(220, getHeight()));
        menuLateral.setBackground(COLOR_PANELES);
        menuLateral.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Logo
        JLabel logoLateral = new JLabel();
        logoLateral.setHorizontalAlignment(SwingConstants.CENTER);
        java.net.URL imgURL = getClass().getResource("/Imagenes/logo_menu.png");
        if (imgURL != null) {
            ImageIcon iconoLogo = new ImageIcon(imgURL);
            Image imgLogo = iconoLogo.getImage().getScaledInstance(140, 65, Image.SCALE_SMOOTH);
            logoLateral.setIcon(new ImageIcon(imgLogo));
        }
        menuLateral.add(logoLateral);

        // Botones del Menú
        JButton btnInicio = crearBotonMenu("INICIO");
        JButton btnMonitoreo = crearBotonMenu("MONITOREO");
        JButton btnControl = crearBotonMenu("CONTROL");
        JButton btnGraficas = crearBotonMenu("GRÁFICAS");
        JButton btnHistorial = crearBotonMenu("HISTORIAL");

        menuLateral.add(btnInicio);
        menuLateral.add(btnMonitoreo);
        menuLateral.add(btnControl);
        menuLateral.add(btnGraficas);
        menuLateral.add(btnHistorial);
        
        // --- Panel Central (CardLayout) ---
        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setBackground(COLOR_FONDO);

        // AGREGAR LAS INSTANCIAS EXACTAS AL CARDLAYOUT
        panelCentral.add(new PanelInicio(), "Inicio");
        panelCentral.add(panelMonitoreo, "Monitoreo");
        panelCentral.add(panelControl, "Control");
        panelCentral.add(panelGraficas, "Graficas"); 
        panelCentral.add(panelHistorial, "Historial"); 

        // --- Acción de botones para cambiar de pestaña ---
        btnInicio.addActionListener(e -> cambiarPestana("Inicio"));
        btnMonitoreo.addActionListener(e -> cambiarPestana("Monitoreo"));
        btnControl.addActionListener(e -> cambiarPestana("Control"));
        
        // Corregido: Ahora se comporta de manera estable y definida igual que los demás paneles
        btnGraficas.addActionListener(e -> {
            cambiarPestana("Graficas");
            // Forzar refresco específico para asegurar que la gráfica se expanda correctamente
            panelGraficas.revalidate();
            panelGraficas.repaint();
        });

        btnHistorial.addActionListener(e -> cambiarPestana("Historial"));
        
        add(menuLateral, BorderLayout.WEST);
        add(panelCentral, BorderLayout.CENTER);

        // Iniciar conexión serial al método nativo
        if (puertoSeleccionado != null && !puertoSeleccionado.isEmpty()) {
            gestorSerial.iniciarConexion(puertoSeleccionado);
        } else {
            JOptionPane.showMessageDialog(this, "No se detectó un puerto activo. Conecta tu hardware.");
        }
    } 
    
    // Método centralizado para conmutar vistas limpiamente
    private void cambiarPestana(String nombrePestana) {
        cardLayout.show(panelCentral, nombrePestana);
        panelCentral.revalidate();
        panelCentral.repaint();
    }
        
    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(COLOR_TEXTO);
        boton.setBackground(COLOR_PANELES);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static void main(String[] args) {
        System.setProperty("os.arch", "amd64");
        
        SwingUtilities.invokeLater(() -> {
            SelectorPuerto selector = new SelectorPuerto(null);
            selector.setVisible(true);
            
            String puerto = selector.getPuertoSeleccionado();
            
            if (puerto == null) {
                System.exit(0);
                return;
            }
            
            new Proyecto_AliUta(puerto).setVisible(true);
        });
    }
}