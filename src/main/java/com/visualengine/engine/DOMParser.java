package com.visualengine.engine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import java.util.HashMap;
import java.util.Map;

public class DOMParser {
    public static String captureDOMSnapshot(WebDriver driver) {
        Document doc = Jsoup.parse(driver.getPageSource());
        // Anonymize: Remove text nodes, leaving only the structural tags and classes
        doc.select("*").forEach(el -> {
            if (!el.ownText().isEmpty()) {
                el.text("[REDACTED]");
            }
        });
        return doc.outerHtml();
    }

    public static Map<String, Object> extractMetadata(WebDriver driver, String scenarioName) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("url", driver.getCurrentUrl());
        meta.put("scenario", scenarioName);
        meta.put("window_size", driver.manage().window().getSize().toString());
        return meta;
    }

    public static Map<String, Integer> compareDOMs(String baselineDOM, String actualDOM) {
        Document baseDoc = Jsoup.parse(baselineDOM);
        Document actualDoc = Jsoup.parse(actualDOM);

        int baseCount = baseDoc.getAllElements().size();
        int actualCount = actualDoc.getAllElements().size();

        Map<String, Integer> diff = new HashMap<>();
        diff.put("base_count", baseCount);
        diff.put("actual_count", actualCount);
        diff.put("diff_count", Math.abs(baseCount - actualCount));
        
        return diff;
    }
}
