package com.mycompany.proyecto_aliuta;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.EmptyBorder;
import org.knowm.xchart.*;
import org.knowm.xchart.style.XYStyler;

public class PanelGraficas extends JPanel {

    private JLabel lblUltimoValor;
    private XYChart chart;
    private XChartPanel<XYChart> chartPanel; 
    private String variableActual = "Temperatura";
    private String unidadActual = "°C";

    // Almacenamiento directo de datos (X = Tiempo, Y = Telemetría)
    private final List<Integer> datosX = new ArrayList<>();
    private final List<Double> datosY = new ArrayList<>();
    private int contadorTiempo = 0;

    public PanelGraficas() {
        // Inicializar vectores con 10 puntos en cero de manera limpia
        for (int i = 0; i < 10; i++) {
            datosX.add(contadorTiempo++);
            datosY.add(0.0);
        }

        // Configuración de la "hoja" física del panel
        setOpaque(true);
        setBackground(new Color(18, 18, 24));
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- ENCABEZADO ---
        JPanel panelNorte = new JPanel(new BorderLayout(0, 15));
        panelNorte.setOpaque(false);

        JLabel lblTitulo = new JLabel("ANÁLISIS DE TENDENCIAS EN VIVO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        // --- BOTONES DE FILTRO DIRECTO ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelFiltros.setOpaque(false);
        panelFiltros.add(crearBotonFiltro("Temperatura", "°C", new Color(255, 61, 0)));
        panelFiltros.add(crearBotonFiltro("Humedad", "%", new Color(0, 229, 255)));
        panelFiltros.add(crearBotonFiltro("Nivel de Agua", "%", new Color(0, 150, 255)));
        panelFiltros.add(crearBotonFiltro("Luz", " lx", new Color(255, 234, 0)));

        panelNorte.add(lblTitulo, BorderLayout.NORTH);
        panelNorte.add(panelFiltros, BorderLayout.SOUTH);

        // --- CREACIÓN E INYECCIÓN FIJA DE LA GRÁFICA ---
        chart = new XYChartBuilder()
                .title("")
                .xAxisTitle("Tiempo (s)")
                .yAxisTitle(variableActual + " (" + unidadActual + ")")
                .build();

        // Estilos Modo Oscuro SCADA estables y compatibles con todas las versiones
        XYStyler styler = chart.getStyler();
        styler.setChartBackgroundColor(new Color(30, 30, 42));
        styler.setPlotBackgroundColor(new Color(24, 24, 36));
        styler.setPlotGridLinesColor(new Color(50, 50, 65));
        
        // Desactivar visualmente el título para mantener la interfaz limpia sin romper el motor gráfico
        styler.setChartTitleVisible(false);
        
        // Compatibilidad universal de fuentes y colores
        styler.setChartFontColor(Color.WHITE);
        styler.setAxisTickLabelsColor(Color.LIGHT_GRAY);
        styler.setLegendVisible(false);

        // Inyectar serie inicial de control
        chart.addSeries("Telemetria", new ArrayList<>(datosX), new ArrayList<>(datosY));
        
        // El panel de XChart se instancia una SOLA VEZ y se expandirá en el CENTER
        chartPanel = new XChartPanel<>(chart);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 65), 1));
        
        // Definimos un tamaño de reserva preferido estable para el Layout
        chartPanel.setPreferredSize(new Dimension(750, 400));

        // --- PIE DE PÁGINA ---
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSur.setOpaque(false);
        lblUltimoValor = new JLabel("ÚLTIMO VALOR REGISTRADO: 0.0 " + unidadActual);
        lblUltimoValor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUltimoValor.setForeground(new Color(139, 141, 152));
        panelSur.add(lblUltimoValor);

        // Montar todo de forma limpia
        add(panelNorte, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER); 
        add(panelSur, BorderLayout.SOUTH);
    }

    private JButton crearBotonFiltro(String texto, String unidad, Color colorAcento) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(new Color(30, 30, 42));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 3, 0, colorAcento),
            new EmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            variableActual = texto;
            unidadActual = unidad;
            
            chart.setYAxisTitle(variableActual + " (" + unidadActual + ")");
            
            for (int i = 0; i < datosY.size(); i++) {
                datosY.set(i, 0.0);
            }
            
            chart.updateXYSeries("Telemetria", new ArrayList<>(datosX), new ArrayList<>(datosY), null);
            lblUltimoValor.setText("VARIABLE ACTUAL: " + texto.toUpperCase() + " (Esperando transmisión...)");
            
            chartPanel.revalidate();
            chartPanel.repaint();
        });

        return btn;
    }

    public void actualizarDatoReal(double nuevoDato) {
        datosY.remove(0);
        datosY.add(nuevoDato);
        
        datosX.remove(0);
        datosX.add(contadorTiempo++);

        chart.updateXYSeries("Telemetria", new ArrayList<>(datosX), new ArrayList<>(datosY), null);
        lblUltimoValor.setText(String.format("ÚLTIMO VALOR REGISTRADO (%s): %.1f %s", variableActual.toUpperCase(), nuevoDato, unidadActual));
        
        chartPanel.repaint();
    }

    public String getVariableActual() {
        return this.variableActual;
    }
}