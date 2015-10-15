package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.Authorized;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.forms.ForumThreadCreateForm;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.services.ForumService;
import org.iatoki.judgels.raguel.services.ForumThreadService;
import org.iatoki.judgels.raguel.services.ThreadPostService;
import org.iatoki.judgels.raguel.views.html.forum.thread.createForumThreadView;
import org.iatoki.judgels.raguel.views.html.forum.thread.listThreadsView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Stack;

public final class ForumThreadController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ForumService forumService;
    private final ForumThreadService forumThreadService;
    private final ThreadPostService threadPostService;

    @Inject
    public ForumThreadController(ForumService forumService, ForumThreadService forumThreadService, ThreadPostService threadPostService) {
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
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        if (!ForumControllerUtils.getInstance().isAllowedToEnterForum(forum, IdentityUtils.getUserJid())) {
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        return showListForumsThreads(forum, pageIndex, orderBy, orderDir, filterString);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createForumThread(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        if (forum.containOrInheritModule(ForumModules.EXCLUSIVE) && !RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        if (!ForumControllerUtils.getInstance().isAllowedToEnterForum(forum, IdentityUtils.getUserJid())) {
            return redirect(routes.ForumController.viewForums(forum.getId()));
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
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        if (forum.containOrInheritModule(ForumModules.EXCLUSIVE) && !RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        if (!ForumControllerUtils.getInstance().isAllowedToEnterForum(forum, IdentityUtils.getUserJid())) {
            return redirect(routes.ForumController.viewForums(forum.getId()));
        }

        Form<ForumThreadCreateForm> forumThreadCreateForm = Form.form(ForumThreadCreateForm.class).bindFromRequest();

        if (formHasErrors(forumThreadCreateForm)) {
            return showCreateForumThread(forum, forumThreadCreateForm);
        }

        ForumThreadCreateForm forumThreadCreateData = forumThreadCreateForm.get();
        ForumThread forumThread = forumThreadService.createForumThread(forum, forumThreadCreateData.name, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        threadPostService.createPost(forumThread, IdentityUtils.getUserJid(), forumThreadCreateData.name, forumThreadCreateData.content, IdentityUtils.getIpAddress());

        return redirect(routes.ThreadPostController.viewThreadPosts(forumThread.getId()));
    }

    private Result showListForumsThreads(Forum forum, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Page<ForumThread> pageOfForumThreads = forumThreadService.getPageOfForumThreads(forum, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listThreadsView.render(forum, pageOfForumThreads, orderBy, orderDir, filterString));

        ForumControllerUtils.getInstance().appendTabsLayout(content, forum, IdentityUtils.getUserJid());

        RaguelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> internalLinkBuilder;
        internalLinkBuilder = ForumControllerUtils.getForumBreadcrumbsBuilder(forum.getParentForum());
        internalLinkBuilder.add(new InternalLink(Messages.get("forum.threads"), routes.ForumThreadController.viewThreads(forum.getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forums - Threads");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateForumThread(Forum forum, Form<ForumThreadCreateForm> forumThreadCreateForm) {
        LazyHtml content = new LazyHtml(createForumThreadView.render(forum, forumThreadCreateForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("forum.thread.create"), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();

        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = forum;
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }

        internalLinkBuilder.add(new InternalLink(Messages.get("forum.thread.create"), routes.ForumThreadController.createForumThread(forum.getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Create");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }
}
