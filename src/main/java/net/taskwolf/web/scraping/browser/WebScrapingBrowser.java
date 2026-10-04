package net.taskwolf.web.scraping.browser;

import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.SilentJavaScriptErrorListener;

public final class WebScrapingBrowser {
  public static WebScrapingBrowser create() {
    var browser = new WebScrapingBrowser();
    browser.initialize();
    return browser;
  }

  private WebClient client;

  private void initialize() {
    client = new WebClient();
    client.getOptions().setJavaScriptEnabled(true);
    client.getOptions().setCssEnabled(false);
    client.getOptions().setPrintContentOnFailingStatusCode(false);
    client.getOptions().setThrowExceptionOnScriptError(false);
    client.getOptions().setThrowExceptionOnFailingStatusCode(false);
    client.setJavaScriptErrorListener(new SilentJavaScriptErrorListener());
    client.setCssErrorHandler(new SilentCssErrorHandler());
    client.getOptions().setDownloadImages(false);
    client.getOptions().setRedirectEnabled(true);
    client.getOptions().setTimeout(1000);
    client.setJavaScriptTimeout(1000);
  }

  public String fetchHtmlContent(String url) {
    try {
      HtmlPage page = client.getPage(url);
      return page.asXml();
    } catch (Exception exception) {
      return "";
    }
  }

  public void close() {
    if (client != null) {
      client.close();
    }
  }
}
