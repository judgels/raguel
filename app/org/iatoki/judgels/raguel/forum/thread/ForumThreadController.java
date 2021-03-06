package org.iatoki.judgels.raguel.forum.thread;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.avatar.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.activity.UserActivityMessageService;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.jid.BaseJidCacheService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.raguel.forum.AbstractForumController;
import org.iatoki.judgels.raguel.forum.Forum;
import org.iatoki.judgels.raguel.forum.ForumNotFoundException;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.Authorized;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.forum.module.ForumModules;
import org.iatoki.judgels.raguel.forum.member.ForumMemberService;
import org.iatoki.judgels.raguel.forum.ForumService;
import org.iatoki.judgels.raguel.forum.thread.post.ThreadPostService;
import org.iatoki.judgels.raguel.forum.thread.html.createForumThreadView;
import org.iatoki.judgels.raguel.forum.thread.html.listThreadsView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;

public final class ForumThreadController extends AbstractForumController {

    private static final long PAGE_SIZE = 20;

    private final ForumService forumService;
    private final ForumThreadService forumThreadService;
    private final ThreadPostService threadPostService;

    @Inject
    public ForumThreadController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, ForumMemberService forumMemberService, ForumService forumService, ForumThreadService forumThreadService, ThreadPostService threadPostService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService, forumMemberService);
        this.forumService = forumService;
        this.forumThreadService = forumThreadService;
        this.threadPostService = threadPostService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    public Result viewThreads(long forumId) throws ForumNotFoundException {
        return listThreads(forumId, 0, "timeUpdate", "desc", "");
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    public Result listThreads(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        return showListForumsThreads(forum, pageIndex, orderBy, orderDir, filterString);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createForumThread(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (forum.containOrInheritModule(ForumModules.EXCLUSIVE) && !isCurrentUserModeratorOrAdmin()) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        Form<ForumThreadCreateForm> forumThreadCreateForm = Form.form(ForumThreadCreateForm.class);

        return showCreateForumThread(forum, forumThreadCreateForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postCreateForumThread(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (forum.containOrInheritModule(ForumModules.EXCLUSIVE) && !isCurrentUserModeratorOrAdmin()) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        Form<ForumThreadCreateForm> forumThreadCreateForm = Form.form(ForumThreadCreateForm.class).bindFromRequest();

        if (formHasErrors(forumThreadCreateForm)) {
            return showCreateForumThread(forum, forumThreadCreateForm);
        }

        ForumThreadCreateForm forumThreadCreateData = forumThreadCreateForm.get();
        ForumThread forumThread = forumThreadService.createForumThread(forum, forumThreadCreateData.name, getCurrentUserJid(), getCurrentUserIpAddress());
        threadPostService.createPost(forumThread, getCurrentUserJid(), forumThreadCreateData.name, forumThreadCreateData.content, getCurrentUserIpAddress());

        return redirect(org.iatoki.judgels.raguel.forum.thread.post.routes.ThreadPostController.viewThreadPosts(forumThread.getId()));
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate(Forum forum) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate(forum);

        htmlTemplate.markBreadcrumbLocation(Messages.get("forum.text.threads"), routes.ForumThreadController.viewThreads(forum.getId()));
        return htmlTemplate;
    }

    private Result showListForumsThreads(Forum forum, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Page<ForumThread> pageOfForumThreads = forumThreadService.getPageOfForumThreads(forum, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        Html content = listThreadsView.render(forum, pageOfForumThreads, orderBy, orderDir, filterString);

        htmlTemplate.setContent(content);

        return renderTemplate(htmlTemplate);
    }

    private Result showCreateForumThread(Forum forum, Form<ForumThreadCreateForm> forumThreadCreateForm) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Html content = createForumThreadView.render(forum, forumThreadCreateForm);

        htmlTemplate.setContent(content);

        return renderTemplate(htmlTemplate);
    }
}
