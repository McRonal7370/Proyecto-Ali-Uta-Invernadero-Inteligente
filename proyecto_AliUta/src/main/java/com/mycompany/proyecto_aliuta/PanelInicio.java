package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PanelInicio extends JPanel {

    public PanelInicio() {
        // Configuración principal del panel
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 24));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // =========================================================
        // 1. ZONA SUPERIOR: LOGO DE ALIuTA
        // =========================================================
        JLabel tituloLogo = new JLabel();
        tituloLogo.setHorizontalAlignment(SwingConstants.CENTER);
        
        java.net.URL imgURL = getClass().getResource("/Imagenes/logo_central.png");
        
        if (imgURL != null) {
            ImageIcon iconoPrincipal = new ImageIcon(imgURL);
            // Usamos medidas fijas (400x160) en lugar de usar -1
            Image imgPrincipal = escalarAltaCalidad(iconoPrincipal.getImage(), 435, 190);
            tituloLogo.setIcon(new ImageIcon(imgPrincipal));
        }else {
            System.err.println("¡ERROR! No se pudo encontrar: /Imagenes/logo_central.png");
        } 

        // Subtítulo
        JLabel subtitulo = new JLabel("Arquitectura de Red y Telemetría IoT", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitulo.setForeground(new Color(139, 141, 152));
        subtitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        // Agrupamos el Logo y el Subtítulo arriba (esto faltaba en tu código)
        JPanel contenedorTitulo = new JPanel(new BorderLayout());
        contenedorTitulo.setOpaque(false);
        contenedorTitulo.add(tituloLogo, BorderLayout.CENTER);
        contenedorTitulo.add(subtitulo, BorderLayout.SOUTH);

        // =========================================================
        // 2. ZONA CENTRAL: DIAGRAMA DE ARQUITECTURA
        // =========================================================
        JLabel placeholderArquitectura = new JLabel("[ AQUÍ IRÁ TU IMAGEN DE ARQUITECTURA ]", SwingConstants.CENTER);
        placeholderArquitectura.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        placeholderArquitectura.setForeground(new Color(139, 141, 152));
        placeholderArquitectura.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 42), 4));

        // Añadir todo al panel principal
        add(contenedorTitulo, BorderLayout.NORTH);
        add(placeholderArquitectura, BorderLayout.CENTER);
    }

    private Image escalarAltaCalidad(Image imgOriginal, int ancho, int alto) {
        BufferedImage imagenRedimensionada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagenRedimensionada.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(imgOriginal, 0, 0, ancho, alto, null);
        g2d.dispose();
        
        return imagenRedimensionada;
    }
}