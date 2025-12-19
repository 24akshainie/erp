package ui.student;

import auth.session.SessionManager;
import data.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class TranscriptFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public TranscriptFrame() {
        setTitle("Transcript Viewer");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // header
        JLabel header = new JLabel("My Transcript", SwingConstants.CENTER);
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        // table
        String[] cols = {"Course Code", "Course Title", "Semester", "Year", "Final Grade"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // buttons
        JButton exportCSV = styledButton("Export CSV");
        JButton exportPDF = styledButton("Export PDF");
        JButton refresh = styledButton("Refresh");

        exportCSV.addActionListener(e -> exportAsCSV());
        exportPDF.addActionListener(e -> exportAsPDF());
        refresh.addActionListener(e -> loadTranscript());

        JPanel btnPanel = new JPanel();
        btnPanel.add(refresh);
        btnPanel.add(exportCSV);
        btnPanel.add(exportPDF);

        add(btnPanel, BorderLayout.SOUTH);

        // Load data
        loadTranscript();
    }


    // table styling
    private void styleTable(JTable table) {

        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setRowHeight(28);

        JTableHeader header = table.getTableHeader();
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        header.setBackground(new Color(55,70,115));
        header.setForeground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {

                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);

                if (isSelected) c.setBackground(new Color(200, 220, 240));
                else c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);

                return c;
            }
        });
    }

    // button styling
    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(225, 233, 245)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(Color.WHITE); }
        });

        return btn;
    }


    private void loadTranscript() {

        System.out.println("\n====================");
        System.out.println("[DEBUG] loadTranscript() called");
        System.out.println("====================");

        model.setRowCount(0);
        int studentId = SessionManager.getCurrentUserId();

        System.out.println("[DEBUG] SessionManager.getUserId() = " + studentId);

        String sql = """
                    SELECT 
                        c.code,
                        c.title,
                        s.semester,
                        s.year,
                        COALESCE(MAX(g.final_grade), 'NA') AS final_grade
                    FROM enrollments e
                    JOIN sections s ON e.section_id = s.id
                    JOIN courses c ON s.course_id = c.code
                    LEFT JOIN grades g ON g.enrollment_id = e.id
                    WHERE e.student_id = ?
                    GROUP BY c.code, c.title, s.semester, s.year
                    ORDER BY s.year, s.semester;

                            """;

        try (Connection conn = DBConnection.getErpConnection()) {

            if (conn == null) {
              
                JOptionPane.showMessageDialog(this, "DB Connection failed!");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;

                model.addRow(new Object[]{
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("semester"),
                        rs.getInt("year"),
                        rs.getString("final_grade")
                });
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "No transcript records!");
            } 
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data!");
        }
    }

    private void exportAsCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("Transcript.csv"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                FileWriter writer = new FileWriter(chooser.getSelectedFile());
                writer.write("Code,Title,Semester,Year,FinalGrade\n");

                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        writer.write(model.getValueAt(i, j).toString());
                        if (j < model.getColumnCount() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }
                writer.close();
                JOptionPane.showMessageDialog(this, "CSV exported!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "CSV export failed!");
        }
    }

    private void exportAsPDF() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("Transcript.pdf"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                Document doc = new Document();
                PdfWriter.getInstance(doc, new java.io.FileOutputStream(chooser.getSelectedFile()));
                doc.open();

                Paragraph title = new Paragraph(
                    "Official Transcript - " + SessionManager.getCurrentUsername(),
                    new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            16,
                            com.itextpdf.text.Font.BOLD
                    )
                );

                title.setAlignment(Element.ALIGN_CENTER);

                doc.add(title);
                doc.add(new Paragraph("\n"));

                PdfPTable pdfTable = new PdfPTable(model.getColumnCount());
                pdfTable.setWidthPercentage(100);

                for (int i = 0; i < model.getColumnCount(); i++) {
                    pdfTable.addCell(new PdfPCell(new Phrase(model.getColumnName(i))));
                }

                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        pdfTable.addCell(new PdfPCell(new Phrase(String.valueOf(model.getValueAt(i, j)))));
                    }
                }

                doc.add(pdfTable);
                doc.close();

                JOptionPane.showMessageDialog(this, "PDF Exported!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "PDF export failed!");
        }
    }
}
