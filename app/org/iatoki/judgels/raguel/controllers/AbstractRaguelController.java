package org.iatoki.judgels.raguel.controllers;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.UserActivityMessage;
import org.iatoki.judgels.jophiel.controllers.AbstractBaseJophielClientController;
import org.iatoki.judgels.jophiel.forms.UserProfileSearchForm;
import org.iatoki.judgels.jophiel.forms.ViewpointForm;
import org.iatoki.judgels.jophiel.services.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.services.UserActivityMessageService;
import org.iatoki.judgels.jophiel.views.html.script.isLoggedIn;
import org.iatoki.judgels.jophiel.views.html.script.isLoggedOut;
import org.iatoki.judgels.jophiel.views.html.sidebar.linkedClientsView;
import org.iatoki.judgels.jophiel.views.html.sidebar.userProfileSearchView;
import org.iatoki.judgels.jophiel.views.html.sidebar.userProfileView;
import org.iatoki.judgels.jophiel.views.html.viewas.viewAs;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import org.iatoki.judgels.play.jid.BaseJidCacheService;
import org.iatoki.judgels.play.views.html.sidebar.guestView;
import org.iatoki.judgels.raguel.RaguelUtils;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;

public abstract class AbstractRaguelController extends AbstractBaseJophielClientController {

    private final JophielClientAPI jophielClientAPI;
    private final JophielPublicAPI jophielPublicAPI;
    private final UserActivityMessageService userActivityMessageService;

    protected AbstractRaguelController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService) {
        super(jidCacheService, avatarCacheService);
        this.jophielClientAPI = jophielClientAPI;
        this.jophielPublicAPI = jophielPublicAPI;
        this.userActivityMessageService = userActivityMessageService;
    }

    protected boolean isCurrentUserModerator() {
        return currentUserHasRole("moderator");
    }

    protected boolean isCurrentUserModeratorOrAdmin() {
        return isCurrentUserAdmin() || isCurrentUserModerator();
    }

    protected void addActivityLog(String log) {
        if (!isCurrentUserGuest()) {
            String newLog = log;
            try {
                if (JudgelsPlayUtils.hasViewPoint()) {
                    newLog += " view as " + IdentityUtils.getUserJid();
                }
                userActivityMessageService.addUserActivityMessage(new UserActivityMessage(System.currentTimeMillis(), RaguelUtils.getRealUserJid(), newLog, IdentityUtils.getIpAddress()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        String linkedClientsAPIEndpoint = jophielClientAPI.getLinkedClientsAPIEndpoint();
        Html linkedClientsWidget = linkedClientsView.render(linkedClientsAPIEndpoint);
        htmlTemplate.addLowerSidebarWidget(linkedClientsWidget);

        if (isCurrentUserGuest()) {
            String registerUrl = jophielClientAPI.getRegisterEndpoint();
            String loginUrl = getAbsoluteUrl(org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.login(ControllerUtils.getCurrentUrl(request())));
            Html guestWidget = guestView.render(registerUrl, loginUrl);
            htmlTemplate.addUpperSidebarWidget(guestWidget);
            Html isLoggedInScript = isLoggedIn.render(jophielClientAPI.getUserIsLoggedInAPIEndpoint(), getAbsoluteUrl(routes.ApplicationController.auth(ControllerUtils.getCurrentUrl(Http.Context.current().request()))), "lib/jophielcommons/javascripts/isLoggedIn.js");
            htmlTemplate.addAdditionalScript(isLoggedInScript);
        } else {
            String editProfileUrl = jophielClientAPI.getUserEditProfileEndpoint();
            String logoutUrl = getAbsoluteUrl(org.iatoki.judgels.jophiel.controllers.routes.JophielClientController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request())));

            Html userProfileWidget = userProfileView.render(getCurrentUsername(), getCurrentUserRealName(), getCurrentUserAvatarUrl(), editProfileUrl, logoutUrl);
            htmlTemplate.addUpperSidebarWidget(userProfileWidget);

            htmlTemplate.addSidebarMenu(Messages.get("forum.text.forums"), routes.ForumController.index());

            Html isLoggedOutScript = isLoggedOut.render(jophielClientAPI.getUserIsLoggedInAPIEndpoint(), getAbsoluteUrl(routes.ApplicationController.logout(ControllerUtils.getCurrentUrl(Http.Context.current().request()))), "lib/jophielcommons/javascripts/isLoggedOut.js", RaguelUtils.getRealUserJid());
            htmlTemplate.addAdditionalScript(isLoggedOutScript);

            if (isCurrentUserAdmin()) {
                htmlTemplate.addSidebarMenu(Messages.get("user.text.users"), routes.UserController.index());

                Form<ViewpointForm> form = Form.form(ViewpointForm.class);
                if (JudgelsPlayUtils.hasViewPoint()) {
                    ViewpointForm viewpointForm = new ViewpointForm();
                    viewpointForm.username = IdentityUtils.getUsername();
                    form.fill(viewpointForm);
                }
                Html viewAsWidget = viewAs.render(form, jophielPublicAPI.getUserAutocompleteAPIEndpoint(), "lib/jophielcommons/javascripts/userAutoComplete.js", routes.ApplicationController.postViewAs(), routes.ApplicationController.resetViewAs());
                htmlTemplate.addLowerSidebarWidget(viewAsWidget);
            }
        }

        Form<UserProfileSearchForm> userProfileSearchForm = Form.form(UserProfileSearchForm.class);
        String autocompleteUserAPIEndpoint = jophielPublicAPI.getUserAutocompleteAPIEndpoint();
        String postSearchUserProfileUrl = jophielClientAPI.getUserSearchProfileEndpoint();
        Html userProfileSearchWidget =  userProfileSearchView.render(userProfileSearchForm, autocompleteUserAPIEndpoint, postSearchUserProfileUrl);
        htmlTemplate.addLowerSidebarWidget(userProfileSearchWidget);

        return htmlTemplate;
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        template.reverseBreadcrumbLocations();

        return super.renderTemplate(template);
    }
}
