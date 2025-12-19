package ui.instructor;

import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.RectangleInsets;


import auth.session.SessionManager;
import data.DBConnection;

public class ClassStatsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> sectionSelector;
    private JPanel chartPanel;

    public ClassStatsPanel() {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // header
        JLabel header = new JLabel("Class Statistics - Grade Distribution", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(new EmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(5, 0, 15, 0));

        sectionSelector = new JComboBox<>();
        loadInstructorSections();

        JLabel secLabel = new JLabel("Select Section: ");
        secLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        topPanel.add(secLabel);
        topPanel.add(sectionSelector);

        add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        // table
        String[] cols = {"Metric", "Value"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(260, 0));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(new EmptyBorder(0, 20, 0, 0));

        // Layout combining table + chart
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.add(scroll, BorderLayout.WEST);
        center.add(chartPanel, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // Load chart when section is selected
        sectionSelector.addActionListener(e -> loadStats());
    }

    // table styling
    private void styleTable(JTable table) {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setShowGrid(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(55,70,115));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);

                if (isSelected) c.setBackground(new Color(200, 220, 240));
                else if (row % 2 == 0) c.setBackground(new Color(250, 250, 250));
                else c.setBackground(Color.WHITE);

                setBorder(noFocusBorder);
                return c;
            }
        });
    }

    // load instructors from db    
    private void loadInstructorSections() {
        sectionSelector.removeAllItems();
        int instructorId = SessionManager.getCurrentUserId();

        String sql = "SELECT id, course_id FROM sections WHERE instructor_id = ?";

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sectionSelector.addItem(rs.getInt("id") + " - " + rs.getString("course_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStats() {

        model.setRowCount(0);
        chartPanel.removeAll();

        if (sectionSelector.getSelectedItem() == null) return;

        int sectionId = Integer.parseInt(sectionSelector.getSelectedItem().toString().split(" - ")[0]);

        String avgSql = """
            SELECT 
                COUNT(DISTINCT e.student_id) AS total_students,
                AVG(CASE WHEN g.component='Quiz' THEN g.score END) AS avg_quiz,
                AVG(CASE WHEN g.component='Midterm' THEN g.score END) AS avg_midterm,
                AVG(CASE WHEN g.component='Final' THEN g.score END) AS avg_final
            FROM enrollments e
            LEFT JOIN grades g ON g.enrollment_id = e.id
            WHERE e.section_id = ?
        """;

        String medianSql = """
            SELECT g.component, g.score
            FROM enrollments e
            JOIN grades g ON g.enrollment_id = e.id
            WHERE e.section_id = ?
        """;

        try (Connection conn = DBConnection.getErpConnection()) {

            // get average
            PreparedStatement ps1 = conn.prepareStatement(avgSql);
            ps1.setInt(1, sectionId);
            ResultSet rs1 = ps1.executeQuery();

            double avgQuiz = 0, avgMid = 0, avgFinal = 0;
            int total = 0;

            if (rs1.next()) {
                total = rs1.getInt("total_students");
                avgQuiz = rs1.getDouble("avg_quiz");
                avgMid = rs1.getDouble("avg_midterm");
                avgFinal = rs1.getDouble("avg_final");
            }

            // get median
            PreparedStatement ps2 = conn.prepareStatement(medianSql);
            ps2.setInt(1, sectionId);
            ResultSet rs2 = ps2.executeQuery();

            List<Double> quiz = new ArrayList<>();
            List<Double> mid = new ArrayList<>();
            List<Double> fin = new ArrayList<>();

            while (rs2.next()) {
                String comp = rs2.getString("component");
                double score = rs2.getDouble("score");

                if (comp.equals("Quiz")) quiz.add(score);
                else if (comp.equals("Midterm")) mid.add(score);
                else if (comp.equals("Final")) fin.add(score);
            }

            double medQuiz = median(quiz);
            double medMid = median(mid);
            double medFinal = median(fin);

            model.addRow(new Object[]{"Total Students", total});
            model.addRow(new Object[]{"Average Quiz", avgQuiz});
            model.addRow(new Object[]{"Average Midterm", avgMid});
            model.addRow(new Object[]{"Average Final", avgFinal});
            model.addRow(new Object[]{"Median Quiz", medQuiz});
            model.addRow(new Object[]{"Median Midterm", medMid});
            model.addRow(new Object[]{"Median Final", medFinal});

            // bar chart
            DefaultCategoryDataset ds = new DefaultCategoryDataset();
            ds.addValue(avgQuiz, "Average", "Quiz");
            ds.addValue(avgMid, "Average", "Midterm");
            ds.addValue(avgFinal, "Average", "Final");

            JFreeChart chart = ChartFactory.createBarChart(
                    "", "Component", "Score", ds
            );

            chart.setBackgroundPaint(Color.WHITE);
            var plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);

            plot.setRangeGridlinePaint(new Color(220, 220, 220));
            plot.setRangeGridlinesVisible(true);

            plot.setInsets(new RectangleInsets(20, 20, 20, 20));

            plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 13));
            plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 13));

            Color elegantBlue = new Color(33, 105, 185);

            var renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, elegantBlue);
            renderer.setMaximumBarWidth(0.12);
            renderer.setShadowVisible(false);
            renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());

            plot.getDomainAxis().setLowerMargin(0.15);
            plot.getDomainAxis().setUpperMargin(0.15);

            chart.setAntiAlias(true);

            ChartPanel cp = new ChartPanel(chart);
            cp.setBackground(Color.WHITE);
            cp.setPopupMenu(null);
            cp.setBorder(new EmptyBorder(10, 10, 10, 10));

            chartPanel.add(cp, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "⚠ Error loading statistics!");
        }
    }

    // helper fnc to find median
    private double median(List<Double> list) {
        if (list.isEmpty()) return 0;

        list.sort(Double::compareTo);
        int n = list.size();

        if (n % 2 == 1) {
            return list.get(n / 2);
        }
        return (list.get(n/2 - 1) + list.get(n/2)) / 2.0;
    }
}