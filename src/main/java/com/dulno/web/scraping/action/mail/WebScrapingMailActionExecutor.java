package com.dulno.web.scraping.action.mail;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import com.dulno.web.scraping.WebScrapingConfiguration;
import com.dulno.web.scraping.browser.WebScrapingBrowserPool;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class WebScrapingMailActionExecutor implements ActionExecutor {
  public static WebScrapingMailActionExecutor create(
    WebScrapingBrowserPool webScrapingBrowserPool,
    WebScrapingConfiguration webScrapingConfiguration, String website
  ) {
    return new WebScrapingMailActionExecutor(webScrapingBrowserPool,
      webScrapingConfiguration, website);
  }

  private final WebScrapingBrowserPool webScrapingBrowserPool;
  private final WebScrapingConfiguration webScrapingConfiguration;
  private String website;
  private String domain;

  private WebScrapingMailActionExecutor(
    WebScrapingBrowserPool webScrapingBrowserPool,
    WebScrapingConfiguration webScrapingConfiguration, String website
  ) {
    this.webScrapingBrowserPool = webScrapingBrowserPool;
    this.webScrapingConfiguration = webScrapingConfiguration;
    this.website = website;
  }

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    website = dissolve.dissolve(website);
    website = website.toLowerCase().replaceAll("(https?://[^/]+).*", "$1");
    if (!website.startsWith("http")) {
      website = "https://" + website;
    }
    domain = website.replaceAll("https?://(?:[^/]+\\.)?([^/]+\\.[a-z]+)(?:/.*)?",
      "$1").replaceAll("^(.*?)\\..*$", "$1");
    return webScrapingBrowserPool.fetchHtml(website)
      .thenCompose(content -> searchForEmail(content, extractRedirectUrls(content))
        .thenApply(email -> ActionResult.success(buildInformation(email))));
  }

  private CompletableFuture<String> searchForEmail(
    String pageContent, List<String> redirects
  ) {
    var emails = extractEmails(pageContent);
    if (!emails.isEmpty()) {
      return CompletableFuture.completedFuture(findMostRelevantEmail(emails));
    }
    if (redirects.isEmpty()) {
      return CompletableFuture.completedFuture("");
    }
    var redirect = redirects.getFirst();
    redirects.remove(redirect);
    return webScrapingBrowserPool.fetchHtml(redirect).thenCompose(
      redirectPageContent -> searchForEmail(redirectPageContent, redirects));
  }

  private String findMostRelevantEmail(HashSet<String> emails) {
    var emailPrefixes = emails.stream()
      .map(email -> email.split("@")[0]).collect(Collectors.toSet());
    var highestRated = rateSet(webScrapingConfiguration.emailPrefixes(),
      emailPrefixes).getLast();
    return emails.stream().filter(email -> email.startsWith(highestRated))
      .findFirst().get();
  }

  private HashSet<String> extractEmails(String content) {
    var emails = new HashSet<String>();
    var emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+" +
      "(?:\\s*(?:at|@|\\(at\\))\\s*)" + domain + "\\.[a-zA-Z]{2,}");
    var matcher = emailPattern.matcher(content.toLowerCase());
    while (matcher.find()) {
      emails.add(matcher.group().replaceAll("\\(at\\)", "@")
        .replaceAll("at", "@").replaceAll(" ", ""));
    }
    return emails;
  }

  private static final int MAX_REDIRECTS = 10;
  private static final int MAX_REDIRECT_CHECKS = 500;

  private List<String> extractRedirectUrls(String content) {
    var urls = new HashSet<String>();
    var urlPattern = Pattern.compile("href=\"(.*?)\"");
    var matcher = urlPattern.matcher(content);
    var checks = 0;
    while (matcher.find() && checks < MAX_REDIRECT_CHECKS && urls.size() < MAX_REDIRECTS) {
      var entry = matcher.group(1);
      checkRedirectUrl(entry).ifPresent(urls::add);
      checks++;
    }
    return rateSet(webScrapingConfiguration.emailRedirects(), urls);
  }

  private Optional<String> checkRedirectUrl(String url) {
    if (webScrapingConfiguration.emailRedirects().stream().noneMatch(url::contains)) {
      return Optional.empty();
    }
    if (!url.startsWith("http")) {
      url = website + (url.startsWith("/") ? "" : "/") + url;
    }
    if (!url.contains(domain)) {
      return Optional.empty();
    }
    return Optional.of(url);
  }

  private List<String> rateSet(List<String> criteria, Set<String> evaluationSet) {
    return evaluationSet.stream()
      .map(element -> new AbstractMap.SimpleEntry<>(element,
        calculateScore(element, criteria)))
      .sorted((entry1, entry2) -> Double.compare(entry2.getValue(),
        entry1.getValue()))
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
  }

  private double calculateScore(String element, List<String> criteria) {
    var punkte = 0D;
    for (var i = 0; i < criteria.size(); i++) {
      var criterion = criteria.get(i);
      if (element.toLowerCase().contains(criterion.toLowerCase())) {
        punkte += (criteria.size() - i);
      }
      if (element.equalsIgnoreCase(criterion)) {
        punkte += 0.5;
      }
    }
    return punkte;
  }

  private Map<String, Object> buildInformation(String mail) {
    var information = Maps.<String, Object>newHashMap();
    information.put("mail", mail);
    information.put("website", website);
    return information;
  }
}
