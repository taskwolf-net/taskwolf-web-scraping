package com.dulno.web.scraping;

import com.dulno.core.error.ErrorRepository;
import com.dulno.web.scraping.browser.WebScrapingBrowserPool;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class WebScrapingInjectionModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  private static final int BROWSER_POOL_SIZE = 1;

  @Provides
  @Singleton
  WebScrapingBrowserPool provideWebScrapingBrowserPool(
    ErrorRepository errorRepository
  ) {
    return WebScrapingBrowserPool.of(errorRepository,BROWSER_POOL_SIZE );
  }
}
