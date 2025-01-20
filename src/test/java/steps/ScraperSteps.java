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

    @When("I Click \"Men\" from the navbar")
    public void i_click_men_from_the_navbar() {
        Locator menNav = page.locator("a[data-group='men']");
        menNav.hover(); // Hover over "Men" in the navbar

        // Wait for the dropdown menu to appear
        Locator dropdown = page.locator("div.desktop-categoryContainer[data-group='men']");
        dropdown.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    @When("I Select \"T-Shirts\" from the dropdown")
    public void i_select_tshirts_from_the_dropdown() {
        // Locate and click the "T-Shirts" link within the dropdown
        Locator tshirtsLink = page.locator("a.desktop-categoryLink[href='/men-tshirts']");
        tshirtsLink.click();
    }

    @When("I navigate to the filter section on the left side")
    public void i_navigate_to_filter_section() {
        // Wait for the filter section to be visible
        Locator filterSection = page.locator(".vertical-filters-panel");
        filterSection.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    @When("I search for the brand name {string}")
    public void i_search_for_the_brand_name(String brand) {
        // Locate the search icon using the provided XPath
        Locator searchIcon = page
                .locator("xpath=//*[@id=\"mountRoot\"]/div/main/div[3]/div[1]/section/div/div[3]/div[1]");
        searchIcon.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        searchIcon.click();

        // Fill the brand name in the search bar
        page.fill("input[placeholder='Search for Brand']", brand);
        page.keyboard().press("Enter");
    }

    @When("I filter by the brand {string}")
    public void i_filter_by_the_brand(String brand) {
        // Locate the label corresponding to the brand name
        Locator brandCheckbox = page.locator("label:has-text('" + brand + "')").first();

        // Wait for the label to be fully visible and click it
        brandCheckbox.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        brandCheckbox.click();
        // Wait for the filtering to apply
        page.waitForTimeout(2000);

    }

    @When("I sort by the {string}")
    public void i_filter_by_the_discount(String discount) {

        // Locate and wait for the discount filter
        Locator sortByDropdown = page.locator(".sort-sortBy");

        // Hover over the dropdown menu to reveal options
        sortByDropdown.hover();

        // Wait for the dropdown options to be visible
        page.waitForSelector(".sort-list", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));

        // Locate the specific discount option
        Locator discountOption = page.locator("label.sort-label:has-text('" + discount + "')");

        // Click the "Better Discount" option
        discountOption.click();

        // Wait for the filtering to apply (optional)
        page.waitForTimeout(2000);

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

            // Check if discount is a valid percentage
            if (!discount.isEmpty() && Integer.parseInt(discount) > 100) {
                System.out.println(
                        "Skipping product - Not a percentage discount: Brand: " + brand + ", Discount: " + discount);
                continue;
            }

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
            System.out.println("Brand: " + data.get("brand") + ", Price: " + data.get("price") + ", Discount: "
                    + data.get("discount") + ", Link: " + data.get("product_url"));
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