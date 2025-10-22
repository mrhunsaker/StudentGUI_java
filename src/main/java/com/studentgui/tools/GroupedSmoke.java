package com.studentgui.tools;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.studentgui.apphelpers.Helpers;
import com.studentgui.apppages.JLineGraph;

public class GroupedSmoke {
    public static void main(String[] args) throws Exception {
        Helpers.createFolderHierarchy();
        JLineGraph graph = new JLineGraph();

        // build part codes with P1_, P2_, P3_ groups (3+2+4 items)
        String[] codes = new String[]{"P1_1","P1_2","P1_3","P2_1","P2_2","P3_1","P3_2","P3_3","P3_4"};

        // Create sample data: 3 sessions
        List<List<Integer>> data = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            List<Integer> row = new ArrayList<>();
            for (int i = 0; i < codes.length; i++) {
                row.add((i + s) % 5);
            }
            data.add(row);
        }
        graph.updateWithGroupedData(data, codes);

        Path outDir = Helpers.APP_HOME.resolve("StudentDataFiles").resolve(Helpers.safeName("Grouped Smoke")).resolve("plots");
        java.nio.file.Files.createDirectories(outDir);
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        Path outFile = outDir.resolve("GroupedSmoke-" + LocalDate.now().format(df) + ".png");
        graph.saveChart(outFile, 800, 600);
        System.out.println("Grouped smoke wrote chart to: " + outFile.toAbsolutePath());
        System.out.println("Exists: " + java.nio.file.Files.exists(outFile));
    }
}
