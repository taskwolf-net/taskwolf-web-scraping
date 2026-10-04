package net.taskwolf.web.scraping;

import net.taskwolf.core.account.AccountLink;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.web.scraping.action.mail.WebScrapingMailAction;
import net.taskwolf.web.scraping.browser.WebScrapingBrowserPool;
import net.taskwolf.workflow.integration.Integration;
import com.google.inject.Injector;

@ModuleDescription(name = "web-scraping", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class WebScrapingModule extends Integration {
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
    var webScrapingBrowserPool = injector().getInstance(WebScrapingBrowserPool.class);
    var webScrapingConfiguration = injector().getInstance(WebScrapingConfiguration.class);
    var repository = ActionRepository.create();
    repository.registerAction(WebScrapingMailAction.create(webScrapingBrowserPool,
      webScrapingConfiguration, databaseConnection, databaseKeyspace));
    return repository;
  }
}