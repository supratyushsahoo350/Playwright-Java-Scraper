Feature: Scrape discounted T-shirts from Myntra

  Scenario: Extract discounted Van Heusen T-shirts
    Given I am on the Myntra homepage
    When I search for "Mens" T-shirts
    And I filter by the brand "Van Heusen" and "Roadster"
    Then I extract discounted T-shirts data
    And I print sorted data by highest discount

