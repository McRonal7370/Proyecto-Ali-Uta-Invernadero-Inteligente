package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class PanelControl extends JPanel {

    private GestorSerial gestorSerial;
    
    // Variables de estado
    private boolean isAuto = true;
    private boolean isBombaOn = false;
    private boolean isVentiladorOn = false;
    private int pwmLampara = 0;
    
    // Nueva variable para el tiempo de riego (en minutos)
    private JTextField txtTiempoRiego;

    public PanelControl(GestorSerial gestorSerial) {
        this.gestorSerial = gestorSerial;

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(18, 18, 24));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel("PANEL DE CONTROL ");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(4, 1, 15, 15));
        panelCentro.setBackground(new Color(18, 18, 24));

        // Fila 1: Modo de Operación
        panelCentro.add(crearFilaModo("Modo de Control ", "AUTOMATICO", "MANUAL", new Color(0, 230, 118), new Color(255, 23, 68)));
        
        // Fila 2: Bomba de Agua (AHORA CON TEMPORIZADOR)
        panelCentro.add(crearFilaBombaConTiempo("Bomba de Agua", new Color(0, 229, 255)));
        
        // Fila 3: Ventilador
        panelCentro.add(crearFilaActuador("Ventilador", "ENCENDER", "APAGAR", new Color(0, 229, 255), new Color(80, 80, 95), "ventilador"));
        
        // Fila 4: Lámpara (PWM)
        panelCentro.add(crearFilaPWM());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(18, 18, 24));
        wrapper.add(panelCentro, BorderLayout.NORTH);

        add(wrapper, BorderLayout.CENTER);
    }

    // --- NUEVO: Fila especial para la Bomba con entrada de texto ---
    private JPanel crearFilaBombaConTiempo(String titulo, Color colorAcento) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(new Color(30, 30, 42));
        fila.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 6, 0, 0, colorAcento),
            new EmptyBorder(10, 20, 10, 20)
        ));

        // Panel Izquierdo: Título y Configuración de Tiempo
        JPanel panelIzquierdo = new JPanel(new GridLayout(2, 1, 0, 5));
        panelIzquierdo.setOpaque(false);
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        
        // Controles de tiempo
        JPanel panelTiempo = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        panelTiempo.setOpaque(false);
        
        JLabel lblMinutos = new JLabel("Segundos a regar:");
        lblMinutos.setForeground(new Color(200, 200, 200));
        
        txtTiempoRiego = new JTextField("10", 4); // Por defecto 5, ancho de 4 columnas
        txtTiempoRiego.setHorizontalAlignment(JTextField.CENTER);
        txtTiempoRiego.setBackground(new Color(18, 18, 24));
        txtTiempoRiego.setForeground(Color.WHITE);
        txtTiempoRiego.setCaretColor(Color.WHITE);
        txtTiempoRiego.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 95)));
        
        JLabel lblAdvertencia = new JLabel("  Sugerido: como maximo1 minuto");
        lblAdvertencia.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblAdvertencia.setForeground(new Color(255, 204, 0)); // Amarillo advertencia
        
        panelTiempo.add(lblMinutos);
        panelTiempo.add(txtTiempoRiego);
        panelTiempo.add(lblAdvertencia);
        
        panelIzquierdo.add(lblTitulo);
        panelIzquierdo.add(panelTiempo);

        // Panel Derecho: Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setOpaque(false);
        
        JButton btnEncender = crearBotonUI("ENCENDER", colorAcento, Color.BLACK);
        JButton btnApagar = crearBotonUI("APAGAR", new Color(80, 80, 95), Color.WHITE);

        btnEncender.addActionListener(e -> {
            isBombaOn = true;
            isAuto = false;
            enviarComandoActual();
        });

        btnApagar.addActionListener(e -> {
            isBombaOn = false;
            isAuto = false;
            enviarComandoActual();
        });

        // Alineación vertical de los botones para que queden centrados respecto a las dos líneas de texto
        JPanel wrapperBotones = new JPanel(new GridBagLayout());
        wrapperBotones.setOpaque(false);
        panelBotones.add(btnEncender);
        panelBotones.add(btnApagar);
        wrapperBotones.add(panelBotones);

        fila.add(panelIzquierdo, BorderLayout.WEST);
        fila.add(wrapperBotones, BorderLayout.EAST);
        
        return fila;
    }
    // -------------------------------------------------------------

    private JPanel crearFilaModo(String titulo, String textoBtn1, String textoBtn2, Color color1, Color color2) {
        JPanel fila = crearEstructuraFila(titulo, color1);
        JPanel panelBotones = (JPanel) fila.getComponent(1);

        JButton btnAuto = crearBotonUI(textoBtn1, color1, Color.BLACK);
        JButton btnManual = crearBotonUI(textoBtn2, color2, Color.WHITE);

        btnAuto.addActionListener(e -> { isAuto = true; enviarComandoActual(); });
        btnManual.addActionListener(e -> { isAuto = false; enviarComandoActual(); });

        panelBotones.add(btnAuto);
        panelBotones.add(btnManual);
        return fila;
    }

    private JPanel crearFilaActuador(String titulo, String textoBtn1, String textoBtn2, Color color1, Color color2, String tipo) {
        JPanel fila = crearEstructuraFila(titulo, color1);
        JPanel panelBotones = (JPanel) fila.getComponent(1);

        JButton btnEncender = crearBotonUI(textoBtn1, color1, Color.BLACK);
        JButton btnApagar = crearBotonUI(textoBtn2, color2, Color.WHITE);

        btnEncender.addActionListener(e -> {
            if(tipo.equals("ventilador")) isVentiladorOn = true;
            isAuto = false;
            enviarComandoActual();
        });

        btnApagar.addActionListener(e -> {
            if(tipo.equals("ventilador")) isVentiladorOn = false;
            isAuto = false;
            enviarComandoActual();
        });

        panelBotones.add(btnEncender);
        panelBotones.add(btnApagar);
        return fila;
    }
    
    private JPanel crearFilaPWM() {
        JPanel panelPWM = new JPanel(new BorderLayout());
        panelPWM.setBackground(new Color(30, 30, 42));
        panelPWM.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 6, 0, 0, new Color(255, 234, 0)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblPwm = new JLabel("Lámpara de Crecimiento (PWM 0-255)");
        lblPwm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPwm.setForeground(Color.WHITE);
        
        JSlider slider = new JSlider(0, 255, 0);
        slider.setBackground(new Color(30, 30, 42));
        slider.setForeground(Color.WHITE);
        slider.setMajorTickSpacing(51);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                pwmLampara = slider.getValue();
                enviarComandoActual();
            }
        });
        
        panelPWM.add(lblPwm, BorderLayout.NORTH);
        panelPWM.add(slider, BorderLayout.CENTER);
        return panelPWM;
    }

    private JPanel crearEstructuraFila(String titulo, Color color1) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(new Color(30, 30, 42));
        fila.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 6, 0, 0, color1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelBotones.setOpaque(false);

        fila.add(lblTitulo, BorderLayout.WEST);
        fila.add(panelBotones, BorderLayout.EAST);
        return fila;
    }

    private JButton crearBotonUI(String texto, Color fondo, Color colorTexto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(fondo);
        btn.setForeground(colorTexto);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void enviarComandoActual() {
        if (gestorSerial == null) return;
        
        String comando;
        if (isAuto) {
            comando = "AUTO";
        } else {
            int vBomba = isBombaOn ? 1 : 0;
            int vVent = isVentiladorOn ? 1 : 0;
            
            // FILTRO DE SEGURIDAD PARA EL TIEMPO
            int minutos = 1; // Valor por defecto si hay error
            try {
                // Intentamos leer lo que el usuario escribió
                minutos = Integer.parseInt(txtTiempoRiego.getText().trim());
                if (minutos <= 0) minutos = 1; // No permitimos tiempos negativos o cero
            } catch (NumberFormatException ex) {
                // Si el usuario escribió letras, restauramos a 5 y mostramos en consola
                System.out.println("Error de entrada de tiempo. Usando valor seguro por defecto.");
                txtTiempoRiego.setText("5");
                minutos = 5;
            }

            // Enviamos el comando con el tiempo incluido (Ej: "t_bomba":5)
            comando = "MANUAL \"bomba\":" + vBomba + " \"t_bomba\":" + minutos + " \"vent\":" + vVent + " \"pwm\":" + pwmLampara;
        }
        
        gestorSerial.enviarComando(comando);
    }
}