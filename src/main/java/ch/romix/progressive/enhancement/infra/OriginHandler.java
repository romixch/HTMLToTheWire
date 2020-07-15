package ch.romix.progressive.enhancement.infra;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OriginHandler {
  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OriginHandler.class);

  @Inject
  Vertx vertx;

  public void init(@Observes Router router) {
    router.route().order(1).handler(rc -> {
      String uri = rc.request().absoluteURI();
      UriBuilder builder = UriBuilder.fromUri(uri);
      String forwardProto = rc.request().getHeader("x-forwarded-proto");
      if (forwardProto != null) {
        builder.scheme(forwardProto);
      }
      rc.data().put("origin", builder.build());
      rc.next();
    });
  }

  public static URI getOrigin(RoutingContext rc) {
    return (URI)rc.data().get("origin");
  }
}
