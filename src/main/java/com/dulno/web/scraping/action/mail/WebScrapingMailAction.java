package com.dulno.web.scraping.action.mail;

import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class WebScrapingMailAction implements Action<WebScrapingMailActionExecutor> {
  public static WebScrapingMailAction create(
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("website", DatabaseDataType.TEXT));
    return new WebScrapingMailAction(ActionContentDatabaseTable.create(
      databaseConnection, databaseKeyspace, "action_web_scraping_mail",
      contentColumns));
  }

  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "web-scraping-mail-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("web.scraping.action.mail.name")
      .withDescription("web.scraping.action.mail.description")
      .withInputVariable(InputComponentVariable.createRequired("web.scraping.action.mail.input.website.name",
        "website", "web.scraping.action.mail.input.website.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("web.scraping.action.mail.output.mail", "mail"))
      .withOutputVariable(OutputComponentVariable.create("web.scraping.action.mail.output.website", "website"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId,
      DatabaseRow.of(content.get("website")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID triggerId) {
    return contentDatabaseTable.findContent(triggerId).thenApply(row ->
      Map.of("website", row.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<WebScrapingMailActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(content ->
      WebScrapingMailActionExecutor.create(content.findCell(1).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}
