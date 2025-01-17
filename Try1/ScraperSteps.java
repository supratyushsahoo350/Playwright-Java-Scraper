package steps;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.cucumber.java.en.*;
import org.junit.Assert;

import java.util.*;

public class ScraperSteps {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private List<Map<String, String>> tshirtData = new ArrayList<>();

    @Given("I am on the Myntra homepage")
    public void i_am_on_the_myntra_homepage() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        page = context.newPage();
        page.navigate("https://www.myntra.com/");
    }

    @When("I search for {string} T-shirts")
    public void i_search_for_tshirts(String catagory) {
        page.fill("input[placeholder='Search for products, brands and more']", catagory + " T-shirts");
        page.keyboard().press("Enter");
    }

    @When("I filter by the brand {string} and {string}")
    public void i_filter_by_the_brand(String brand1, String brand2) {
        // Locate and wait for the discount filter
        Locator discountOption = page.locator("text=10% and above");
        discountOption.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        discountOption.click();
    
        // Wait for the filtering to apply
        page.waitForTimeout(2000);
    
        // Locate the filter search box
        Locator filterSearchBox = page.locator("div.filter-search-filterSearchBox").first();
        filterSearchBox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        filterSearchBox.click();
    
        // For the first brand
        page.fill("input[placeholder='Search for Brand']", brand1);
        page.keyboard().press("Enter");
        
        // Use exact text matching including the count
        String exactTextSelector = String.format("label.vertical-filters-label:has-text('^%s\\([0-9]+\\)$')", brand1);
        Locator brand1Label = page.locator(exactTextSelector).first();
        
        brand1Label.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        brand1Label.scrollIntoViewIfNeeded();
        brand1Label.click();
    
        // Clear and search for second brand
        filterSearchBox.fill("");
        page.fill("input[placeholder='Search for Brand']", brand2);
        page.keyboard().press("Enter");
    
        // Use the same exact text matching for second brand
        String exactTextSelector2 = String.format("label.vertical-filters-label:has-text('^%s\\([0-9]+\\)$')", brand2);
        Locator brand2Label = page.locator(exactTextSelector2).first();
        
        brand2Label.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        brand2Label.scrollIntoViewIfNeeded();
        brand2Label.click();
    }

    
    @Then("I extract discounted T-shirts data")
    public void extractDiscountedData() {
        // Get all product elements
        Locator tshirts = page.locator(".product-base");

        // Wait for the products to be loaded
        tshirts.first().waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Loop through each T-shirt element to scrape the data
        for (int i = 0; i < tshirts.count(); i++) {
            // Wait for the product to be fully loaded
            Locator product = tshirts.nth(i);
            product.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            // Extract brand
            String brand = product.locator(".product-brand").innerText();

            // Extract price
            String priceText = product.locator(".product-discountedPrice").innerText();
            int price = priceText.isEmpty() ? -1 : Integer.parseInt(priceText.replace("Rs.", "").trim());

            // Extract original price
            String originalPriceText = product.locator(".product-strike").innerText();
            int originalPrice = originalPriceText.isEmpty() ? -1
                    : Integer.parseInt(originalPriceText.replace("Rs.", "").trim());

            // Extract discount percentage
            String discountText = product.locator(".product-discountPercentage").innerText();
            String discount = extractNumericDiscount(discountText);

            // Extract product description
            String description = product.locator(".product-product").innerText();

            // Extract product URL
            String productLink = product.locator("a").getAttribute("href");

            // Store data in a map and add to the list
            Map<String, String> data = new HashMap<>();
            data.put("brand", brand);
            data.put("price", String.valueOf(price));
            data.put("original_price", String.valueOf(originalPrice));
            data.put("discount", discount);
            data.put("description", description);
            data.put("product_url", "https://www.myntra.com/" + productLink);

            tshirtData.add(data);
        }
    }

    @Then("I print sorted data by highest discount")
    public void printSortedData() {
        // Sort the data by discount in descending order
        tshirtData.sort((a, b) -> {
            // Get the discount values and handle null or empty values
            String discountA = a.get("discount");
            String discountB = b.get("discount");

            // Check if discount is null or empty and set to "0" if so
            int discountValueA = (discountA != null && !discountA.isEmpty())
                    ? Integer.parseInt(discountA)
                    : 0;
            int discountValueB = (discountB != null && !discountB.isEmpty())
                    ? Integer.parseInt(discountB)
                    : 0;

            return Integer.compare(discountValueB, discountValueA); // Sort in descending order
        });

        // Print the sorted data
        for (Map<String, String> data : tshirtData) {
            System.out.println("Brand: " + data.get("brand") + ", Price: " + data.get("price") + ", Discount: " + data.get("discount") + ", Link: " + data.get("product_url"));
        }

        // Close the browser and playwright instances
        browser.close();
        playwright.close();
    }

    @Then("I verify there are discounted items available")
    public void i_verify_there_are_discounted_items_available() {
        // Verify that there are discounted items present
        boolean hasDiscountedItems = page.locator(".product-discountPercentage").count() > 0;
        Assert.assertTrue("No discounted items found", hasDiscountedItems);
    }

    // Helper method to extract numeric value from discount string
    private String extractNumericDiscount(String discountText) {
        if (discountText == null || discountText.isEmpty()) {
            return "0";
        }

        // Extract only the numeric part before the 'OFF' part
        String numericDiscount = discountText.replaceAll("[^0-9]", "");
        return numericDiscount.isEmpty() ? "0" : numericDiscount;
    }
}
