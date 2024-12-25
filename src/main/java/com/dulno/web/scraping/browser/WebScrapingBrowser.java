package com.dulno.web.scraping.browser;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public final class WebScrapingBrowser {
  public static WebScrapingBrowser create() {
    var browser = new WebScrapingBrowser();
    browser.initialize();
    return browser;
  }

  private WebDriver driver;

  private void initialize() {
    System.setProperty("webdriver.chrome.driver",
      System.getProperty("user.dir") + "/configurations/web-scraping/chromedriver");
    var options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-gpu");
    options.addArguments("--disable-javascript");
    options.addArguments("--disable-software-rasterizer");
    options.addArguments("--blink-settings=imagesEnabled=false");
    driver = new ChromeDriver(options);
  }

  public String fetchHtmlContent(String url) {
    driver.get(url);
    return driver.getPageSource();
  }

  public void close() {
    if (driver != null) {
      driver.quit();
    }
  }
}
