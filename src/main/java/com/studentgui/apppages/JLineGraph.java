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
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JLineGraph.class);
    /** The dataset containing XY series for historical and latest sessions. */
    private XYSeriesCollection lineDataset;
    /** The JFreeChart instance used to render the plot. */
    private JFreeChart chart;
    /** Panel that embeds the chart and provides UI features. */
    private ChartPanel chartPanel;
    /** When rendering grouped charts we place multiple ChartPanels in this container. */
    private javax.swing.JPanel multiChartContainer;
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
    multiChartContainer = null;

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
        LOG.debug("updateWithData called with {} rows", allSkillValues == null ? 0 : allSkillValues.size());
        // Fallback to existing single-chart behavior
        lineDataset.removeAllSeries();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Add historical data series (each prior session as a separate series)
        for (int s = 0; s < allSkillValues.size() - 1; s++) {
            XYSeries hs = new XYSeries("S" + s);
            List<Integer> skillValues = allSkillValues.get(s);
            for (int j = 0; j < skillValues.size(); j++) hs.add(j + 1, skillValues.get(j));
            lineDataset.addSeries(hs);
            renderer.setSeriesPaint(s, Color.GRAY);
            renderer.setSeriesStroke(s, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(s, false);
        }

        // Latest session
        XYSeries latestSeries = new XYSeries("Latest");
        List<Integer> latestSkillValues = allSkillValues.get(allSkillValues.size() - 1);
        for (int i = 0; i < latestSkillValues.size(); i++) latestSeries.add(i + 1, latestSkillValues.get(i));
        lineDataset.addSeries(latestSeries);
        int latestIndex = lineDataset.getSeriesCount() - 1;
        renderer.setSeriesPaint(latestIndex, Color.BLACK);
        renderer.setSeriesStroke(latestIndex, new BasicStroke(3f));
        renderer.setSeriesShapesVisible(latestIndex, true);
        renderer.setSeriesShape(latestIndex, new java.awt.geom.Ellipse2D.Double(-6, -6, 12, 12));

        chart.getXYPlot().setDataset(lineDataset);
        chart.getXYPlot().setRenderer(renderer);
        chart.fireChartChanged();
        chartPanel.repaint();
    }

    /**
     * Update the component with grouped plots. Each group is determined by the
     * prefix of the part code (e.g. 'P1' from 'P1_1'). For each group we render
     * a separate small chart stacked vertically.
     *
     * @param allSkillValues list of sessions (older first) where each session is a list of integer skill values
     * @param partCodes array of part codes aligned with columns in each session row
     */
    public void updateWithGroupedData(List<List<Integer>> allSkillValues, String[] partCodes) {
        LOG.debug("updateWithGroupedData called with rows={} partCodes={}", allSkillValues == null ? 0 : allSkillValues.size(), partCodes == null ? 0 : partCodes.length);
        // validate
        if (partCodes == null || partCodes.length == 0 || allSkillValues == null || allSkillValues.isEmpty()) return;

        // Build group -> indexes map preserving order of first occurrence
        java.util.LinkedHashMap<String, java.util.List<Integer>> groups = new java.util.LinkedHashMap<>();
        for (int i = 0; i < partCodes.length; i++) {
            String code = partCodes[i];
            String grp = code != null && code.contains("_") ? code.split("_")[0] : code;
            groups.computeIfAbsent(grp, k -> new java.util.ArrayList<>()).add(i);
        }

        // Remove any single chart mode UI
        removeAll();
        multiChartContainer = new javax.swing.JPanel();
        multiChartContainer.setLayout(new javax.swing.BoxLayout(multiChartContainer, javax.swing.BoxLayout.Y_AXIS));

        // For each group create a small chart
        for (var entry : groups.entrySet()) {
            String grp = entry.getKey();
            java.util.List<Integer> idxs = entry.getValue();
            XYSeriesCollection dataset = new XYSeriesCollection();
            // historical sessions: create one series per prior session
            int sessions = allSkillValues.size();
            for (int s = 0; s < sessions; s++) {
                XYSeries series = new XYSeries(s == sessions - 1 ? "Latest" : "S" + s);
                List<Integer> sessionRow = allSkillValues.get(s);
                for (int k = 0; k < idxs.size(); k++) {
                    int colIndex = idxs.get(k);
                    int x = k + 1;
                    int y = (colIndex < sessionRow.size() ? sessionRow.get(colIndex) : 0);
                    series.add(x, y);
                }
                dataset.addSeries(series);
            }

            JFreeChart subchart = ChartFactory.createXYLineChart(
                    grp + " - " + (idxs.size()) + " items",
                    "Skill",
                    "Value",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false,
                    true,
                    false
            );
            XYPlot plot = subchart.getXYPlot();
            plot.setBackgroundPaint(Color.WHITE);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            for (int s = 0; s < dataset.getSeriesCount(); s++) {
                if (s == dataset.getSeriesCount() - 1) {
                    renderer.setSeriesPaint(s, Color.BLACK);
                    renderer.setSeriesStroke(s, new BasicStroke(2.5f));
                    renderer.setSeriesShapesVisible(s, true);
                    renderer.setSeriesShape(s, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
                } else {
                    renderer.setSeriesPaint(s, Color.GRAY);
                    renderer.setSeriesStroke(s, new BasicStroke(1.5f));
                    renderer.setSeriesShapesVisible(s, false);
                }
            }
            plot.setRenderer(renderer);
            NumberAxis domain = (NumberAxis) plot.getDomainAxis();
            domain.setRange(1, Math.max(1, idxs.size()));

            ChartPanel cp = new ChartPanel(subchart);
            cp.setPreferredSize(new Dimension(800, Math.max(100, 40 * idxs.size())));
            cp.setMaximumSize(new Dimension(Integer.MAX_VALUE, cp.getPreferredSize().height));
            multiChartContainer.add(cp);
        }

        add(new javax.swing.JScrollPane(multiChartContainer), BorderLayout.CENTER);
        revalidate();
        repaint();
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
        if (outputPath == null) throw new java.io.IOException("outputPath is null");
        java.nio.file.Path parent = outputPath.getParent();
        if (parent == null) parent = java.nio.file.Paths.get(".");
        // Ensure parent directory exists
        java.nio.file.Files.createDirectories(parent);
        java.awt.image.BufferedImage img = null;
        // If we are in grouped-chart mode, render the multiChartContainer component
        if (multiChartContainer != null && multiChartContainer.getComponentCount() > 0) {
            // Ensure layout sizes are applied
            multiChartContainer.setSize(width, height);
            multiChartContainer.doLayout();
            img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            // paint background white to match chart look
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, width, height);
            multiChartContainer.paint(g);
            g.dispose();
        } else if (chart != null) {
            img = chart.createBufferedImage(width, height);
        } else {
            throw new java.io.IOException("No chart available to render");
        }

        try {
            // Use an explicit OutputStream -> ImageOutputStream to avoid platform-specific ImageIO issues
            try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(outputPath);
                 javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(os)) {
                boolean written = javax.imageio.ImageIO.write(img, "png", ios);
                if (!written) throw new java.io.IOException("No ImageWriter available for format 'png'");
            }
        } catch (java.io.IOException ioe) {
            String diag = String.format("Failed saving chart to %s (parentExists=%b, parentWritable=%b, parentIsDir=%b)",
                    outputPath.toString(), java.nio.file.Files.exists(parent), java.nio.file.Files.isWritable(parent), java.nio.file.Files.isDirectory(parent));
            throw new java.io.IOException(diag, ioe);
        }
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
