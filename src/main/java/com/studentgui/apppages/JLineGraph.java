package com.studentgui.apppages;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Lightweight line chart component used across pages to display recent
 * assessment sessions. Wraps a JFreeChart XY plot and exposes a simple
 * {@code updateWithData(List<List<Integer>>)} method expected by the pages.
 */
public class JLineGraph extends JPanel {
    private static final long serialVersionUID = 1L;
    /** The dataset containing XY series for historical and latest sessions. */
    private XYSeriesCollection lineDataset;
    /** The JFreeChart instance used to render the plot. */
    private JFreeChart chart;
    /** Panel that embeds the chart and provides UI features. */
    private ChartPanel chartPanel;
    /** Domain axis used to customise X-axis labels and range. */
    private NumberAxis xAxis;
    /** Expected number of skill columns per session. */
    private static final int NUMBER_OF_SKILLS = 28; // Adjust as needed

    /**
     * Create a new JLineGraph with default styling and an empty dataset.
     */
    public JLineGraph() {
        setLayout(new BorderLayout());
        lineDataset = new XYSeriesCollection();

        // Create a chart
        chart = ChartFactory.createXYLineChart(
                "Skill Progression",
                "Skills",
                "Value",
                lineDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customize the plot
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Set axis ranges
        xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(0, NUMBER_OF_SKILLS + 1);
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(-0.25, 4.25);

        // Create background bands
        addBackgroundBands(plot);

    chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(800, 600));
    chartPanel.getAccessibleContext().setAccessibleName("Skill progression chart");
    chartPanel.setToolTipText("Skill progression chart showing historical and latest values");
        add(chartPanel, BorderLayout.CENTER);

        // Set custom X-axis labels
        updateXAxisLabels();
    }

    /**
     * Add lightly-colored horizontal bands to the plot to indicate score
     * ranges.
     */
    private void addBackgroundBands(XYPlot plot) {
        // Define the colors for the bands
        Color red = new Color(255, 0, 0, 25);
        Color orange = new Color(255, 165, 0, 25);
        Color yellow = new Color(255, 255, 0, 25);
        Color green = new Color(0, 255, 0, 25);

        // Define the vertices for the polygons as interleaved coordinates
        double[] redCoords = {0, 0, NUMBER_OF_SKILLS + 1, 0, NUMBER_OF_SKILLS + 1, 1, 0, 1};
        double[] orangeCoords = {0, 1, NUMBER_OF_SKILLS + 1, 1, NUMBER_OF_SKILLS + 1, 2, 0, 2};
        double[] yellowCoords = {0, 2, NUMBER_OF_SKILLS + 1, 2, NUMBER_OF_SKILLS + 1, 3, 0, 3};
        double[] greenCoords = {0, 3, NUMBER_OF_SKILLS + 1, 3, NUMBER_OF_SKILLS + 1, 4, 0, 4};

        // Create and add the background annotations
        plot.addAnnotation(new XYPolygonAnnotation(redCoords, null, null, red));
        plot.addAnnotation(new XYPolygonAnnotation(orangeCoords, null, null, orange));
        plot.addAnnotation(new XYPolygonAnnotation(yellowCoords, null, null, yellow));
        plot.addAnnotation(new XYPolygonAnnotation(greenCoords, null, null, green));
    }

    /**
     * Replace the current dataset with the provided list of skill value
     * series. Each inner list represents a single session and must contain
     * NUMBER_OF_SKILLS entries.
     *
     * @param allSkillValues list of sessions where each session is a list of
     *                       integer skill values (older sessions first)
     */
    public void updateWithData(List<List<Integer>> allSkillValues) {
        lineDataset.removeAllSeries();
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    // Add historical data series
    XYSeries historicalSeries = new XYSeries("Historical");
        for (int i = 0; i < allSkillValues.size() - 1; i++) {
            List<Integer> skillValues = allSkillValues.get(i);
            for (int j = 0; j < skillValues.size(); j++) {
                historicalSeries.add(j + 1, skillValues.get(j));
            }
            renderer.setSeriesPaint(i, Color.GRAY);
            // use thinner stroke for display
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(i, false);
        }
        lineDataset.addSeries(historicalSeries);
        
        // Add the most recent data series in black
        XYSeries latestSeries = new XYSeries("Latest");
        List<Integer> latestSkillValues = allSkillValues.get(allSkillValues.size() - 1);
        for (int i = 0; i < latestSkillValues.size(); i++) {
            latestSeries.add(i + 1, latestSkillValues.get(i));
        }
        lineDataset.addSeries(latestSeries);
        
    renderer.setSeriesPaint(1, Color.BLACK);
    renderer.setSeriesStroke(1, new BasicStroke(3f));
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-10, -10, 20, 20));
        
        chart.getXYPlot().setRenderer(renderer);

        // Ensure the chart updates
        chart.fireChartChanged();
        chartPanel.repaint();
    }

    /**
     * Save the current chart to a PNG file. If the chart is empty this will
     * still export the rendered chart panel contents.
     *
     * @param outputPath path to write the PNG file to
     * @param width image width in pixels
     * @param height image height in pixels
     * @throws java.io.IOException if writing fails
     */
    public void saveChart(java.nio.file.Path outputPath, int width, int height) throws java.io.IOException {
        java.nio.file.Files.createDirectories(outputPath.getParent());
        java.awt.image.BufferedImage img = chart.createBufferedImage(width, height);
        javax.imageio.ImageIO.write(img, "png", outputPath.toFile());
    }

    private void updateXAxisLabels() {
        // Generate labels for the X-axis
        String[] skillLabels = new String[NUMBER_OF_SKILLS];
        int skillGroup = 1;
        int skillNumber = 1;
        for (int i = 0; i < NUMBER_OF_SKILLS; i++) {
            skillLabels[i] = "Skill" + skillGroup + "-" + skillNumber;
            skillNumber++;
            if ((skillGroup == 1 && skillNumber > 6) ||
                (skillGroup == 2 && skillNumber > 4) ||
                (skillGroup == 3 && skillNumber > 11) ||
                (skillGroup == 4 && skillNumber > 7)) {
                skillGroup++;
                skillNumber = 1;
            }
        }

        // Set the custom labels on the X-axis
        NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domain.setVerticalTickLabels(true);
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 8));
        domain.setTickUnit(new org.jfree.chart.axis.NumberTickUnit(1) {
            @Override
            public String valueToString(double value) {
                int index = (int) value - 1;
                if (index >= 0 && index < skillLabels.length) {
                    return skillLabels[index];
                }
                return "";
            }
        });
    }
}
