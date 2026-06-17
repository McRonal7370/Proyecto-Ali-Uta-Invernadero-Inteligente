package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PanelInicio extends JPanel {

    public PanelInicio() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 24));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // --- ZONA SUPERIOR: LOGO ---
        JLabel tituloLogo = new JLabel();
        tituloLogo.setHorizontalAlignment(SwingConstants.CENTER);
        java.net.URL imgURL = getClass().getResource("/Imagenes/logo_central.png");
        if (imgURL != null) {
            tituloLogo.setIcon(new ImageIcon(escalarAltaCalidad(new ImageIcon(imgURL).getImage(), 435, 190)));
        }

        JLabel subtitulo = new JLabel("Agricultura e Iot ", SwingConstants.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitulo.setForeground(new Color(139, 141, 152));
        subtitulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        JPanel contenedorTitulo = new JPanel(new BorderLayout());
        contenedorTitulo.setOpaque(false);
        contenedorTitulo.add(tituloLogo, BorderLayout.CENTER);
        contenedorTitulo.add(subtitulo, BorderLayout.SOUTH);

        // --- ZONA CENTRAL: IMAGEN DE ARQUITECTURA ---
        JLabel lblArquitectura = new JLabel();
        lblArquitectura.setHorizontalAlignment(SwingConstants.CENTER);
        lblArquitectura.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 42), 4));

        // CARGA DE LA IMAGEN DE ARQUITECTURA
        java.net.URL archURL = getClass().getResource("/Imagenes/arquitectura.png");
        if (archURL != null) {
            lblArquitectura.setIcon(new ImageIcon(escalarAltaCalidad(new ImageIcon(archURL).getImage(), 700, 350)));
        } else {
            lblArquitectura.setText("Imagen no encontrada en /Imagenes/arquitectura.png");
            lblArquitectura.setForeground(Color.RED);
        }

        add(contenedorTitulo, BorderLayout.NORTH);
        add(lblArquitectura, BorderLayout.CENTER);
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