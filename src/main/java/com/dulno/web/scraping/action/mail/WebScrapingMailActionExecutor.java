package com.dulno.web.scraping.action.mail;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class WebScrapingMailActionExecutor implements ActionExecutor {
  private String website;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    website = dissolve.dissolve(website);
    return CompletableFuture.completedFuture(findMail());
  }

  private ActionResult findMail() {
    //TODO: FIND MAIL
    return ActionResult.success(buildInformation("test@test.com"));
  }

  private Map<String, Object> buildInformation(String mail) {
    var information = Maps.<String, Object>newHashMap();
    information.put("mail", mail);
    information.put("website", website);
    return information;
  }
}
