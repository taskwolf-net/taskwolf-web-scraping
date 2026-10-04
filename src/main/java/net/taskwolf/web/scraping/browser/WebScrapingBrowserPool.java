package net.taskwolf.web.scraping.browser;

import net.taskwolf.core.error.ErrorRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.*;

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

  public CompletableFuture<String> fetchHtml(String url) {
    return CompletableFuture.supplyAsync(() -> processHtmlFetch(url), executor);
  }

  private String processHtmlFetch(String url) {
    try {
      var browser = acquireBrowser();
      try {
        return browser.fetchHtmlContent(url);
      } finally {
        releaseBrowser(browser);
      }
    } catch (Exception exception) {
      errorRepository.processError(exception);
      return "";
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
