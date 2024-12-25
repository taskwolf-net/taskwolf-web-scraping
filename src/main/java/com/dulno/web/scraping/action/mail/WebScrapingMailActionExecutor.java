package com.dulno.web.scraping.action.mail;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import com.dulno.web.scraping.browser.WebScrapingBrowserPool;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor(staticName = "create")
public final class WebScrapingMailActionExecutor implements ActionExecutor {
  private final WebScrapingBrowserPool webScrapingBrowserPool;
  private String website;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    website = dissolve.dissolve(website);
    website = website.replaceAll("(https?://[^/]+).*", "$1");
    return CompletableFuture.completedFuture(findMail());
  }

  private ActionResult findMail() {
    /*var mainPageContent = webScrapingBrowserPool.fetchHtml(website);

    // Emails extrahieren
    HashSet<String> emails = extractEmails(website, mainPageContent);

    // Nach relevanten Seiten suchen
    HashSet<String> relevantUrls = extractRelevantUrls(website, mainPageContent, website);
    for (String relevantUrl : relevantUrls) {
      String subPageContent = fetchHTMLContent(driver, relevantUrl);
      emails.addAll(extractEmails(website, subPageContent));
    }

    System.out.println("Gefundene Emails:");
    for (String email : emails) {
      System.out.println(email);
    }*/

    //TODO: FIND MAIL
    return ActionResult.success(buildInformation("test@test.com"));
  }

  /*private HashSet<String> extractEmails(String website, String content) {
    var emails = new HashSet<>();
    var components = website.toLowerCase().replace("https://", "")
      .replace("http://", "").split("\\.");
    Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+(?:\\s*(?:at|@|\\(at\\))\\s*)" + components[components.length - 2] + "\\.[a-zA-Z]{2,}");
    Matcher matcher = emailPattern.matcher(content.toLowerCase());
    while (matcher.find()) {
      emails.add(matcher.group().replaceAll("\\(at\\)", "@").replaceAll("at", "@").replaceAll(" ", ""));
    }
    return emails;
  }

  // Relevante URLs extrahieren
  private HashSet<String> extractRelevantUrls(
    String website, String content, String baseUrl
  ) {
    HashSet<String> urls = new HashSet<>();
    Pattern urlPattern = Pattern.compile("href=\"(.*?)\"");
    Matcher matcher = urlPattern.matcher(content);
    while (matcher.find()) {
      String url = matcher.group(1);
      if (url.contains("privacy") || url.contains("policy") || url.contains("imprint") || url.contains("datenschutz") || url.contains("impressum") || url.contains("contact") || url.contains("kontakt") || url.contains("support")) {
        if (!url.startsWith("http")) {
          // Relative URL an die Basis-URL anhÃ¤ngen
          url = baseUrl + (url.startsWith("/") ? "" : "/") + url;
        }
        if (!url.contains(website.replaceAll("https://", "").replace("http://", ""))) {
          continue;
        }
        urls.add(url);
      }
    }
    return urls;
  }*/

  private Map<String, Object> buildInformation(String mail) {
    var information = Maps.<String, Object>newHashMap();
    information.put("mail", mail);
    information.put("website", website);
    return information;
  }
}
