package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.jophiel.services.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.services.UserActivityMessageService;
import org.iatoki.judgels.play.services.BaseJidCacheService;
import org.iatoki.judgels.raguel.User;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.services.UserService;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named
public final class ApplicationController extends AbstractRaguelController {

    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    @Inject
    public ApplicationController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, JophielPublicAPI jophielPublicAPI1, UserService userService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService);
        this.jophielPublicAPI = jophielPublicAPI1;
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

        String userJid = getCurrentUserJid();
        if (!userService.existsByUserJid(userJid)) {
            userService.createUser(userJid, getDefaultRoles(), getCurrentUserJid(), getCurrentUserIpAddress());
            setCurrentUserRoles(getDefaultRoles());
            return redirect(returnUri);
        }

        User userRole = userService.findUserByJid(userJid);
        setCurrentUserRoles(userRole.getRoles());

        return redirect(returnUri);
    }

    @Transactional
    public Result afterLogin(String returnUri) {
        if (!session().containsKey("role")) {
            String newReturnUri = routes.ApplicationController.afterLogin(returnUri).absoluteURL(request(), request().secure());
            return redirect(routes.ApplicationController.authRole(newReturnUri));
        }

        updateUserJidCache();
        updateUserAvatarCache();

        if (hasViewPoint()) {
            try {
                backUpCurrentUserSession();
                setCurrentUserSession(jophielPublicAPI.findUserByJid(getViewPoint()), userService.findUserByJid(getViewPoint()).getRoles());
            } catch (JudgelsAPIClientException e) {
                removeViewPoint();
                restoreBackedUpUserSession();
            }
        }
        return redirect(returnUri);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postViewAs() {
        Form<ViewpointForm> viewpointForm = Form.form(ViewpointForm.class).bindFromRequest();

        if (!formHasErrors(viewpointForm) && backedUpOrCurrentUserHasRole("admin")) {
            ViewpointForm viewpointData = viewpointForm.get();
            try {
                JophielUser jophielUser = jophielPublicAPI.findUserByUsername(viewpointData.username);
                if (jophielUser != null) {
                    userService.upsertUserFromJophielUser(jophielUser, getCurrentUserJid(), getCurrentUserIpAddress());
                    if (!hasViewPoint()) {
                        backUpCurrentUserSession();
                    }
                    setViewPointInSession(jophielUser.getJid());
                    setCurrentUserSession(jophielUser, userService.findUserByJid(jophielUser.getJid()).getRoles());

                    addActivityLog("View as user " + viewpointData.username + ".");
                }
            } catch (JudgelsAPIClientException e) {
                // do nothing
                e.printStackTrace();
            }
        }
        return redirectToReferer();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result resetViewAs() {
        removeViewPoint();
        restoreBackedUpUserSession();

        return redirectToReferer();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    public Result logout(String returnUri) {
        session().clear();
        return redirect(returnUri);
    }

    private List<String> getDefaultRoles() {
        return ImmutableList.of("user");
    }
}
