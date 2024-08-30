import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDate;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DigitalLiteracy extends JPanel {

    private JTextField[] skillFields;
    private Connection conn;
    private JLineGraph lineGraph; // Reference to the JLineGraph instance

    public DigitalLiteracy(String studentName, LocalDate date, JLineGraph lineGraph) {
        this.lineGraph = lineGraph; // Use the passed in graph instance
        setLayout(new BorderLayout());

        // Initialize skills array and layout
        String[] skills = {
            "Skill1-1", "Skill1-2", "Skill1-3", "Skill1-4", "Skill1-5", "Skill1-6",
            "Skill2-1", "Skill2-2", "Skill2-3", "Skill2-4",
            "Skill3-1", "Skill3-2", "Skill3-3", "Skill3-4", "Skill3-5", "Skill3-6",
            "Skill3-7", "Skill3-8", "Skill3-9", "Skill3-10", "Skill3-11",
            "Skill4-1", "Skill4-2", "Skill4-3", "Skill4-4", "Skill4-5", "Skill4-6", "Skill4-7"
        };

        // Panel for data entry
        JPanel dataEntryPanel = new JPanel();
        dataEntryPanel.setLayout(new GridBagLayout());
        JScrollPane dataEntryScrollPane = new JScrollPane(dataEntryPanel);
        dataEntryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        dataEntryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        JLabel titleLabel = new JLabel("DigitalLiteracy Skills Progression", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        dataEntryPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.ipady = 20;
        dataEntryPanel.add(new JPanel(), gbc);

        int labelWidth = 100;
        int fieldWidth = 50;
        int gap = 25;

        skillFields = new JTextField[skills.length];
        for (int i = 0; i < skills.length; i++) {
            gbc.gridy = i + 2;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            JLabel skillLabel = new JLabel(skills[i] + ":");
            skillLabel.setPreferredSize(new Dimension(labelWidth, 30));
            dataEntryPanel.add(skillLabel, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(5, gap, 5, 5);
            JTextField skillField = new JTextField();
            skillField.setPreferredSize(new Dimension(fieldWidth, 30));
            skillFields[i] = skillField;
            dataEntryPanel.add(skillField, gbc);

            gbc.gridx = 2;
            gbc.insets = new Insets(5, 0, 5, 5);
            dataEntryPanel.add(new JPanel(), gbc);
        }

        gbc.gridy = skills.length + 3;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        dataEntryPanel.add(new JPanel(), gbc);

        gbc.gridy = skills.length + 4;
        gbc.weighty = 0.0;
        JButton submitDataButton = new JButton("Submit Data");
        submitDataButton.addActionListener((ActionEvent e) -> submitData(studentName, date));
        dataEntryPanel.add(submitDataButton, gbc);

        gbc.gridy = skills.length + 5;
        JButton refreshGraphButton = new JButton("Refresh Graph");
        refreshGraphButton.addActionListener((ActionEvent e) -> refreshGraph());
        dataEntryPanel.add(refreshGraphButton, gbc);

        add(dataEntryScrollPane, BorderLayout.CENTER);

        // Add existing graph reference
        add(lineGraph, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            dataEntryPanel.setPreferredSize(dataEntryPanel.getPreferredSize());
            revalidate();
        });

        initDatabase();
        refreshGraph();
    }

    private void initDatabase() {
        String userHome = System.getProperty("user.home");
        String databasePath = userHome + File.separator + "Documents" + File.separator + "StudentData";
        File directory = new File(databasePath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String dbFilePath = databasePath + File.separator + "student_data.db";

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
            Statement stmt = conn.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS digitalliteracy_student_data ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "studentName TEXT, "
                    + "date TEXT, "
                    + "skill1_1 INTEGER, "
                    + "skill1_2 INTEGER, "
                    + "skill1_3 INTEGER, "
                    + "skill1_4 INTEGER, "
                    + "skill1_5 INTEGER, "
                    + "skill1_6 INTEGER, "
                    + "skill2_1 INTEGER, "
                    + "skill2_2 INTEGER, "
                    + "skill2_3 INTEGER, "
                    + "skill2_4 INTEGER, "
                    + "skill3_1 INTEGER, "
                    + "skill3_2 INTEGER, "
                    + "skill3_3 INTEGER, "
                    + "skill3_4 INTEGER, "
                    + "skill3_5 INTEGER, "
                    + "skill3_6 INTEGER, "
                    + "skill3_7 INTEGER, "
                    + "skill3_8 INTEGER, "
                    + "skill3_9 INTEGER, "
                    + "skill3_10 INTEGER, "
                    + "skill3_11 INTEGER, "
                    + "skill4_1 INTEGER, "
                    + "skill4_2 INTEGER, "
                    + "skill4_3 INTEGER, "
                    + "skill4_4 INTEGER, "
                    + "skill4_5 INTEGER, "
                    + "skill4_6 INTEGER, "
                    + "skill4_7 INTEGER"
                    + ")";
            stmt.execute(createTableSQL);

            System.out.println("Database initialized and table created.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void submitData(String studentName, LocalDate date) {
        String insertSQL = "INSERT INTO digitalliteracy_student_data ("
                + "studentName, date, "
                + "skill1_1, skill1_2, skill1_3, skill1_4, skill1_5, skill1_6, "
                + "skill2_1, skill2_2, skill2_3, skill2_4, "
                + "skill3_1, skill3_2, skill3_3, skill3_4, skill3_5, skill3_6, skill3_7, skill3_8, skill3_9, skill3_10, skill3_11, "
                + "skill4_1, skill4_2, skill4_3, skill4_4, skill4_5, skill4_6, skill4_7"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, studentName);
            pstmt.setString(2, date.toString());

            for (int i = 0; i < skillFields.length; i++) {
                String text = skillFields[i].getText();
                pstmt.setInt(i + 3, text.isEmpty() ? 0 : Integer.parseInt(text));
            }

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Data submitted successfully. Rows affected: " + rowsAffected);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format in skill fields.");
        }
    }

private ResultSet createCustomResultSet(int id, String studentName, String date, int[] skills) {
    // You need to create an in-memory ResultSet or modify JLineGraph
    // For now, returning null
    return null;
}

    private void refreshGraph() {
        try {
            if (conn == null || conn.isClosed()) {
                System.out.println("Database connection is not established.");
                return;
            }

            Statement stmt = conn.createStatement();
            String selectAllSQL = "SELECT * FROM digitalliteracy_student_data ORDER BY id DESC LIMIT 5";
            System.out.println("Executing query: " + selectAllSQL);

            ResultSet rs = stmt.executeQuery(selectAllSQL);

            List<List<Integer>> allSkillValues = new ArrayList<>();

            while (rs.next()) {
                List<Integer> skillValues = new ArrayList<>();

                // Skill Group 1 (6 skills)
                for (int i = 1; i <= 6; i++) {
                    skillValues.add(rs.getInt("skill1_" + i));
                }

                // Skill Group 2 (4 skills)
                for (int i = 1; i <= 4; i++) {
                    skillValues.add(rs.getInt("skill2_" + i));
                }

                // Skill Group 3 (11 skills)
                for (int i = 1; i <= 11; i++) {
                    skillValues.add(rs.getInt("skill3_" + i));
                }

                // Skill Group 4 (7 skills)
                for (int i = 1; i <= 7; i++) {
                    skillValues.add(rs.getInt("skill4_" + i));
                }

                allSkillValues.add(skillValues);
            }

            if (!allSkillValues.isEmpty()) {
                lineGraph.updateWithData(allSkillValues);
                System.out.println("Graph updated with data: " + allSkillValues);
            } else {
                System.out.println("No data to plot.");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void printResultSet(ResultSet rs) throws SQLException {
        // Print column names
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            System.out.print(rsmd.getColumnName(i) + "\t");
        }
        System.out.println();

        // Print rows
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
        }
    }

}
