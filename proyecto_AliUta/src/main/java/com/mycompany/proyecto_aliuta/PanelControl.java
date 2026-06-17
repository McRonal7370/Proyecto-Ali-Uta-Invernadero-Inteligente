package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class PanelControl extends JPanel {
    private GestorSerial gestorSerial;
    private boolean isAuto = true;
    private boolean isBombaOn = false;
    private boolean isVentiladorOn = false;
    private int pwmLampara = 0;
    private JTextField txtTiempoRiego;

    public PanelControl(GestorSerial gestorSerial) {
        this.gestorSerial = gestorSerial;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(18, 18, 24));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel("PANEL DE CONTROL");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentro = new JPanel(new GridLayout(4, 1, 15, 15));
        panelCentro.setBackground(new Color(18, 18, 24));

        panelCentro.add(crearFilaModo("Modo de Control ", "AUTOMATICO", "MANUAL", new Color(0, 230, 118), new Color(255, 23, 68)));
        panelCentro.add(crearFilaBombaConTiempo("Bomba de Agua", new Color(0, 229, 255)));
        panelCentro.add(crearFilaActuador("Ventilador", "ENCENDER", "APAGAR", new Color(0, 229, 255), new Color(80, 80, 95), "ventilador"));
        panelCentro.add(crearFilaPWM());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(18, 18, 24));
        wrapper.add(panelCentro, BorderLayout.NORTH);
        add(wrapper, BorderLayout.CENTER);
    }

    private void enviarComandoActual() {
        if (gestorSerial == null) return;
        
        String comando;
        if (isAuto) {
            comando = "AUTO";
        } else {
            int vBomba = isBombaOn ? 1 : 0;
            int vVent = isVentiladorOn ? 1 : 0;
            
            int segundos = 10;
            try {
                segundos = Integer.parseInt(txtTiempoRiego.getText().trim());
                if (segundos <= 0) segundos = 1;
            } catch (NumberFormatException ex) {
                segundos = 10;
            }
            // FORMATO: M,modo(1=Manual),bomba,ventilador,pwm,tiempo
            comando = "M,1," + vBomba + "," + vVent + "," + pwmLampara + "," + segundos;
        }
        gestorSerial.enviarComando(comando);
    }

    private JPanel crearFilaBombaConTiempo(String titulo, Color colorAcento) {
        JPanel fila = crearEstructuraFila(titulo, colorAcento);
        JPanel panelBotones = (JPanel) fila.getComponent(1);
        
        JPanel panelTiempo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelTiempo.setOpaque(false);
        txtTiempoRiego = new JTextField("10", 4);
        panelTiempo.add(new JLabel("Segundos:") {{setForeground(Color.WHITE);}});
        panelTiempo.add(txtTiempoRiego);
        fila.add(panelTiempo, BorderLayout.SOUTH);

        JButton btnEncender = crearBotonUI("ENCENDER", colorAcento, Color.BLACK);
        JButton btnApagar = crearBotonUI("APAGAR", new Color(80, 80, 95), Color.WHITE);
        btnEncender.addActionListener(e -> { isBombaOn = true; isAuto = false; enviarComandoActual(); });
        btnApagar.addActionListener(e -> { isBombaOn = false; isAuto = false; enviarComandoActual(); });
        panelBotones.add(btnEncender);
        panelBotones.add(btnApagar);
        return fila;
    }

    private JPanel crearFilaModo(String titulo, String t1, String t2, Color c1, Color c2) {
        JPanel fila = crearEstructuraFila(titulo, c1);
        JPanel panelBotones = (JPanel) fila.getComponent(1);
        JButton btn1 = crearBotonUI(t1, c1, Color.BLACK);
        JButton btn2 = crearBotonUI(t2, c2, Color.WHITE);
        btn1.addActionListener(e -> { isAuto = true; enviarComandoActual(); });
        btn2.addActionListener(e -> { isAuto = false; enviarComandoActual(); });
        panelBotones.add(btn1); panelBotones.add(btn2);
        return fila;
    }

    private JPanel crearFilaActuador(String titulo, String t1, String t2, Color c1, Color c2, String tipo) {
        JPanel fila = crearEstructuraFila(titulo, c1);
        JPanel panelBotones = (JPanel) fila.getComponent(1);
        JButton btn1 = crearBotonUI(t1, c1, Color.BLACK);
        JButton btn2 = crearBotonUI(t2, c2, Color.WHITE);
        btn1.addActionListener(e -> { if(tipo.equals("ventilador")) isVentiladorOn = true; isAuto = false; enviarComandoActual(); });
        btn2.addActionListener(e -> { if(tipo.equals("ventilador")) isVentiladorOn = false; isAuto = false; enviarComandoActual(); });
        panelBotones.add(btn1); panelBotones.add(btn2);
        return fila;
    }

    private JPanel crearFilaPWM() {
        JPanel panel = crearEstructuraFila("Lámpara (PWM)", new Color(255, 234, 0));
        JSlider slider = new JSlider(0, 255, 0);
        slider.setOpaque(false);
        slider.addChangeListener(e -> { if(!slider.getValueIsAdjusting()) { pwmLampara = slider.getValue(); enviarComandoActual(); }});
        panel.add(slider, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearEstructuraFila(String titulo, Color color) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(new Color(30, 30, 42));
        fila.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 6, 0, 0, color), new EmptyBorder(10, 20, 10, 20)));
        JLabel lbl = new JLabel(titulo);
        lbl.setForeground(Color.WHITE);
        fila.add(lbl, BorderLayout.WEST);
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        b.setOpaque(false);
        fila.add(b, BorderLayout.EAST);
        return fila;
    }

    private JButton crearBotonUI(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto);
        btn.setBackground(fondo);
        btn.setForeground(textoColor);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}