package com.visualengine.engine;

import com.visualengine.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SelfHealingDriver {
    private final WebDriver driver;
    private Map<String, Map<String, String>> locators;
    private final Gson gson = new Gson();

    public SelfHealingDriver(WebDriver driver) {
        this.driver = driver;
        this.locators = loadLocators();
    }

    private Map<String, Map<String, String>> loadLocators() {
        File file = new File(Config.LOCATOR_MAP_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    private void saveLocators() {
        try (FileWriter writer = new FileWriter(Config.LOCATOR_MAP_FILE)) {
            gson.toJson(locators, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private By getBy(String byType, String value) {
        switch (byType) {
            case "id": return By.id(value);
            case "css": return By.cssSelector(value);
            case "xpath": return By.xpath(value);
            default: return By.id(value);
        }
    }

    public WebElement findElement(String elementId, String defaultByType, String defaultValue) {
        if (locators.containsKey(elementId)) {
            Map<String, String> saved = locators.get(elementId);
            try {
                return driver.findElement(getBy(saved.get("by"), saved.get("value")));
            } catch (NoSuchElementException e) {
                // fall through
            }
        }

        try {
            WebElement elem = driver.findElement(getBy(defaultByType, defaultValue));
            if (!locators.containsKey(elementId)) {
                Map<String, String> newLoc = new HashMap<>();
                newLoc.put("by", defaultByType);
                newLoc.put("value", defaultValue);
                locators.put(elementId, newLoc);
                saveLocators();
            }
            return elem;
        } catch (NoSuchElementException e) {
            if (Config.MODE != Config.ExecutionMode.HEAL) {
                throw e;
            }
            System.out.println("Locator failed for " + elementId + ". Initiating self-healing...");
            throw new NoSuchElementException("Unable to heal locator for " + elementId + ". AI Test Agent should review.");
        }
    }
}
