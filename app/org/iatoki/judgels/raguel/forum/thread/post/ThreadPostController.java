package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.avatar.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.activity.UserActivityMessageService;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.jid.BaseJidCacheService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.raguel.forum.AbstractForumController;
import org.iatoki.judgels.raguel.forum.Forum;
import org.iatoki.judgels.raguel.forum.thread.ForumThread;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadNotFoundException;
import org.iatoki.judgels.raguel.user.item.UserItemStatus;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.GuestView;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.forum.module.ForumModules;
import org.iatoki.judgels.raguel.forum.member.ForumMemberService;
import org.iatoki.judgels.raguel.forum.thread.ForumThreadService;
import org.iatoki.judgels.raguel.user.item.UserItemService;
import org.iatoki.judgels.raguel.forum.thread.post.html.editThreadPostView;
import org.iatoki.judgels.raguel.forum.thread.post.html.listPostContentsView;
import org.iatoki.judgels.raguel.forum.thread.post.html.listThreadPostsTreeView;
import org.iatoki.judgels.raguel.forum.thread.post.html.listThreadPostsView;
import org.iatoki.judgels.raguel.forum.thread.post.html.replyThreadPostView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public final class ThreadPostController extends AbstractForumController {

    private static final long PAGE_SIZE = 20;

    private final ForumThreadService forumThreadService;
    private final ThreadPostService threadPostService;
    private final UserItemService userItemService;

    @Inject
    public ThreadPostController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, ForumMemberService forumMemberService, ForumThreadService forumThreadService, ThreadPostService threadPostService, UserItemService userItemService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService, forumMemberService);
        this.forumThreadService = forumThreadService;
        this.threadPostService = threadPostService;
        this.userItemService = userItemService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewThreadPosts(long forumThreadId) throws ForumThreadNotFoundException {
        return showListThreadPosts(forumThreadId, 0, "id", "asc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result listThreadPosts(long forumThreadId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumThreadNotFoundException {
        return showListThreadPosts(forumThreadId, pageIndex, orderBy, orderDir, filterString);
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewTreeThreadPosts(long forumThreadId) throws ForumThreadNotFoundException {
        ForumThread forumThread = forumThreadService.findForumThreadById(forumThreadId);

        Forum forum = forumThread.getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        List<ThreadPostWithLevel> threadPostsWithLevel = threadPostService.getAllThreadPostsWithLevel(forumThread);
        List<String> replyJids = threadPostsWithLevel.stream().map(e -> e.getThreadPost().getReplyJid()).collect(Collectors.toList());
        Map<String, Long> replyJidToIdMap = threadPostService.getThreadPostsJidToIdMap(replyJids);
        Map<String, String> replyJidToUserJidMap = threadPostService.getThreadPostsJidToUserJidMap(replyJids);

        userItemService.upsertUserItem(getCurrentUserJid(), forumThread.getJid(), UserItemStatus.VIEWED);

        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Html content = listThreadPostsTreeView.render(forumThread, threadPostsWithLevel, replyJidToIdMap, replyJidToUserJidMap);
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(forumThread.getName());
        htmlTemplate.setMainBackButton(Messages.get("commons.button.backTo1", forumThread.getParentForum().getName()), org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forumThread.getParentForum().getId()));

        htmlTemplate.markBreadcrumbLocation(forumThread.getName(), routes.ThreadPostController.viewThreadPosts(forumThread.getId()));

        return renderTemplate(htmlTemplate);
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewPostVersions(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        Forum forum = threadPost.getThread().getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Html content = listPostContentsView.render(threadPost);
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(Messages.get("forum.thread.post.text.versions"));
        htmlTemplate.setMainBackButton(Messages.get("commons.button.backTo1", threadPost.getThread().getName()), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));

        htmlTemplate.markBreadcrumbLocation(threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));

        return renderTemplate(htmlTemplate);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editThreadPost(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        Forum forum = threadPost.getThread().getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!getCurrentUserJid().equals(threadPost.getUserJid())) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        ThreadPostUpsertForm threadPostUpsertData = new ThreadPostUpsertForm();
        threadPostUpsertData.subject = threadPost.getLatestContent().getSubject();
        threadPostUpsertData.content = threadPost.getLatestContent().getContent();

        Form<ThreadPostUpsertForm> threadPostUpsertForm = Form.form(ThreadPostUpsertForm.class).fill(threadPostUpsertData);

        return showEditThreadPost(threadPost, threadPostUpsertForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postEditThreadPost(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        Forum forum = threadPost.getThread().getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!getCurrentUserJid().equals(threadPost.getUserJid())) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        Form<ThreadPostUpsertForm> threadPostUpsertForm = Form.form(ThreadPostUpsertForm.class).bindFromRequest();

        if (formHasErrors(threadPostUpsertForm)) {
            return showEditThreadPost(threadPost, threadPostUpsertForm);
        }

        ThreadPostUpsertForm threadPostUpsertData = threadPostUpsertForm.get();
        threadPostService.editPost(threadPost, getCurrentUserJid(), threadPostUpsertData.subject, threadPostUpsertData.content, getCurrentUserIpAddress());

        return redirect(routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result replyThreadPost(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        Forum forum = threadPost.getThread().getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        ThreadPostUpsertForm threadPostUpsertData = new ThreadPostUpsertForm();

        if (threadPost.getReplyJid() == null) {
            threadPostUpsertData.subject = "Re: " + threadPost.getLatestContent().getSubject();
        } else {
            threadPostUpsertData.subject = threadPost.getLatestContent().getSubject();
        }

        Form<ThreadPostUpsertForm> threadPostUpsertForm = Form.form(ThreadPostUpsertForm.class).fill(threadPostUpsertData);

        return showReplyThreadPost(threadPost, threadPostUpsertForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postReplyThreadPost(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        Forum forum = threadPost.getThread().getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        Form<ThreadPostUpsertForm> threadPostUpsertForm = Form.form(ThreadPostUpsertForm.class).bindFromRequest();

        if (formHasErrors(threadPostUpsertForm)) {
            return showReplyThreadPost(threadPost, threadPostUpsertForm);
        }

        ThreadPostUpsertForm threadPostUpsertData = threadPostUpsertForm.get();
        threadPostService.replyPost(threadPost, getCurrentUserJid(), threadPostUpsertData.subject, threadPostUpsertData.content, getCurrentUserIpAddress());

        long totalPost = threadPostService.countThreadPost(threadPost.getThread());

        return redirect(routes.ThreadPostController.listThreadPosts(threadPost.getThread().getId(), ((totalPost - 1) / PAGE_SIZE), "id", "asc", ""));
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate(Forum forum) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();
        markForumBreadcrumbs(htmlTemplate, forum);

        return htmlTemplate;
    }

    private Result showListThreadPosts(long forumThreadId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumThreadNotFoundException {
        ForumThread forumThread = forumThreadService.findForumThreadById(forumThreadId);

        Forum forum = forumThread.getParentForum();
        if (!forum.containModule(ForumModules.THREAD)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        if (!isCurrentUserAllowedToEnterForum(forum)) {
            return redirect(org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forum.getId()));
        }

        Page<ThreadPost> pageOfThreadPosts = threadPostService.getPageOfThreadPosts(forumThread, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        List<String> replyJids = pageOfThreadPosts.getData().stream().map(e -> e.getReplyJid()).collect(Collectors.toList());
        Map<String, Long> replyJidToIdMap = threadPostService.getThreadPostsJidToIdMap(replyJids);
        Map<String, String> replyJidToUserJidMap = threadPostService.getThreadPostsJidToUserJidMap(replyJids);

        userItemService.upsertUserItem(getCurrentUserJid(), forumThread.getJid(), UserItemStatus.VIEWED);

        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(forum);

        Html content = listThreadPostsView.render(forumThread, pageOfThreadPosts, replyJidToIdMap, replyJidToUserJidMap, orderBy, orderDir, filterString);
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(forumThread.getName());

        htmlTemplate.setMainBackButton(Messages.get("commons.button.backTo1", forumThread.getParentForum().getName()), org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(forumThread.getParentForum().getId()));

        htmlTemplate.markBreadcrumbLocation(forumThread.getName(), routes.ThreadPostController.viewThreadPosts(forumThread.getId()));

        return renderTemplate(htmlTemplate);
    }

    private Result showEditThreadPost(ThreadPost threadPost, Form<ThreadPostUpsertForm> threadPostUpsertForm) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(threadPost.getThread().getParentForum());

        Html content = editThreadPostView.render(threadPost, threadPostUpsertForm);
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(Messages.get("commons.button.edit"));
        htmlTemplate.setMainBackButton(Messages.get("commons.button.backTo1", threadPost.getThread().getParentForum().getName()), org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(threadPost.getThread().getParentForum().getId()));

        htmlTemplate.markBreadcrumbLocation(threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));
        htmlTemplate.markBreadcrumbLocation(Messages.get("commons.button.edit"), routes.ThreadPostController.editThreadPost(threadPost.getId()));

        return renderTemplate(htmlTemplate);
    }

    private Result showReplyThreadPost(ThreadPost threadPost, Form<ThreadPostUpsertForm> threadPostUpsertForm) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate(threadPost.getThread().getParentForum());

        Html content = replyThreadPostView.render(threadPost, threadPostUpsertForm);
        htmlTemplate.setContent(content);

        htmlTemplate.setMainTitle(Messages.get("forum.thread.post.text.reply"));
        htmlTemplate.setMainBackButton(Messages.get("commons.button.backTo1", threadPost.getThread().getParentForum().getName()), org.iatoki.judgels.raguel.forum.routes.ForumController.viewForums(threadPost.getThread().getParentForum().getId()));

        htmlTemplate.markBreadcrumbLocation(threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));
        htmlTemplate.markBreadcrumbLocation(Messages.get("forum.thread.post.text.reply"), routes.ThreadPostController.replyThreadPost(threadPost.getId()));

        return renderTemplate(htmlTemplate);
    }
}
