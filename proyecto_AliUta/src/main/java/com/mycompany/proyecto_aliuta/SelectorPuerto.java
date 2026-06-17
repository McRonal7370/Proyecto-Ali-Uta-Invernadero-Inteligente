package com.mycompany.proyecto_aliuta;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.*;

public class SelectorPuerto extends JDialog {
    private JComboBox<String> comboPuertos;
    private String puertoSeleccionado = null;

    public SelectorPuerto(Frame parent) {
        super(parent, "Conexión Gateway", true);
        
        // Configuración estética para coincidir con tu app
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 42));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new GridLayout(3, 1, 10, 10));

        JLabel lbl = new JLabel("Seleccione el puerto COM del Gateway:");
        lbl.setForeground(Color.WHITE);
        
        comboPuertos = new JComboBox<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) {
            comboPuertos.addItem("Sin puertos");
        } else {
            for (SerialPort p : ports) comboPuertos.addItem(p.getSystemPortName());
        }
        
        JButton btnAceptar = new JButton("Conectar");
        btnAceptar.setBackground(new Color(0, 229, 255));
        btnAceptar.setFocusPainted(false);
        btnAceptar.addActionListener(e -> {
            puertoSeleccionado = (String) comboPuertos.getSelectedItem();
            dispose();
        });

        panel.add(lbl);
        panel.add(comboPuertos);
        panel.add(btnAceptar);
        
        add(panel);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    public String getPuertoSeleccionado() { return puertoSeleccionado; }
}