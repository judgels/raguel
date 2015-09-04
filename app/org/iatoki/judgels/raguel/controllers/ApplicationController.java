package org.iatoki.judgels.raguel.controllers;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.raguel.RaguelUtils;
import org.iatoki.judgels.raguel.User;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.raguel.services.UserService;
import org.iatoki.judgels.raguel.services.impls.JidCacheServiceImpl;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
@Named
public final class ApplicationController extends AbstractJudgelsController {

    private final Jophiel jophiel;
    private final UserService userService;

    @Inject
    public ApplicationController(Jophiel jophiel, UserService userService) {
        this.jophiel = jophiel;
        this.userService = userService;
    }

    public Result index() {
        return redirect(routes.ForumController.index());
    }

    public Result auth(String returnUri) {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(returnUri);
        } else if (session().containsKey("username")) {
            return redirect(routes.ApplicationController.authRole(returnUri));
        } else {
            String newReturnUri = routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.login(newReturnUri));
        }
    }

    @Transactional
    public Result authRole(String returnUri) {
        if (session().containsKey("username") && session().containsKey("role")) {
            return redirect(returnUri);
        }

        String userRoleJid = IdentityUtils.getUserJid();
        if (!userService.existsByUserJid(userRoleJid)) {
            userService.createUser(userRoleJid, RaguelUtils.getDefaultRoles());
            RaguelUtils.saveRolesInSession(RaguelUtils.getDefaultRoles());
            return redirect(returnUri);
        }

        User userRole = userService.findUserByJid(userRoleJid);
        RaguelUtils.saveRolesInSession(userRole.getRoles());
        return redirect(returnUri);
    }

    @Transactional
    public Result afterLogin(String returnUri) {
        if (!session().containsKey("role")) {
            String newReturnUri = routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(newReturnUri));
        }

        JudgelsPlayUtils.updateUserJidCache(JidCacheServiceImpl.getInstance());
        Jophiel.updateUserAvatarCache(AvatarCacheServiceImpl.getInstance());

        if (JudgelsPlayUtils.hasViewPoint()) {
            try {
                RaguelUtils.backupSession();
                RaguelUtils.setUserSession(jophiel.getPublicUserByJid(JudgelsPlayUtils.getViewPoint()), userService.findUserByJid(JudgelsPlayUtils.getViewPoint()));
            } catch (IOException e) {
                JudgelsPlayUtils.removeViewPoint();
                RaguelUtils.restoreSession();
            }
        }
        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postViewAs() {
        Form<ViewpointForm> viewpointForm = Form.form(ViewpointForm.class).bindFromRequest();

        if (!formHasErrors(viewpointForm) && RaguelUtils.trullyHasRole("admin")) {
            ViewpointForm viewpointData = viewpointForm.get();
            try {
                String userJid = jophiel.verifyUsername(viewpointData.username);
                if (userJid != null) {
                    try {
                        userService.upsertUserFromJophielUserJid(userJid);
                        if (!JudgelsPlayUtils.hasViewPoint()) {
                            RaguelUtils.backupSession();
                        }
                        JudgelsPlayUtils.setViewPointInSession(userJid);
                        RaguelUtils.setUserSession(jophiel.getPublicUserByJid(userJid), userService.findUserByJid(userJid));

                        RaguelControllerUtils.getInstance().addActivityLog("View as user " + viewpointData.username + ".");

                    } catch (IOException e) {
                        JudgelsPlayUtils.removeViewPoint();
                        RaguelUtils.restoreSession();
                    }
                }
            } catch (IOException e) {
                // do nothing
                e.printStackTrace();
            }
        }
        return redirectToReferer();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        JudgelsPlayUtils.removeViewPoint();
        RaguelUtils.restoreSession();

        return redirectToReferer();
    }
}
