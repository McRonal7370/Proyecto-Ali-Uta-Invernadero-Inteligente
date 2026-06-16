package com.mycompany.proyecto_aliuta;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.*;

public class SelectorPuerto extends JDialog {
    private JComboBox<String> comboPuertos;
    private String puertoSeleccionado = null;

    public SelectorPuerto(Frame parent) {
        super(parent, "Seleccionar Puerto Gateway", true);
        setLayout(new FlowLayout());
        comboPuertos = new JComboBox<>();
        for (SerialPort p : SerialPort.getCommPorts()) comboPuertos.addItem(p.getSystemPortName());
        
        JButton btnAceptar = new JButton("Conectar");
        btnAceptar.addActionListener(e -> {
            puertoSeleccionado = (String) comboPuertos.getSelectedItem();
            dispose();
        });
        add(new JLabel("Puerto COM:")); add(comboPuertos); add(btnAceptar);
        pack(); setLocationRelativeTo(parent);
    }
    public String getPuertoSeleccionado() { return puertoSeleccionado; }
}