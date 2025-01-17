Feature: Scrape discounted T-shirts from Myntra

  Scenario: Extract discounted Van Heusen T-shirts
    Given I am on the Myntra homepage
    When I search for "Van Heusen and Roadster Men" T-shirts
    And I filter by the brand "Van Heusen"
    Then I extract discounted T-shirts data
    And I print sorted data by highest discount

  Scenario: Handle no discount scenario for Van Heusen
    Given I am on the Myntra homepage
    When I search for "Van Heusen" T-shirts
    And I filter by the brand "Van Heusen"
    Then I verify there are discounted items available
