package com.visualengine.engine;

import com.visualengine.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class VisualCheckpoint {
    private final VisualComparisonEngine engine;
    private final AIReviewer ai;
    public static final String RESULTS_FILE = Config.getReportsDir() + "/results.jsonl";

    public VisualCheckpoint() {
        this.engine = new VisualComparisonEngine(Config.PIXEL_MATCH_THRESHOLD);
        this.ai = new AIReviewer();
    }

    private void appendResult(Map<String, Object> result) {
        try (FileWriter fw = new FileWriter(RESULTS_FILE, true)) {
            fw.write(new Gson().toJson(result) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validateStep(WebDriver driver, String pageName, List<VisualComparisonEngine.Region> ignoreRegions) throws IOException {
        String viewport = driver.manage().window().getSize().getWidth() + "x" + driver.manage().window().getSize().getHeight();
        String baselineDir = Config.getBaselineDir(viewport);
        String reportsDir = Config.getReportsDir();

        String baseImgPath = baselineDir + "/" + pageName + ".png";
        String baseDomPath = baselineDir + "/" + pageName + ".html";
        
        String actualImgPath = reportsDir + "/" + pageName + "_actual.png";
        String actualDomPath = reportsDir + "/" + pageName + "_actual.html";
        String diffImgPath = reportsDir + "/" + pageName + "_diff.png";

        File actualScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(actualScreenshot.toPath(), new File(actualImgPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        
        String actualDom = DOMParser.captureDOMSnapshot(driver);
        try (FileWriter fw = new FileWriter(actualDomPath)) { fw.write(actualDom); }
        Map<String, Object> metadata = DOMParser.extractMetadata(driver, pageName);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("scenario", pageName);
        result.put("mode", Config.MODE.name());
        result.put("actual_img", actualImgPath);
        result.put("baseline_img", baseImgPath);
        result.put("passed", true);
        result.put("classification", "N/A");

        if (Config.MODE == Config.ExecutionMode.BASELINE) {
            String pendingDir = Config.getPendingBaselineDir(viewport);
            String pendingImgPath = pendingDir + "/" + pageName + ".png";
            String pendingDomPath = pendingDir + "/" + pageName + ".html";
            
            Files.copy(actualScreenshot.toPath(), new File(pendingImgPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            try (FileWriter fw = new FileWriter(pendingDomPath)) { fw.write(actualDom); }
            System.out.println("Pending baseline saved for " + pageName + " at " + pendingDir + " (Needs Approval)");
            
            result.put("classification", "PENDING_APPROVAL");
            result.put("passed", true);
            appendResult(result);
            return true;
        } else if (Config.MODE == Config.ExecutionMode.COMPARE) {
            if (!new File(baseImgPath).exists()) {
                System.out.println("Baseline missing for " + pageName + ". Auto-generating a pending baseline.");
                String pendingDir = Config.getPendingBaselineDir(viewport);
                String pendingImgPath = pendingDir + "/" + pageName + ".png";
                Files.copy(actualScreenshot.toPath(), new File(pendingImgPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                result.put("classification", "MISSING_BASELINE");
                result.put("passed", false);
                appendResult(result);
                return false;
            }

            VisualComparisonEngine.ComparisonResult compRes = engine.compareImages(baseImgPath, actualImgPath, diffImgPath, ignoreRegions);
            result.put("score", compRes.score);
            result.put("passed", compRes.passed);
            result.put("diff_img", diffImgPath);

            if (!compRes.passed) {
                System.out.println("Visual difference detected. Triggering AI...");
                String baseDom = new String(Files.readAllBytes(new File(baseDomPath).toPath()));
                Map<String, Integer> domDiff = DOMParser.compareDOMs(baseDom, actualDom);
                String classification = ai.classifyDiff(compRes.score, domDiff, metadata);
                result.put("classification", classification);
                
                // Never auto-pass. A visual diff requires human approval or an explicit BASELINE re-run.
                result.put("passed", false);
            }

            appendResult(result);
            return (boolean) result.get("passed");
        }
        return true;
    }
}
