package ch.romix.mpa.web.time;

import ch.romix.mpa.domain.UserValueObject;
import ch.romix.mpa.domain.time.DayAggregate;
import ch.romix.mpa.domain.time.EntryEntity;
import ch.romix.mpa.domain.time.EntryEntity.TimeType;
import ch.romix.mpa.domain.time.EntryRepository;
import ch.romix.mpa.domain.time.EntryService;
import ch.romix.mpa.infra.OriginHandler;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import io.quarkus.vertx.web.Route;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.apache.http.client.utils.URIBuilder;

@Singleton
public class TimeRoute {

  @Inject
  @ResourcePath("time/index.html")
  Template index;

  @Inject
  Validator validator;

  @Inject
  EntryRepository entryRepository;

  @Route(path = "/time", methods = HttpMethod.GET)
  public void index(RoutingContext rc) throws URISyntaxException {
    try {
      UserValueObject user = rc.session().get("user");
      Collection<EntryEntity> entries = entryRepository.findByUser(rc.session().get("user"));
      Collection<DayAggregate> dailyAggregates = EntryService.aggregateByDate(entries);
      Duration totalWorktime = EntryService.total(TimeType.WORKTIME, entries);
      Duration totalSparetime = EntryService.total(TimeType.SPARETIME, entries);
      StatisticsTemplateData statisticsTemplateData = new StatisticsTemplateData(totalWorktime,
          totalSparetime);
      rc.response().end(index
          .data("user", user)
          .data("dailyAggregates", dailyAggregates)
          .data("currentDay", LocalDate.now())
          .data("stats", statisticsTemplateData)
          .render());
    } catch (ClassCastException ex) {
      // Just for development time
      // Login again in case UserValueObject can't be cast because of a different ClassLoader
      redirectTo(rc, "/auth/login");
    }
  }

  @Route(path = "/time/add", methods = HttpMethod.POST)
  public void postForm(RoutingContext rc) throws URISyntaxException {
    try {
    TimeAddPostData data = new TimeAddPostData(rc.request().formAttributes());
      Set<ConstraintViolation<TimeAddPostData>> violations = validator.validate(data);
    if (violations.isEmpty()) {
      UserValueObject user = rc.session().get("user");
      EntryEntity entryEntity = new EntryEntity(UUID.randomUUID().toString(), user.getName(),
          data.parsedDay, data.parsedStart, data.parsedEnd, data.timeType);
      entryRepository.add(entryEntity);
    }
    redirectTo(rc, "/time");
    } catch (ClassCastException ex) {
      // Just for development time
      // Login again in case UserValueObject can't be cast because of a different ClassLoader
      redirectTo(rc, "/auth/login");
    }
  }

  private void redirectTo(RoutingContext rc, String s) throws URISyntaxException {
    URI origin = OriginHandler.getOrigin(rc);
    URI uri = new URIBuilder(origin).setPath(s).build();
    rc.response().putHeader("location", uri.toString()).setStatusCode(302).end();
  }
}
