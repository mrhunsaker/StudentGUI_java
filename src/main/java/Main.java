import com.formdev.flatlaf.intellijthemes.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class Main extends JFrame {

    private static final Map<String, Class<? extends LookAndFeel>> INTELLIJ_THEMES = new TreeMap<>();

    static {
        // Initialize IntelliJ themes
        INTELLIJ_THEMES.put("Arc", FlatArcIJTheme.class);
        INTELLIJ_THEMES.put("Arc Orange", FlatArcOrangeIJTheme.class);
        INTELLIJ_THEMES.put("Carbon", FlatCarbonIJTheme.class);
        INTELLIJ_THEMES.put("Cobalt 2", FlatCobalt2IJTheme.class);
        INTELLIJ_THEMES.put("Cyan Light", FlatCyanLightIJTheme.class);
        INTELLIJ_THEMES.put("Dark Purple", FlatDarkPurpleIJTheme.class);
        INTELLIJ_THEMES.put("Dracula", FlatDraculaIJTheme.class);
        INTELLIJ_THEMES.put("Gray", FlatGrayIJTheme.class);
        INTELLIJ_THEMES.put("Gruvbox Dark Hard", FlatGruvboxDarkHardIJTheme.class);
        INTELLIJ_THEMES.put("Hiberbee Dark", FlatHiberbeeDarkIJTheme.class);
        INTELLIJ_THEMES.put("High Contrast", FlatHighContrastIJTheme.class);
        INTELLIJ_THEMES.put("Light Flat", FlatLightFlatIJTheme.class);
        INTELLIJ_THEMES.put("Material Design Dark", FlatMaterialDesignDarkIJTheme.class);
        INTELLIJ_THEMES.put("Monocai", FlatMonocaiIJTheme.class);
        INTELLIJ_THEMES.put("Nord", FlatNordIJTheme.class);
        INTELLIJ_THEMES.put("One Dark", FlatOneDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Dark", FlatSolarizedDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Light", FlatSolarizedLightIJTheme.class);
        INTELLIJ_THEMES.put("Spacegray", FlatSpacegrayIJTheme.class);
        INTELLIJ_THEMES.put("Vuesion", FlatVuesionIJTheme.class);
    }

    private JTextField dateField;
    private JComboBox<String> learningTypeComboBox, studentField;
    private JPanel bottomPanel;
    private JPanel contentPanel;
    private JLineGraph lineGraph; // Initialize the graph once and reuse
    
    public Main() {
        super("Main");

        // Set the window size and position
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthPercentage = 0.67; // 67% of screen width
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int appWidth = (int) (screenSize.width * widthPercentage);
        int appHeight = screenSize.height;
        setTitle("Vision Skills Student Progressions");
        setSize(appWidth, appHeight);
        setLocation((screenWidth - appWidth) / 2, (screenHeight - appHeight) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create MenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu themeMenu = new JMenu("Themes");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(this, String.format(
                """
                Accessible Document Student Progress %s
                \u00a9 2024 Michael Ryan Hunsaker, M.Ed., Ph.D.
                All rights reserved.
                """,
                "1.0"
            ), "About", JOptionPane.INFORMATION_MESSAGE);
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        // Add theme options to the theme menu
        for (String themeName : INTELLIJ_THEMES.keySet()) {
            JMenuItem item = new JMenuItem(themeName);
            item.addActionListener(e -> setIntelliJTheme(themeName));
            themeMenu.add(item);
        }
        fileMenu.add(aboutMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar);

        // Top panel with GridBagLayout
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for flexible positioning
        add(topPanel, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around each component
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        // Student field
        JLabel studentLabel = new JLabel("Student:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(studentLabel, gbc);
        studentField = new JComboBox<>(new String[]{
            // Add student options here
            "AaAa", "AlPu", "AlRo", "AmRi", "AmOl", "AsNe", "AvWi", "BaAl", "BeWi", "BoUt",
            "BrTi", "CaDa", "CaHe", "CeNe", "ChTr", "ChCh", "ChGr", "ClPe", "CoBl",
            "CoCo", "CoHa", "CoBu", "CoHa", "CrPe", "CrAn", "DaCa", "DyPe", "ElLe",
            "ElWh", "ElSt", "EmTh", "EmTo", "EvCo", "FrAn", "FrLe", "GeBr", "GrDa",
            "GrCh", "HaGa", "HaHa", "HeUt", "HiWh", "HuHa", "HuTr", "InJo", "JaKa",
            "JaPe", "JaAb", "JaSm", "JeLe", "JuMa", "JuBa", "KaSt", "KaBr", "KaVi",
            "KaWa", "KeJo", "KeBy", "KiCh", "KiEl", "KiAg", "KiMi", "LaZa", "LaUl",
            "LaGr", "LaLe", "LaAr", "LiVa", "LiHo", "LuKi", "LuMo", "LyPe", "MaMc",
            "MaHa", "MaWi", "MaMa", "MaBl", "MaHe", "MaHe", "MeSc", "MiCo", "MiWe",
            "MiBe", "MoSt", "NaBu", "OlPa", "OlEv", "PaSa", "PeLa", "PrPe", "RaSc",
            "RaBa", "RoSo", "RyWe", "SaWi", "SaHi", "ScUt", "TaTi", "TaTr", "ThLl",
            "TjGu", "TrWe", "TrHa", "TrKe", "TyAs", "TyGr", "WeUt", "WeHe", "WiHa",
            "YaVa", "ZoFe",
        });
        gbc.gridx = 1;
        topPanel.add(studentField, gbc);

        // Date field
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(dateLabel, gbc);
        dateField = new JTextField(LocalDate.now().toString());
        gbc.gridx = 1;
        topPanel.add(dateField, gbc);

        // Learning type
        JLabel learningTypeLabel = new JLabel("Learning type:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(learningTypeLabel, gbc);
        String[] learningTypes = {
            "Screenreader", "Abacus", "Braille", "BrailleNote", "BrailleSense", "CVI",
            "DigitalLiteracy", "IOS", "Keyboarding",
        };
        learningTypeComboBox = new JComboBox<>(learningTypes);
        gbc.gridx = 1;
        topPanel.add(learningTypeComboBox, gbc);

        // Submit button
        JButton submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(300, 40)); // Full width button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(submitButton, gbc);

        // Initialize contentPanel and bottomPanel
        contentPanel = new JPanel(new CardLayout()); // Use CardLayout for contentPanel
        bottomPanel = new JPanel(new BorderLayout());
        lineGraph = new JLineGraph(); // Create or obtain your JLineGraph instance // Initialize the graph once

        // Add contentPanel and bottomPanel to the frame
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Add the line graph to the bottomPanel
        bottomPanel.add(lineGraph, BorderLayout.CENTER);

        // Add action listener for submit button
submitButton.addActionListener((ActionEvent event) -> {
    String studentName = studentField.getSelectedItem().toString();
    LocalDate date = LocalDate.parse(
            dateField.getText(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );
    String learningType = (String) learningTypeComboBox.getSelectedItem();

    contentPanel.removeAll();

    JPanel newPanel = null;
    switch (learningType) {
        case "Screenreader":
            newPanel = new ScreenReader(studentName, date, lineGraph);
            break;
                case "Abacus":
                    newPanel = new Abacus(studentName, date, lineGraph);
                    break;
                case "Braille":
                    newPanel = new Braille(studentName, date, lineGraph);
                    break;
                case "BrailleNote":
                    newPanel = new BrailleNote(studentName, date, lineGraph);
                    break;
                case "BrailleSense":
                    newPanel = new BrailleSense(studentName, date, lineGraph);
                    break;
                case "CVI":
                    newPanel = new CVI(studentName, date, lineGraph);
                    break;
                case "DigitalLiteracy":
                    newPanel = new DigitalLiteracy(studentName, date, lineGraph);
                    break;
                case "IOS":
                    newPanel = new IOS(studentName, date, lineGraph);
                    break;
                case "Keyboarding":
                    newPanel = new Keyboarding(studentName, date, lineGraph);
                    break;
            }

            if (newPanel != null) {
        // Ensure the classes have updateGraph or similar methods if you need them
        // Example without method calls to avoid errors
        // Add the new panel to contentPanel and show it
        contentPanel.add(newPanel, "content");
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "content");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
});
    }

    private void setIntelliJTheme(String themeName) {
        try {
            UIManager.setLookAndFeel(INTELLIJ_THEMES.get(themeName).newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

}
