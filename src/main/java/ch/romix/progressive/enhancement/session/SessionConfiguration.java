package ch.romix.progressive.enhancement.session;

import io.vertx.core.Vertx;
import io.vertx.core.http.CookieSameSite;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class SessionConfiguration {
  @Inject
  Vertx vertx;

  public void init(@Observes Router router) {
    LocalSessionStore sessionStore = LocalSessionStore.create(this.vertx);
    SessionHandler sessionHandler = SessionHandler.create(sessionStore);
    sessionHandler.setCookieSameSite(CookieSameSite.STRICT);
    router.route().order(0).handler(sessionHandler);
  }
}
