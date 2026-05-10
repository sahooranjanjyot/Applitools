# Enterprise Offline Visual Validation Engine (Applitools Alternative)

A highly secure, 100% offline, deterministic visual regression and autonomous QA framework built natively for Java Selenium BDD. 

This engine is designed specifically for highly restricted corporate networks (Banking, Defense, Enterprise) where data cannot leave the network. It uses OpenCV for mathematically precise Structural Similarity (SSIM) visual comparison and JSoup for deterministic DOM analysis—without requiring any external API calls, cloud storage, or third-party subscriptions.

## 🌟 Key Features

1. **100% Offline & Secure:** No API keys, no network boundaries crossed. Your proprietary DOM structures and screenshots never leave the internal network.
2. **True OpenCV SSIM Analysis:** Uses `TM_CCOEFF_NORMED` template matching instead of naive pixel differencing, drastically reducing false positives caused by sub-pixel shifts or anti-aliasing.
3. **Deterministic Rule-Based Fallback:** Automatically classifies defects (`MINOR_DOM_CHANGE`, `LAYOUT_OR_STYLE_SHIFT`, `STRUCTURAL_DEFECT`) via rapid mathematical logic—no LLM hallucinations.
4. **Base64 Standalone HTML Reports:** Generates portable HTML visual reports with images fully embedded as Base64 strings. Perfect for Jenkins, GitLab CI, and GitHub Actions.
5. **Approval Governance Workflow:** Strict `PENDING_APPROVAL` workflow prevents automated tests from silently overwriting golden baselines.
6. **Thread-Safe Architecture:** Built for parallel execution with isolated instances preventing WebDriver resource leakage.

---

## 🏗️ Architecture Setup

To deploy this engine into an isolated environment, you will compile it as a standalone JAR and inject it into your primary test framework.

### 1. Compile the Engine JAR (Internet-Connected Machine)
```bash
git clone https://github.com/sahooranjanjyot/Applitools.git
cd Applitools
mvn clean package -DskipTests
```
This produces the core engine file: `target/visual-validation-engine-1.0-SNAPSHOT.jar`

### 2. Move to the Restricted Network
Copy the `visual-validation-engine-1.0-SNAPSHOT.jar` to a USB drive or Secure FTP transfer, and place it inside your main Selenium BDD project under a `libs/` directory.

```text
my-selenium-framework/
├── src/
├── libs/
│   └── visual-validation-engine-1.0-SNAPSHOT.jar
└── pom.xml
```

---

## ⚙️ Integration into your Selenium Framework

### Update `pom.xml`
In your existing Selenium BDD framework, add the engine as a `system` scoped dependency along with its core libraries:

```xml
<!-- 1. The Offline Visual Validation Engine JAR -->
<dependency>
    <groupId>com.visualengine</groupId>
    <artifactId>visual-validation-engine</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/visual-validation-engine-1.0-SNAPSHOT.jar</systemPath>
</dependency>

<!-- 2. Required Engine Dependencies -->
<dependency>
    <groupId>org.openpnp</groupId>
    <artifactId>opencv</artifactId>
    <version>4.7.0-0</version>
</dependency>
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### Implement Cucumber Steps
Instantiate `VisualCheckpoint` and pass it your framework's active WebDriver instance.

```java
package your.framework.steps;

import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import org.junit.Assert;
import com.visualengine.engine.VisualCheckpoint;
import com.visualengine.engine.VisualComparisonEngine.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisualValidationSteps {
    
    // Instantiate the isolated engine checkpoint per scenario
    private VisualCheckpoint checkpoint = new VisualCheckpoint();

    @Then("the page should visually match baseline {string}")
    public void validateBaseline(String pageName) throws Exception {
        boolean passed = checkpoint.validateStep(YourDriverManager.getDriver(), pageName, null);
        Assert.assertTrue("Visual regression detected! Check visual_reports directory.", passed);
    }

    @Then("the page should visually match baseline {string} ignoring regions")
    public void validateBaselineWithIgnore(String pageName, DataTable table) throws Exception {
        List<Region> ignoreRegions = new ArrayList<>();
        for (Map<String, String> row : table.asMaps()) {
            ignoreRegions.add(new Region(
                Integer.parseInt(row.get("x")),
                Integer.parseInt(row.get("y")),
                Integer.parseInt(row.get("w")),
                Integer.parseInt(row.get("h"))
            ));
        }
        boolean passed = checkpoint.validateStep(YourDriverManager.getDriver(), pageName, ignoreRegions);
        Assert.assertTrue("Visual regression detected! Check visual_reports directory.", passed);
    }
}
```

---

## 🏃 Execution Modes

The engine dynamically alters its behavior via the `VISUAL_EXECUTION_MODE` environment variable.

### 1. Baseline Generation (Capture Mode)
Use this mode to capture new baseline screenshots.
* **Logic:** Screenshots are written to `/pending_baselines/{branch}/{viewport}/`.
* **Note:** They must be manually reviewed and moved to `/visual_baselines/` to enforce strict QA governance.
```bash
export VISUAL_EXECUTION_MODE=BASELINE
mvn test
```

### 2. Validation Execution (Compare Mode)
Use this mode during CI/CD to compare the active DOM and Screenshots against the golden baselines.
* **Logic:** Generates OpenCV Diff images and outputs a fully portable Base64 HTML report to `/visual_reports/`.
```bash
export VISUAL_EXECUTION_MODE=COMPARE
mvn test
```

### 3. Graceful Missing Baseline Handling
If the pipeline runs in `COMPARE` mode and encounters a page without a baseline, it will **not** throw an exception. Instead, it will:
1. Log a `MISSING_BASELINE` classification.
2. Automatically generate the pending baseline in the `/pending_baselines/` directory.
3. Fail the test to mandate QA approval.

---

## 🔧 Fine-Tuning the Engine
You can dynamically adjust the OpenCV SSIM strictness by setting an environment variable before execution:
```bash
export PIXEL_MATCH_THRESHOLD=0.98  # 98% Correlation Required (Default)
```
