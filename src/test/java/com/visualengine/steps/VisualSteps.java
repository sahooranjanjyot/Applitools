package com.visualengine.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.datatable.DataTable;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import com.visualengine.engine.VisualCheckpoint;
import com.visualengine.engine.VisualComparisonEngine;
import com.visualengine.engine.Reporter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.junit.Assert;

import io.github.bonigarcia.wdm.WebDriverManager;

public class VisualSteps {
    private WebDriver driver;
    private VisualCheckpoint checkpoint;

    @Before
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        checkpoint = new VisualCheckpoint();
    }

    @Given("I navigate to {string}")
    public void i_navigate_to(String url) {
        driver.get(url);
    }

    @Then("the page should visually match baseline {string}")
    public void the_page_should_visually_match_baseline(String pageName) throws Exception {
        boolean passed = checkpoint.validateStep(driver, pageName, null);
        Assert.assertTrue("Visual validation failed for " + pageName, passed);
    }

    @Then("the page should visually match baseline {string} ignoring regions")
    public void the_page_should_visually_match_baseline_ignoring_regions(String pageName, DataTable table) throws Exception {
        List<VisualComparisonEngine.Region> ignoreRegions = new ArrayList<>();
        for (Map<String, String> row : table.asMaps()) {
            ignoreRegions.add(new VisualComparisonEngine.Region(
                Integer.parseInt(row.get("x")),
                Integer.parseInt(row.get("y")),
                Integer.parseInt(row.get("w")),
                Integer.parseInt(row.get("h"))
            ));
        }
        boolean passed = checkpoint.validateStep(driver, pageName, ignoreRegions);
        Assert.assertTrue("Visual validation failed for " + pageName, passed);
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
        Reporter.generateReport();
    }
}
