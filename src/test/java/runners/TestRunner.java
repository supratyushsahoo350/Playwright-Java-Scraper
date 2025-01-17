package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features", // Absolute path to your feature files
        glue = {"steps"}, // Package containing step definitions
        plugin = {"pretty", "html:target/cucumber-reports.html"} // Optional: Generate readable test reports
)
public class TestRunner {
}
