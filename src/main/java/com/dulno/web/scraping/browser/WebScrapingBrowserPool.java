package com.dulno.web.scraping.browser;

import com.dulno.core.error.ErrorRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor(staticName = "create")
public final class WebScrapingBrowserPool {
  public static WebScrapingBrowserPool of(
    ErrorRepository errorRepository, int browserCount
  ) {
    var executor = Executors.newFixedThreadPool(browserCount);
    var browsers = Lists.<WebScrapingBrowser>newArrayList();
    for (var i = 0; i < browserCount; i++) {
      browsers.add(WebScrapingBrowser.create());
    }
    return create(errorRepository, executor, browsers);
  }

  private final ErrorRepository errorRepository;
  private final ExecutorService executor;
  private final List<WebScrapingBrowser> browsers;

  public Future<String> fetchHtml(String url) {
    return executor.submit(() -> processHtmlFetch(url));
  }

  private String processHtmlFetch(String url) throws Exception {
    var browser = acquireBrowser();
    try {
      return browser.fetchHtmlContent(url);
    } finally {
      releaseBrowser(browser);
    }
  }

  private WebScrapingBrowser acquireBrowser() throws InterruptedException {
    synchronized (browsers) {
      while (browsers.isEmpty()) {
        browsers.wait();
      }
      return browsers.remove(0);
    }
  }

  private void releaseBrowser(WebScrapingBrowser browser) {
    synchronized (browsers) {
      browsers.add(browser);
      browsers.notifyAll();
    }
  }

  public void shutdown() {
    try {
      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException exception) {
      errorRepository.processError(exception);
    } finally {
      for (var browser : browsers) {
        browser.close();
      }
    }
  }
}
