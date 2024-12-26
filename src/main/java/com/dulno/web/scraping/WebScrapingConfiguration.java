package com.dulno.web.scraping;

import com.dulno.core.configuration.Configuration;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class WebScrapingConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/web-scraping/web-scraping.json";

  public static WebScrapingConfiguration createAndLoad() throws Exception {
    var configuration = new WebScrapingConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private List<String> emailRedirects;
  private List<String> emailPrefixes;

  private WebScrapingConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    emailRedirects = json.getJSONArray("emailRedirects")
      .toList().stream().map(String::valueOf).toList();
    emailPrefixes = json.getJSONArray("emailPrefixes")
      .toList().stream().map(String::valueOf).toList();
  }
}

