package util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class CSVExporter {

    /**
     * Export a table with headers + rows to CSV.
     */
    public static void export(String fileName,
                              List<String> headers,
                              List<List<String>> rows) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {

            // Write headers
            pw.println(toCSVLine(headers));

            // Write rows
            for (List<String> row : rows) {
                pw.println(toCSVLine(row));
            }

            System.out.println("[CSVExporter] CSV created: " + fileName);

        } catch (Exception e) {
            System.out.println("[CSVExporter] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts a list of values into a properly formatted CSV line.
     */
    private static String toCSVLine(List<String> values) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            sb.append(escape(values.get(i)));

            if (i != values.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * Escapes commas, quotes, and newlines according to CSV rules.
     */
    private static String escape(String value) {
        if (value == null) return "";

        boolean mustQuote = value.contains(",") ||
                            value.contains("\"") ||
                            value.contains("\n") ||
                            value.contains("\r");

        String escaped = value.replace("\"", "\"\"");
        
        if (mustQuote) {
            return "\"" + escaped + "\"";
        } else {
            return escaped;
        }
    }
}
