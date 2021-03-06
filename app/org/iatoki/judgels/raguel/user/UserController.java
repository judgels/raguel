package org.iatoki.judgels.raguel.user;

import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.api.JudgelsAPIClientException;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.avatar.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.activity.UserActivityMessageService;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.jid.BaseJidCacheService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.raguel.AbstractRaguelController;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.Authorized;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.jid.JidCacheServiceImpl;
import org.iatoki.judgels.raguel.user.html.createUserView;
import org.iatoki.judgels.raguel.user.html.listUsersView;
import org.iatoki.judgels.raguel.user.html.updateUserView;
import org.iatoki.judgels.raguel.user.html.viewUserView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;
import javax.inject.Singleton;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
public final class UserController extends AbstractRaguelController {

    private static final long PAGE_SIZE = 20;

    private final JophielPublicAPI jophielPublicAPI;
    private final UserService userService;

    @Inject
    public UserController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, UserService userService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService);
        this.jophielPublicAPI = jophielPublicAPI;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Transactional(readOnly = true)
    public Result listUsers(long pageIndex, String sortBy, String orderBy, String filterString) {
        Page<User> pageOfUsers = userService.getPageOfUsers(pageIndex, PAGE_SIZE, sortBy, orderBy, filterString);

        HtmlTemplate htmlTemplate = getBaseHtmlTemplate();

        Html content = listUsersView.render(pageOfUsers, sortBy, orderBy, filterString);
        htmlTemplate.setContent(content);
        htmlTemplate.setMainTitle(Messages.get("commons.text.list1", Messages.get("user.text.user")));

        htmlTemplate.addMainButton(Messages.get("commons.button.new1", Messages.get("user.text.user")), routes.UserController.createUser());

        addActivityLog("List all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return renderTemplate(htmlTemplate);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createUser() {
        UserCreateForm userCreateData = new UserCreateForm();
        userCreateData.roles = StringUtils.join(getDefaultUserRoles(), ",");
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).fill(userCreateData);

        addActivityLog("Try to create user <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateUser(userCreateForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postCreateUser() {
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).bindFromRequest();

        if (formHasErrors(userCreateForm)) {
            return showCreateUser(userCreateForm);
        }

        UserCreateForm userCreateData = userCreateForm.get();
        JophielUser jophielUser;
        try {
            jophielUser = jophielPublicAPI.findUserByUsername(userCreateData.username);
        } catch (JudgelsAPIClientException e) {
            jophielUser = null;
        }

        if (jophielUser == null) {
            userCreateForm.reject(Messages.get("user.new.error.invalid"));
            return showCreateUser(userCreateForm);
        }

        if (userService.existsByUserJid(jophielUser.getJid())) {
            userCreateForm.reject(Messages.get("user.new.error.registered"));
            return showCreateUser(userCreateForm);
        }

        userService.upsertUserFromJophielUser(jophielUser, userCreateData.getRolesAsList(), getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog("Create user " + jophielUser.getJid() + ".");

        return redirect(routes.UserController.index());
    }

    @Transactional(readOnly = true)
    public Result viewUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);

        HtmlTemplate htmlTemplate = getBaseHtmlTemplate();

        Html content = viewUserView.render(user);
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(Messages.get("user.text.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()));
        htmlTemplate.addMainButton(Messages.get("commons.button.edit"), routes.UserController.updateUser(user.getId()));

        htmlTemplate.markBreadcrumbLocation(Messages.get("commons.text.view"), routes.UserController.viewUser(user.getId()));

        addActivityLog("View user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return renderTemplate(htmlTemplate);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        UserUpdateForm userUpdateData = new UserUpdateForm();
        userUpdateData.roles = StringUtils.join(user.getRoles(), ",");
        Form<UserUpdateForm> userUpdateForm = Form.form(UserUpdateForm.class).fill(userUpdateData);

        addActivityLog("Try to update user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateUser(userUpdateForm, user);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUpdateUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        Form<UserUpdateForm> userUpdateForm = Form.form(UserUpdateForm.class).bindFromRequest();

        if (formHasErrors(userUpdateForm)) {
            return showUpdateUser(userUpdateForm, user);
        }

        UserUpdateForm userUpdateData = userUpdateForm.get();
        userService.updateUser(user.getId(), userUpdateData.getRolesAsList(), getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog("Update user " + user.getUserJid() + ".");

        return redirect(routes.UserController.index());
    }

    @Transactional
    public Result deleteUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        userService.deleteUser(user.getId());

        addActivityLog("Delete user " + user.getUserJid() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        htmlTemplate.markBreadcrumbLocation(Messages.get("user.text.users"), routes.UserController.index());
        return htmlTemplate;
    }

    private Result showCreateUser(Form<UserCreateForm> userCreateForm) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate();

        Html content = createUserView.render(userCreateForm, jophielPublicAPI.getUserAutocompleteAPIEndpoint());
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(Messages.get("commons.text.new1", Messages.get("user.text.user")));
        htmlTemplate.markBreadcrumbLocation(Messages.get("commons.text.new"), routes.UserController.createUser());

        return renderTemplate(htmlTemplate);
    }

    private Result showUpdateUser(Form<UserUpdateForm> userUpdateForm, User user) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate();

        Html content = updateUserView.render(userUpdateForm, user.getId());
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(Messages.get("user.text.user") + " #" + user.getId() + ": " + JidCacheServiceImpl.getInstance().getDisplayName(user.getUserJid()));
        htmlTemplate.markBreadcrumbLocation(Messages.get("commons.text.edit"), routes.UserController.updateUser(user.getId()));

        return renderTemplate(htmlTemplate);
    }
}
