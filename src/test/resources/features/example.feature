Feature: Visual Validation Engine Test

  Scenario: Verify Google Home Page visually
    Given I navigate to "https://www.google.com"
    Then the page should visually match baseline "google_home"

  Scenario: Verify Example Domain visually with ignore regions
    Given I navigate to "https://example.com"
    Then the page should visually match baseline "example_domain" ignoring regions
      | x | y | w | h |
      | 0 | 0 | 100| 50|

  Scenario: Negative Visual Validation (Deliberate Failure)
    Given I navigate to "https://www.wikipedia.org"
    Then the page should visually match baseline "example_domain"

  Scenario: Self-Healing Element Detection
    Given I navigate to "https://www.wikipedia.org"
    # The real search box is id="searchInput". We will use id="searchInput-broken" to trigger the healer!
    And I try to click a broken element "searchInput-broken"
