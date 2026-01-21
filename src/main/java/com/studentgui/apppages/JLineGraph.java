package com.studentgui.apppages;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
 * Reusable JFreeChart-based line chart component for visualizing student assessment progress.
 *
 * <p>This component is shared across all assessment pages (Braille, Abacus, iOS, ScreenReader, etc.)
 * to display time-series data showing skill progression over multiple sessions. It supports three
 * primary visualization modes:</p>
 *
 * <ul>
 *   <li><b>Single-chart mode:</b> {@link #updateWithData(java.util.List)} - Plots all skills on one
 *       chart with historical sessions in gray and the latest session highlighted in black</li>
 *   <li><b>Grouped mode (session indices):</b> {@link #updateWithGroupedData(java.util.List, String[])} -
 *       Creates multiple stacked charts, one per phase group (determined by part code prefix like "P1", "P2")</li>
 *   <li><b>Grouped mode (chronological dates):</b> {@link #updateWithGroupedDataByDate(java.util.List, java.util.List, String[], String[])} -
 *       Plots grouped data with actual dates on the X-axis for true time-series visualization</li>
 * </ul>
 *
 * <p><b>Visual Design and Rendering:</b></p>
 * <ul>
 *   <li><b>Background bands:</b> Colored horizontal bands indicate score ranges to aid interpretation:
 *     <ul>
 *       <li><span style="color:red;">Red band</span>: -0.25 to 0.5 (minimal/no proficiency)</li>
 *       <li><span style="color:orange;">Orange bands</span>: 0.5\u20131.5, 1.5\u20132.5 (emerging skills)</li>
 *       <li><span style="color:yellow;">Yellow band</span>: 2.5\u20133.5 (developing proficiency)</li>
 *       <li><span style="color:green;">Green band</span>: 3.5\u20134.5 (mastery/proficient)</li>
 *     </ul>
 *   </li>
 *   <li><b>Rendering jitter:</b> A configurable visual jitter of ±{@value #JITTER_AMPLITUDE} is applied
 *       to plotted points via {@link #addJitter(double)} to reveal overlapping data points. This is a
 *       display-only transformation and does not modify persisted values. Jitter can be:
 *     <ul>
 *       <li>Enabled/disabled via {@link #setJitterEnabled(boolean)}</li>
 *       <li>Made deterministic (for testing) via {@link #setJitterDeterministic(boolean)} and {@link #setJitterSeed(Long)}</li>
 *       <li>Configured via {@link com.studentgui.apphelpers.Settings} keys: "jitter.enabled", "jitter.deterministic", "jitter.seed"</li>
 *     </ul>
 *   </li>
 *   <li><b>Color palette:</b> Consistent color-blind friendly palette used for series rendering:
 *     <ul>
 *       <li>{@link #PALETTE_HEX}: Hex color strings for HTML legend generation (8 colors)</li>
 *       <li>{@link #PALETTE}: AWT Color objects for JFreeChart rendering (8 colors matching PALETTE_HEX)</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>Typical Workflow for Assessment Pages:</b></p>
 * <ol>
 *   <li>Page fetches recent sessions from database via {@link com.studentgui.apphelpers.Database#fetchLatestAssessmentResultsWithDates}</li>
 *   <li>Page calls {@link #updateWithGroupedDataByDate(java.util.List, java.util.List, String[], String[])} to populate chart</li>
 *   <li>On submit, page calls {@link #saveGroupedCharts(java.nio.file.Path, String, int, int)} to export PNG images</li>
 *   <li>Page generates Markdown/HTML reports linking to the exported plots</li>
 * </ol>
 *
 * <p><b>Export and Persistence:</b></p>
 * <ul>
 *   <li>{@link #saveGroupedCharts(java.nio.file.Path, String, int, int)} - Exports each phase group as a separate PNG file</li>
 *   <li>{@link #saveChart(java.nio.file.Path, int, int)} - Exports the single main chart (when not in grouped mode)</li>
 *   <li>Returns Map&lt;groupName, filePath&gt; for use in report generation</li>
 * </ul>
 *
 * <p><b>Accessibility:</b></p>
 * <ul>
 *   <li>ChartPanel accessible name set to "Skill progression chart"</li>
 *   <li>Tooltips enabled showing coordinate values on hover</li>
 *   <li>Keyboard navigation supported through JFreeChart's default ChartPanel behavior</li>
 * </ul>
 *
 * <p><b>Settings Integration:</b> Implements {@link com.studentgui.app.SettingsChangeListener} to respond
 * to jitter configuration changes at runtime without requiring application restart.</p>
 *
 * @see com.studentgui.apphelpers.Database#fetchLatestAssessmentResultsWithDates
 * @see com.studentgui.app.SettingsChangeListener
 * @see org.jfree.chart.JFreeChart
 * @see org.jfree.chart.ChartPanel
 */
public class JLineGraph extends JPanel implements com.studentgui.app.SettingsChangeListener {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JLineGraph.class);
    /** The dataset containing XY series for historical and latest sessions. */
    private final XYSeriesCollection lineDataset;
    /** The JFreeChart instance used to render the plot. */
    private final JFreeChart chart;
    /** Panel that embeds the chart and provides UI features. */
    private final ChartPanel chartPanel;
    /** When rendering grouped charts we place multiple ChartPanels in this container. */
    private javax.swing.JPanel multiChartContainer;
    /** Domain axis used to customise X-axis labels and range. */
    private final NumberAxis xAxis;
    /** Expected number of skill columns per session. */
    private static final int NUMBER_OF_SKILLS = 28; // Adjust as needed
    /** Jitter amplitude (plus/minus) applied to plotted data points. */
    private static final double JITTER_AMPLITUDE = 0.10d;

    /** Whether rendering jitter is currently enabled. Default: true. */
    private boolean jitterEnabled = true;
    /** When true, use a deterministic java.util.Random seeded RNG instead of ThreadLocalRandom. */
    private boolean jitterDeterministic = false;
    /** Optional seed used when deterministic jitter is enabled. */
    private Long jitterSeed = null;
    /** Cached Random instance when deterministic mode is enabled. */
    private Random deterministicRandom = null;

    /**
     * Add a small random jitter within +/- JITTER_AMPLITUDE to the provided value.
     * When jitter is disabled this returns the original value unchanged.
     */
    private double addJitter(final double v) {
        if (!jitterEnabled) {
            return v;
        }
        try {
            if (jitterDeterministic) {
                if (deterministicRandom == null) {
                    long seed = jitterSeed == null ? 0L : jitterSeed.longValue();
                    deterministicRandom = new Random(seed);
                }
                double r = deterministicRandom.nextDouble() * 2.0 - 1.0; // -1..1
                return v + (r * JITTER_AMPLITUDE);
            } else {
                return v + ThreadLocalRandom.current().nextDouble(-JITTER_AMPLITUDE, JITTER_AMPLITUDE);
            }
        } catch (Throwable t) {
            // In the unlikely event RNG is unavailable, fall back to no jitter
            return v;
        }
    }
    /** Public color palette (hex) for HTML legends and consistency across pages. */
    public static final String[] PALETTE_HEX = new String[] {
        "#1b9e77","#d95f02","#7570b3","#e7298a","#66a61e","#e6ab02","#a6761d","#666666"
    };
    /** Public color palette as AWT Color objects for chart rendering. */
    public static final java.awt.Color[] PALETTE = new java.awt.Color[] {
        new java.awt.Color(0x1b9e77),
        new java.awt.Color(0xd95f02),
        new java.awt.Color(0x7570b3),
        new java.awt.Color(0xe7298a),
        new java.awt.Color(0x66a61e),
        new java.awt.Color(0xe6ab02),
        new java.awt.Color(0xa6761d),
        new java.awt.Color(0x666666)
    };

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
        // Apply any persisted settings at creation time
        try {
            settingsChanged();
        } catch (Throwable t) {
            // ignore any issues reading settings at startup
        }
    }

    @Override
    /**
     * settingsChanged - TODO: describe this method
     */

    public void settingsChanged() {
        try {
            String je = com.studentgui.apphelpers.Settings.get("jitter.enabled", String.valueOf(this.jitterEnabled));
            setJitterEnabled("true".equalsIgnoreCase(je));
            String jd = com.studentgui.apphelpers.Settings.get("jitter.deterministic", String.valueOf(this.jitterDeterministic));
            setJitterDeterministic("true".equalsIgnoreCase(jd));
            String s = com.studentgui.apphelpers.Settings.get("jitter.seed", this.jitterSeed == null ? "" : String.valueOf(this.jitterSeed));
            if (s == null || s.trim().isEmpty()) {
                setJitterSeed(null);
            } else {
                try {
                    long v = Long.parseLong(s.trim());
                    setJitterSeed(Long.valueOf(v));
                } catch (NumberFormatException nfe) {
                    setJitterSeed(null);
                }
            }
            // reset cached RNG so seed/cfg takes effect
            this.deterministicRandom = null;
            if (chart != null) {
                chart.fireChartChanged();
            }
            if (chartPanel != null) {
                chartPanel.repaint();
            }
        } catch (Throwable t) {
            LOG.debug("Failed applying settings: {}", t.toString());
        }
    }

    /**
     * Add lightly-colored horizontal bands to the plot to indicate score
     * ranges.
     */
    private void addBackgroundBands(final XYPlot plot) {
        // Use the generic band painter to draw the requested bands across the
        // full X domain of the main chart.
        double left = 0.0;
        double right = NUMBER_OF_SKILLS + 1;
        addHorizontalBands(plot, left, right);
    }

    /**
     * Add horizontal background bands to the provided plot between left and right
     * X coordinates. Bands follow the requested ranges:
     * red = -0.25..0.5, orange = 0.5..1.5, orange = 1.5..2.5, yellow = 2.5..3.5,
     * green = 3.5..4.5
     */
    private void addHorizontalBands(final XYPlot plot, final double left, final double right) {
        try {
            java.awt.Color red = new java.awt.Color(255, 0, 0, 40);
            java.awt.Color orange = new java.awt.Color(255, 165, 0, 40);
            java.awt.Color orange2 = new java.awt.Color(255, 140, 0, 40);
            java.awt.Color yellow = new java.awt.Color(255, 255, 0, 40);
            java.awt.Color green = new java.awt.Color(0, 255, 0, 40);

            double[][] bands = new double[][]{
                { -0.25, 0.5 },
                {  0.5,  1.5 },
                {  1.5,  2.5 },
                {  2.5,  3.5 },
                {  3.5,  4.5 }
            };
            java.awt.Color[] colors = new java.awt.Color[] { red, orange, orange2, yellow, green };

            for (int i = 0; i < bands.length; i++) {
                double low = bands[i][0];
                double high = bands[i][1];
                double[] coords = new double[] { left, low, right, low, right, high, left, high };
                plot.addAnnotation(new XYPolygonAnnotation(coords, null, null, colors[i]));
            }
        } catch (Throwable t) {
            LOG.debug("Unable to add horizontal bands: {}", t.toString());
        }
    }

    /**
     * Enable or disable rendering jitter at runtime.
     * @param enabled true to enable jitter, false to draw raw values
     */
    public void setJitterEnabled(final boolean enabled) {
        this.jitterEnabled = enabled;
    }

    /**
     * Query whether rendering jitter is currently enabled.
     *
     * @return true when jitter is enabled, false otherwise
     */
    public boolean isJitterEnabled() {
        return this.jitterEnabled;
    }

    /**
     * Enable/disable deterministic (seeded) jitter.
     * When enabled, jitter will be generated from a java.util.Random seeded
     * with {@link #jitterSeed} (or 0 when seed is null).
     *
     * @param deterministic true to use a seeded RNG, false to use non-deterministic RNG
     */
    public void setJitterDeterministic(final boolean deterministic) {
        this.jitterDeterministic = deterministic;
        this.deterministicRandom = null; // reset instance so seed takes effect
    }

    /**
     * Query whether deterministic jitter is enabled.
     *
     * @return true when deterministic (seeded) jitter is enabled
     */
    public boolean isJitterDeterministic() {
        return this.jitterDeterministic;
    }

    /**
     * Set the seed used when deterministic jitter is enabled. Pass null to
     * clear the seed (will use 0 when a deterministic RNG is created).
     *
     * @param seed seed value or null to clear
     */
    public void setJitterSeed(final Long seed) {
        this.jitterSeed = seed;
        this.deterministicRandom = null;
    }

    /**
     * Return the currently configured jitter seed or null when unset.
     *
     * @return configured seed value or null when not set
     */
    public Long getJitterSeed() {
        return this.jitterSeed;
    }

    /**
     * Replace the current dataset with the provided list of skill value
     * series. Each inner list represents a single session and must contain
     * NUMBER_OF_SKILLS entries.
     *
     * @param allSkillValues list of sessions where each session is a list of
     *                       integer skill values (older sessions first)
     */
    public void updateWithData(final List<List<Integer>> allSkillValues) {
        LOG.debug("updateWithData called with {} rows", allSkillValues == null ? 0 : allSkillValues.size());
        if (allSkillValues == null || allSkillValues.isEmpty()) {
            return;
        }
        // Fallback to existing single-chart behavior
        lineDataset.removeAllSeries();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Add historical data series (each prior session as a separate series)
        for (int s = 0; s < allSkillValues.size() - 1; s++) {
            XYSeries hs = new XYSeries("S" + s);
            List<Integer> skillValues = allSkillValues.get(s);
            if (skillValues == null) {
                continue;
            }
            for (int j = 0; j < skillValues.size(); j++) {
                Integer v = skillValues.get(j);
                double y = (double) (v == null ? 0 : v);
                hs.add(j + 1, addJitter(y));
            }
            lineDataset.addSeries(hs);
            renderer.setSeriesPaint(s, Color.GRAY);
            renderer.setSeriesStroke(s, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(s, false);
        }

        // Latest session
        XYSeries latestSeries = new XYSeries("Latest");
        List<Integer> latestSkillValues = allSkillValues.get(allSkillValues.size() - 1);
        if (latestSkillValues != null) {
            for (int i = 0; i < latestSkillValues.size(); i++) {
                Integer v = latestSkillValues.get(i);
                double y = (double) (v == null ? 0 : v);
                latestSeries.add(i + 1, addJitter(y));
            }
        }
        lineDataset.addSeries(latestSeries);
        int latestIndex = lineDataset.getSeriesCount() - 1;
        renderer.setSeriesPaint(latestIndex, Color.BLACK);
        renderer.setSeriesStroke(latestIndex, new BasicStroke(3f));
        renderer.setSeriesShapesVisible(latestIndex, true);
        renderer.setSeriesShape(latestIndex, new java.awt.geom.Ellipse2D.Double(-6, -6, 12, 12));

        chart.getXYPlot().setDataset(lineDataset);
        chart.getXYPlot().setRenderer(renderer);
        // Ensure Y axis range and ticks are consistent across charts
        try {
            NumberAxis y = (NumberAxis) chart.getXYPlot().getRangeAxis();
            y.setRange(-0.25, 4.25);
            y.setTickUnit(new org.jfree.chart.axis.NumberTickUnit(1));
        } catch (ClassCastException ignored) {
            // if range axis isn't a NumberAxis, ignore
        }
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
    public void updateWithGroupedData(final List<List<Integer>> allSkillValues, final String[] partCodes) {
        LOG.debug("updateWithGroupedData called with rows={} partCodes={}", allSkillValues == null ? 0 : allSkillValues.size(), partCodes == null ? 0 : partCodes.length);
        // validate
        if (partCodes == null || partCodes.length == 0 || allSkillValues == null || allSkillValues.isEmpty()) {
            return;
        }

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
                    Integer vv = (colIndex < sessionRow.size() ? sessionRow.get(colIndex) : null);
                    double y = (double) (vv == null ? 0 : vv);
                    series.add(x, addJitter(y));
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
            // Ensure Y axis range and ticks show 0..3 grid with a small lower padding for x-axis visibility
            try {
                NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
                yAxis.setRange(-0.25, 4.25);
                yAxis.setTickUnit(new org.jfree.chart.axis.NumberTickUnit(1));
            } catch (ClassCastException cce) {
                LOG.debug("Range axis is not a NumberAxis: {}", cce.toString());
            }
            NumberAxis domain = (NumberAxis) plot.getDomainAxis();
            if (idxs.size() <= 1) {
                // single-point chart: give a small visual range around the point
                domain.setRange(0.5, 1.5);
            } else {
                domain.setRange(1, idxs.size());
            }

            ChartPanel cp = new ChartPanel(subchart);
            // Store the group id on the panel so callers can name files per-group
            cp.setName(grp);
            cp.setPreferredSize(new Dimension(800, Math.max(100, 40 * idxs.size())));
            cp.setMaximumSize(new Dimension(Integer.MAX_VALUE, cp.getPreferredSize().height));
            multiChartContainer.add(cp);
        }

        add(new javax.swing.JScrollPane(multiChartContainer), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Plot grouped data over time. Dates are used as the X axis (oldest first).
     * Each skill within a group is drawn as its own line (one series per skill)
     * with point markers and a color-blind friendly palette. Legend placed
     * in the upper-right corner.
     *
     * @param dates chronological list of session dates (oldest first)
     * @param rows list of session rows where each row is a list of integer scores
     * @param partCodes array of part codes aligned with the columns in each row
     */
    public void updateWithGroupedDataByDate(final java.util.List<java.time.LocalDate> dates, final java.util.List<java.util.List<Integer>> rows, final String[] partCodes) {
        // Backwards-compatible wrapper: use code strings as labels if caller didn't provide labels
        String[] labels = partCodes == null ? null : partCodes.clone();
        updateWithGroupedDataByDate(dates, rows, partCodes, labels);
    }

    /**
     * Plot grouped data over time with optional human-friendly labels.
     * Each provided {@code partCodes} entry maps to a column index inside
     * {@code rows} and (optionally) a friendly label supplied in
     * {@code partLabels}. The dates list must be ordered oldest-first and
     * must be parallel to the rows list.
     *
     * @param dates chronological list of session dates (oldest first)
     * @param rows list of session rows where each row is a list of integer scores
     * @param partCodes array of part codes aligned with the columns in each row
     * @param partLabels optional human friendly labels parallel to {@code partCodes}
     */
    public void updateWithGroupedDataByDate(final java.util.List<java.time.LocalDate> dates, final java.util.List<java.util.List<Integer>> rows, final String[] partCodes, final String[] partLabels) {
        LOG.debug("updateWithGroupedDataByDate called with dates={} rows={} parts={}", dates == null ? 0 : dates.size(), rows == null ? 0 : rows.size(), partCodes == null ? 0 : partCodes.length);
        if (dates == null || rows == null || partCodes == null) {
            return;
        }
        // Build groups preserving order
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

        // Color-blind friendly palette (ColorBrewer Set2-like)
        java.awt.Color[] palette = new java.awt.Color[] {
            new java.awt.Color(0x1b9e77), // green
            new java.awt.Color(0xd95f02), // orange
            new java.awt.Color(0x7570b3), // purple
            new java.awt.Color(0xe7298a), // pink
            new java.awt.Color(0x66a61e), // olive
            new java.awt.Color(0xe6ab02), // mustard
            new java.awt.Color(0xa6761d), // brown
            new java.awt.Color(0x666666)  // gray
        };

        for (var entry : groups.entrySet()) {
            String grp = entry.getKey();
            java.util.List<Integer> idxs = entry.getValue();
            org.jfree.data.time.TimeSeriesCollection dataset = new org.jfree.data.time.TimeSeriesCollection();

            // For each skill in the group, build a time series across dates
            for (int k = 0; k < idxs.size(); k++) {
                int colIndex = idxs.get(k);
                String code = partCodes[colIndex];
                String human = (partLabels != null && partLabels.length > colIndex && partLabels[colIndex] != null) ? partLabels[colIndex] : code;
                String seriesName = code + " - " + human; // legend shows code plus friendly label
                org.jfree.data.time.TimeSeries ts = new org.jfree.data.time.TimeSeries(seriesName);
                for (int r = 0; r < rows.size(); r++) {
                    java.time.LocalDate d = dates.get(r);
                    java.util.List<Integer> row = rows.get(r);
                    Integer vv = (colIndex < row.size()) ? row.get(colIndex) : null;
                    double val = (double) (vv == null ? 0 : vv);
                    org.jfree.data.time.Day day = new org.jfree.data.time.Day(java.util.Date.from(d.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
                    ts.addOrUpdate(day, addJitter(val));
                }
                dataset.addSeries(ts);
            }

            // Title: "Phase N Progression" when grp matches P<digit(s)>
            String title = (grp != null && grp.startsWith("P") && grp.length() > 1)
                    ? ("Phase " + grp.substring(1) + " Progression")
                    : (grp + " progression");

            JFreeChart subchart = ChartFactory.createTimeSeriesChart(
                    title,
                    "Date",
                    "Value",
                    dataset,
                    true,
                    true,
                    false
            );

            XYPlot plot = subchart.getXYPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            // Add colored horizontal bands behind the data using polygon annotations
            try {
                // Compute domain lower/upper bounds in millis for the current dataset if available
                long domainLower = Long.MIN_VALUE;
                long domainUpper = Long.MAX_VALUE;
                if (!dates.isEmpty()) {
                    java.time.ZoneId zid = java.time.ZoneId.systemDefault();
                    java.time.LocalDate first = dates.get(0);
                    java.time.LocalDate last = dates.get(dates.size() - 1).plusDays(4);
                    domainLower = java.util.Date.from(first.atStartOfDay(zid).toInstant()).getTime();
                    domainUpper = java.util.Date.from(last.atStartOfDay(zid).toInstant()).getTime();
                }
                double left = domainLower == Long.MIN_VALUE ? plot.getDomainAxis().getRange().getLowerBound() : domainLower;
                double right = domainUpper == Long.MAX_VALUE ? plot.getDomainAxis().getRange().getUpperBound() : domainUpper;
                // Use shared helper to draw bands in the domain coordinates (millis)
                addHorizontalBands(plot, left, right);
            } catch (Throwable t) {
                LOG.debug("Unable to add background bands as annotations: {}", t.toString());
            }
            org.jfree.chart.renderer.xy.XYLineAndShapeRenderer renderer = new org.jfree.chart.renderer.xy.XYLineAndShapeRenderer(true, true);
            // assign colors and markers
            for (int s = 0; s < dataset.getSeriesCount(); s++) {
                java.awt.Color c = palette[s % palette.length];
                renderer.setSeriesPaint(s, c);
                renderer.setSeriesStroke(s, new java.awt.BasicStroke(2.0f));
                renderer.setSeriesShapesVisible(s, true);
                renderer.setSeriesShape(s, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
            }
            plot.setRenderer(renderer);

            // Ensure Y axis range and ticks show 0..3 grid with a small lower padding for x-axis visibility
                try {
                    NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
                    yAxis.setRange(-0.25, 4.25);
                    yAxis.setTickUnit(new org.jfree.chart.axis.NumberTickUnit(1));
                } catch (ClassCastException cce) {
                    LOG.debug("Range axis is not a NumberAxis: {}", cce.toString());
                }

            // Ensure Y axis range and ticks show 0..3 grid with a small lower padding for x-axis visibility
            try {
                org.jfree.chart.axis.DateAxis dateAxis = (org.jfree.chart.axis.DateAxis) plot.getDomainAxis();
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
                dateAxis.setDateFormatOverride(fmt);
                // Use the provided dates list to determine bounds (oldest first)
                if (!dates.isEmpty()) {
                    java.time.ZoneId zid = java.time.ZoneId.systemDefault();
                    java.time.LocalDate firstDate = dates.get(0);
                    java.time.LocalDate lastDate = dates.get(dates.size() - 1);
                    // pad 4 days on the right to provide visual breathing room
                    java.time.LocalDate paddedUpper = lastDate.plusDays(4);
                    java.util.Date lower = java.util.Date.from(firstDate.atStartOfDay(zid).toInstant());
                    java.util.Date upper = java.util.Date.from(paddedUpper.atStartOfDay(zid).toInstant());
                    dateAxis.setRange(lower, upper);
                    // one-day tick units so each datapoint maps to a single label
                    dateAxis.setTickUnit(new org.jfree.chart.axis.DateTickUnit(org.jfree.chart.axis.DateTickUnitType.DAY, 1));
                }
            } catch (ClassCastException cce) {
                LOG.debug("Domain axis is not a DateAxis: {}", cce.toString());
            }

            // Place legend below the plot for clarity and allow it to show codes+labels
            if (subchart.getLegend() != null) {
                subchart.getLegend().setPosition(org.jfree.chart.ui.RectangleEdge.BOTTOM);
            }

            ChartPanel cp = new ChartPanel(subchart);
            cp.setName(grp);
            cp.setPreferredSize(new Dimension(1000, Math.max(180, 40 * idxs.size())));
            cp.setMaximumSize(new Dimension(Integer.MAX_VALUE, cp.getPreferredSize().height));
            multiChartContainer.add(cp);
        }

        add(new javax.swing.JScrollPane(multiChartContainer), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Save each grouped subchart as an individual PNG file. The method writes
     * files named {baseName}-{group}.png into the provided directory and
     * returns a map of group -> written path. Caller must ensure grouped data
     * has been rendered (updateWithGroupedData called) prior to invoking this.
     *
     * @param dir directory to write files into
     * @param baseName base filename (no extension) to prefix each file
     * @param width image width in pixels
     * @param heightPerGroup per-group image height in pixels
     * @return ordered map of group id to written file path
     * @throws java.io.IOException on I/O error
     */
    public java.util.Map<String, java.nio.file.Path> saveGroupedCharts(final java.nio.file.Path dir, final String baseName, final int width, final int heightPerGroup) throws java.io.IOException {
        java.util.Map<String, java.nio.file.Path> out = new java.util.LinkedHashMap<>();
        if (dir == null) {
            throw new java.io.IOException("output dir is null");
        }
        java.nio.file.Files.createDirectories(dir);
        if (multiChartContainer == null || multiChartContainer.getComponentCount() == 0) {
            return out;
        }
        for (int i = 0; i < multiChartContainer.getComponentCount(); i++) {
            java.awt.Component c = multiChartContainer.getComponent(i);
            String grp = c.getName() != null ? c.getName() : String.valueOf(i+1);
            int h = Math.max(100, heightPerGroup);
            c.setSize(width, h);
            c.doLayout();
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = img.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, width, h);
            c.paint(g);
            g.dispose();
            java.nio.file.Path file = dir.resolve(baseName + "-" + grp + ".png");
            try (java.io.OutputStream os = java.nio.file.Files.newOutputStream(file);
                 javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(os)) {
                boolean written = javax.imageio.ImageIO.write(img, "png", ios);
                if (!written) {
                    throw new java.io.IOException("No ImageWriter for png");
                }
            }
            out.put(grp, file);
        }
        return out;
    }

    /**
     * Show an empty grouped chart using the provided part codes. This will
     * render one row of zeros sized to the number of parts so the UI shows
     * grouped axes and placeholders even when no session data exists yet.
     *
     * @param partCodes array of part codes used to determine the number of columns
     */
    public void showEmptyGrouped(final String[] partCodes) {
        if (partCodes == null) {
            return;
        }
        List<Integer> zeros = new java.util.ArrayList<>(java.util.Collections.nCopies(partCodes.length, 0));
        List<List<Integer>> rows = new java.util.ArrayList<>();
        rows.add(zeros);
        updateWithGroupedData(rows, partCodes);
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
    public void saveChart(final java.nio.file.Path outputPath, final int width, final int height) throws java.io.IOException {
        if (outputPath == null) {
            throw new java.io.IOException("outputPath is null");
        }
        java.nio.file.Path parent = outputPath.getParent();
        if (parent == null) {
            parent = java.nio.file.Paths.get(".");
        }
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
                if (!written) {
                    throw new java.io.IOException("No ImageWriter available for format 'png'");
                }
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
            /**
             * valueToString - TODO: describe this method
             * @param value TODO: describe parameter
             * @return TODO: describe return value
             */

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
