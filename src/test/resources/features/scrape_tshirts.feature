Feature: Scrape discounted T-shirts from Myntra

  Scenario: Extract discounted Van Heusen T-shirts
    Given I am on the Myntra homepage
    When I Click "Men" from the navbar
    And I Select "T-Shirts" from the dropdown
    And I search for the brand name "Van Heusen"
    And I filter by the brand "Van Heusen"
    And I sort by the "Better Discount"
    Then I extract discounted T-shirts data
    And I print sorted data by highest discount

  Scenario: Handle no discount scenario for Van Heusen
    Given I am on the Myntra homepage
    When I Click "Men" from the navbar
    And I Select "T-Shirts" from the dropdown
    And I search for the brand name "Van Heusen"
    And I filter by the brand "Van Heusen"
    Then I verify there are discounted items available