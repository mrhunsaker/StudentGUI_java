package com.studentgui.apppages;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contact log page for storing freeform contact notes for a student.
 * <p>
 * Provides a multi-line text area for notes and persists them to the
 * normalized database as session notes using the {@code Database} helper.
 * </p>
 */
public class ContactLog extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(ContactLog.class);
    /** Text area where the user enters contact notes for the selected student. */
    private final JTextArea notesArea;
    // additional contact fields
    private final JTextField guardianField;
    
    private final JTextField phoneField;
    private final JTextField emailField;
    private final JComboBox<String> contactMethodCombo;
    private final JTextField contactResponseField;
    private final JTextField contactGeneralField;
    private final JTextField contactSpecificField;

    /** Selected student display name associated with this page instance (may be null). */
    private final String studentNameParam;

    /** Session date to associate with saved notes from this page. */
    private final LocalDate dateParam;

    /**
     * Construct a ContactLog page for the provided student and date.
     *
     * @param studentName selected student display name (may be null)
     * @param date session date to associate with saved notes
     * @param graph shared graph component shown under the editor
     */
    public ContactLog(String studentName, LocalDate date, JLineGraph graph) {
        this.studentNameParam = studentName;
        this.dateParam = date;
        setLayout(new BorderLayout());

    JPanel p = new JPanel(new GridBagLayout());
    JPanel view = new JPanel(new BorderLayout());
    view.add(p, BorderLayout.NORTH);
    view.setBorder(javax.swing.BorderFactory.createEmptyBorder(20,20,20,20));
    JScrollPane scroll = new JScrollPane(view);
    scroll.getAccessibleContext().setAccessibleName("Contact Log data entry scroll pane");
    GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(2,2,2,2); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.NORTHWEST;
    JLabel title = new JLabel("Contact Log"); title.setFont(title.getFont().deriveFont(Font.BOLD,16)); title.setHorizontalAlignment(JLabel.LEFT); gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; p.add(title, gbc);

    // Structured contact fields (placed above notes)
    int row = 1;
    gbc.gridwidth = 1;
    int globalLabel = com.studentgui.uicomp.PhaseScoreField.getGlobalLabelWidth();
    gbc.gridx = 0; gbc.gridy = row; JLabel guardianLabel = new JLabel("Guardian Name:"); guardianLabel.setPreferredSize(new java.awt.Dimension(globalLabel, guardianLabel.getPreferredSize().height)); p.add(guardianLabel, gbc);
    guardianField = new JTextField(24); guardianField.setName("contactlog_guardian"); gbc.gridx = 1; p.add(guardianField, gbc);
    row++;
    gbc.gridx = 0; gbc.gridy = row; JLabel methodLabel = new JLabel("Contact Method:"); methodLabel.setPreferredSize(new java.awt.Dimension(globalLabel, methodLabel.getPreferredSize().height)); p.add(methodLabel, gbc);
    contactMethodCombo = new JComboBox<>(new String[]{"Phone","Email","In Person","Other"}); contactMethodCombo.setName("contactlog_method"); gbc.gridx = 1; p.add(contactMethodCombo, gbc);
    row++;
    gbc.gridx = 0; gbc.gridy = row; JLabel phoneLabel = new JLabel("Phone Number:"); phoneLabel.setPreferredSize(new java.awt.Dimension(globalLabel, phoneLabel.getPreferredSize().height)); p.add(phoneLabel, gbc);
    phoneField = new JTextField(18); phoneField.setName("contactlog_phone"); gbc.gridx = 1; p.add(phoneField, gbc);
    row++;
    gbc.gridx = 0; gbc.gridy = row; JLabel emailLabel = new JLabel("Email Address:"); emailLabel.setPreferredSize(new java.awt.Dimension(globalLabel, emailLabel.getPreferredSize().height)); p.add(emailLabel, gbc);
    emailField = new JTextField(24); emailField.setName("contactlog_email"); gbc.gridx = 1; p.add(emailField, gbc);
    row++;
    gbc.gridx = 0; gbc.gridy = row; JLabel responseLabel = new JLabel("Contact Response:"); responseLabel.setPreferredSize(new java.awt.Dimension(globalLabel, responseLabel.getPreferredSize().height)); p.add(responseLabel, gbc);
    contactResponseField = new JTextField(24); contactResponseField.setName("contactlog_response"); gbc.gridx = 1; p.add(contactResponseField, gbc);
    row++;
    gbc.gridx = 0; gbc.gridy = row; JLabel generalLabel = new JLabel("Contact General:"); generalLabel.setPreferredSize(new java.awt.Dimension(globalLabel, generalLabel.getPreferredSize().height)); p.add(generalLabel, gbc);
    contactGeneralField = new JTextField(24); contactGeneralField.setName("contactlog_general"); gbc.gridx = 1; p.add(contactGeneralField, gbc);
    row++;
    gbc.gridx = 0; gbc.gridy = row; JLabel specificLabel = new JLabel("Contact Specific:"); specificLabel.setPreferredSize(new java.awt.Dimension(globalLabel, specificLabel.getPreferredSize().height)); p.add(specificLabel, gbc);
    contactSpecificField = new JTextField(24); contactSpecificField.setName("contactlog_specific"); gbc.gridx = 1; p.add(contactSpecificField, gbc);
    row++;

    // Notes label + text area with accessibility
    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; JLabel notesLabel = new JLabel("Notes:"); notesLabel.setPreferredSize(new java.awt.Dimension(globalLabel, notesLabel.getPreferredSize().height)); p.add(notesLabel, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; notesArea = new JTextArea(8,40); notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true); notesArea.setToolTipText("Enter contact notes for the student"); notesArea.getAccessibleContext().setAccessibleName("Contact notes"); JScrollPane notesScroll = new JScrollPane(notesArea); notesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); p.add(notesScroll, gbc);
    notesArea.setName("contactlog_notes");
    notesLabel.setLabelFor(notesArea);

    row++;
    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; JButton save = new JButton("Save Contact");
    save.addActionListener((ActionEvent e)-> saveContact());
    save.setToolTipText("Save contact notes to the database");
    save.setMnemonic(KeyEvent.VK_S);
    save.getAccessibleContext().setAccessibleName("Save Contact Notes");
    save.setName("contactlog_save");
    p.add(save, gbc);

    gbc.gridx = 1; JButton load = new JButton("Load Last Contact");
    load.addActionListener((ActionEvent e) -> loadLastContact());
    load.setToolTipText("Load the most recent contact for the selected student");
    load.setName("contactlog_load");
    p.add(load, gbc);

    add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(()->{ p.setPreferredSize(p.getPreferredSize()); revalidate(); });

        com.studentgui.apphelpers.Helpers.createFolderHierarchy();
    }

    private void loadLastContact() {
        try {
            java.util.Map<String,String> m = com.studentgui.apphelpers.Database.fetchLatestContactLog(this.studentNameParam);
            if (m == null) {
                com.studentgui.apphelpers.UiNotifier.show("No contact found for this student.");
                return;
            }
            guardianField.setText(m.getOrDefault("guardianName", ""));
            String method = m.getOrDefault("contactMethod", "");
            if (method != null) contactMethodCombo.setSelectedItem(method);
            phoneField.setText(m.getOrDefault("phoneNumber", ""));
            emailField.setText(m.getOrDefault("emailAddress", ""));
            contactResponseField.setText(m.getOrDefault("contactResponse", ""));
            contactGeneralField.setText(m.getOrDefault("contactGeneral", ""));
            contactSpecificField.setText(m.getOrDefault("contactSpecific", ""));
            notesArea.setText(m.getOrDefault("contactNotes", ""));
        } catch (SQLException ex) {
            LOG.error("Error loading last contact", ex);
            JOptionPane.showMessageDialog(this, "Database error loading contact: " + ex.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Persist the contact notes entered into the notes area as a session note
     * for the selected student. Shows a confirmation dialog on success and
     * error dialogs on failure.
     */
    private void saveContact() {
        try {
            int studentId = com.studentgui.apphelpers.Database.getOrCreateStudent(this.studentNameParam);
            int ptId = com.studentgui.apphelpers.Database.getOrCreateProgressType("ContactLog");
            int sessionId = com.studentgui.apphelpers.Database.createProgressSession(studentId, ptId, this.dateParam);

            String notes = notesArea.getText();
            String guardian = guardianField.getText();
            String method = (String) contactMethodCombo.getSelectedItem();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String response = contactResponseField.getText();
            String general = contactGeneralField.getText();
            String specific = contactSpecificField.getText();

            // Basic validation
            if (method != null && method.equals("Email") && (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (method != null && method.equals("Phone") && (phone == null || !phone.matches("^[0-9+()\\-\s]{7,20}$"))) {
                JOptionPane.showMessageDialog(this, "Please enter a valid phone number.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Save both the free-form notes field on ProgressSession and structured ContactLog row
            com.studentgui.apphelpers.Database.saveSessionNotes(sessionId, notes);
            com.studentgui.apphelpers.Database.saveContactLog(sessionId, this.studentNameParam, this.dateParam.toString(), guardian, method, phone, email, response, general, specific, notes);
            LOG.info("Saved contact log for {}", studentNameParam);
            com.studentgui.apphelpers.UiNotifier.show("Contact log saved.");
        } catch (SQLException ex) {
            LOG.error("Error saving contact log", ex);
            javax.swing.JOptionPane.showMessageDialog(this, "Database error saving contact log: " + ex.getMessage(), "Database error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}
