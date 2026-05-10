package com.visualengine.engine;

import com.visualengine.config.Config;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

public class Reporter {
    public static void generateReport() {
        File resultsFile = new File(VisualCheckpoint.RESULTS_FILE);
        if (!resultsFile.exists()) return;

        List<Map<String, Object>> results = new ArrayList<>();
        Gson gson = new Gson();
        try (BufferedReader br = new BufferedReader(new FileReader(resultsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                results.add(gson.fromJson(line, Map.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (results.isEmpty()) return;

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>")
            .append("body{font-family:Arial;} .pass{color:green;} .fail{color:red;} img{width:100%;}")
            .append(".col{float:left; width:33%; padding:5px; box-sizing:border-box;} .row::after{content:''; clear:both; display:table;}")
            .append("</style></head><body><h1>Visual Validation Report</h1>");

        for (Map<String, Object> res : results) {
            boolean passed = (boolean) res.get("passed");
            html.append("<h2>Scenario: ").append(res.get("scenario")).append("</h2>")
                .append("<p>Status: <span class='").append(passed ? "pass" : "fail").append("'>")
                .append(passed ? "PASS" : "FAIL").append("</span></p>")
                .append("<p>Classification: ").append(res.get("classification")).append("</p>");

            html.append("<div class='row'>");
            try {
                String base64Baseline = encodeFileToBase64((String) res.get("baseline_img"));
                html.append("<div class='col'><h3>Baseline</h3><img src='data:image/png;base64,").append(base64Baseline).append("'></div>");
            } catch (Exception e) {}

            if (res.containsKey("actual_img")) {
                try {
                    String base64Actual = encodeFileToBase64((String) res.get("actual_img"));
                    html.append("<div class='col'><h3>Actual</h3><img src='data:image/png;base64,").append(base64Actual).append("'></div>");
                } catch (Exception e) {}
            }
            if (res.containsKey("diff_img") && res.get("diff_img") != null) {
                try {
                    String base64Diff = encodeFileToBase64((String) res.get("diff_img"));
                    html.append("<div class='col'><h3>Diff</h3><img src='data:image/png;base64,").append(base64Diff).append("'></div>");
                } catch (Exception e) {}
            }
            html.append("</div><hr>");
        }
        html.append("</body></html>");

        String filename = Config.getReportsDir() + "/report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(html.toString());
            System.out.println("Visual QA Report generated at: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String encodeFileToBase64(String filePath) throws IOException {
        if (filePath == null || !new File(filePath).exists()) return "";
        byte[] fileContent = java.nio.file.Files.readAllBytes(new File(filePath).toPath());
        return java.util.Base64.getEncoder().encodeToString(fileContent);
    }
}
