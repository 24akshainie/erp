package util;

import java.io.*;

public class DatabaseBackupUtil {

    public static boolean backupErpDatabase(
            String user, String password, String dbName, String saveDir) {

        try {
            // 🟦 Output file
            String filePath = saveDir + File.separator + dbName + "_backup.sql";
            File outFile = new File(filePath);

            // 🟦 FIX: password must be passed inline, not interactively
            String passArg = password == null || password.isEmpty()
                    ? "--password="
                    : "--password=" + password;

            String[] command = {
                    "mysqldump",
                    "-u", user,
                    passArg,
                    "--databases", dbName
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);  // merge stderr & stdout

            Process p = pb.start();

            // 🟦 Read mysqldump output
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream())
            );

            PrintWriter writer = new PrintWriter(new FileWriter(outFile));

            String line;
            int lines = 0;

            while ((line = reader.readLine()) != null) {
                writer.println(line);
                lines++;
            }

            writer.close();
            int exit = p.waitFor();

            System.out.println("Lines dumped = " + lines);
            System.out.println("Exit code = " + exit);

            return exit == 0 && lines > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
