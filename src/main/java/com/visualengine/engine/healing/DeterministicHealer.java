package com.visualengine.engine.healing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DeterministicHealer {
    
    public By healLocator(WebDriver driver, By brokenLocator) {
        System.out.println("[HEALING ENGINE] NoSuchElementException caught for locator: " + brokenLocator);
        System.out.println("[HEALING ENGINE] Analyzing DOM to find the closest match...");
        
        String domSource = driver.getPageSource();
        Document doc = Jsoup.parse(domSource);
        
        String locatorString = brokenLocator.toString();
        
        // Very basic deterministic fallback logic (Rule-based Healing without LLM)
        if (locatorString.contains("id: ")) {
            String id = locatorString.substring(locatorString.indexOf("id: ") + 4).trim();
            Elements allElements = doc.getAllElements();
            for (Element el : allElements) {
                if (el.hasAttr("id") && !el.id().isEmpty() && (el.id().contains(id) || id.contains(el.id()))) {
                    System.out.println("[HEALING ENGINE] SUCCESS: Found similar ID -> " + el.id());
                    return By.id(el.id());
                }
            }
        }
        
        if (locatorString.contains("name: ")) {
            String name = locatorString.substring(locatorString.indexOf("name: ") + 6).trim();
            Elements allElements = doc.getAllElements();
            for (Element el : allElements) {
                if (el.hasAttr("name") && !el.attr("name").isEmpty() && (el.attr("name").contains(name) || name.contains(el.attr("name")))) {
                    System.out.println("[HEALING ENGINE] SUCCESS: Found similar Name -> " + el.attr("name"));
                    return By.name(el.attr("name"));
                }
            }
        }
        
        System.out.println("[HEALING ENGINE] FAILED: Could not heal locator deterministically.");
        return null;
    }
}
