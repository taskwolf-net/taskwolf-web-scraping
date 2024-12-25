package com.dulno.web.scraping;

import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.web.scraping.action.mail.WebScrapingMailAction;
import com.google.inject.Injector;

@ModuleDescription(name = "web-scraping", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class WebScrapingModule extends Module {
  private Log log;
  private AccountLink accountLink;

  public WebScrapingModule(Injector injector) {
    super(injector.createChildInjector(WebScrapingInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("Web Scraping");
    accountLink = WebScrapingAccountLink.create();
  }

  @Override
  public void disable() {

  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("Web Scraping", "", "web-scraping.webp",
      ModuleInformation.Type.PUBLIC, ModuleInformation.Novelty.NEW);
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = ActionRepository.create();
    repository.registerAction(WebScrapingMailAction.create(databaseConnection,
      databaseKeyspace));
    return repository;
  }
}