package org.iatoki.judgels.raguel.controllers.securities;

import org.iatoki.judgels.jophiel.controllers.securities.BaseLoggedIn;
import play.mvc.Call;
import play.mvc.Http;

public class LoggedIn extends BaseLoggedIn {

    @Override
    public Call getRedirectCall(Http.Context context) {
        context.session().remove("role");
        return org.iatoki.judgels.raguel.routes.ApplicationController.auth(context.request().uri());
    }

    @Override
    public void removeAuthenticationSessions(Http.Context context) {
        context.session().remove("username");
        context.session().remove("role");
    }
}
