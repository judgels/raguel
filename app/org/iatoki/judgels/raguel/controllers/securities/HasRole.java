package org.iatoki.judgels.raguel.controllers.securities;

import org.iatoki.judgels.raguel.RaguelUtils;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class HasRole extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context context) {
        return RaguelUtils.getRolesFromSession();
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(org.iatoki.judgels.raguel.routes.ApplicationController.authRole(context.request().uri()));
    }
}
