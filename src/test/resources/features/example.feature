Feature: Visual Validation Engine Test

  Scenario: Verify Google Home Page visually
    Given I navigate to "https://www.google.com"
    Then the page should visually match baseline "google_home"

  Scenario: Verify Example Domain visually with ignore regions
    Given I navigate to "https://example.com"
    Then the page should visually match baseline "example_domain" ignoring regions
      | x | y | w | h |
      | 0 | 0 | 100| 50|
