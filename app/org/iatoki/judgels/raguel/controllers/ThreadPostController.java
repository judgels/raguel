package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.ForumThreadNotFoundException;
import org.iatoki.judgels.raguel.ThreadPost;
import org.iatoki.judgels.raguel.ThreadPostNotFoundException;
import org.iatoki.judgels.raguel.ThreadPostWithLevel;
import org.iatoki.judgels.raguel.UserItemStatus;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.GuestView;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.forms.ThreadPostUpsertForm;
import org.iatoki.judgels.raguel.services.ForumThreadService;
import org.iatoki.judgels.raguel.services.ThreadPostService;
import org.iatoki.judgels.raguel.services.UserItemService;
import org.iatoki.judgels.raguel.views.html.forum.thread.editThreadPostView;
import org.iatoki.judgels.raguel.views.html.forum.thread.listPostContentsView;
import org.iatoki.judgels.raguel.views.html.forum.thread.listThreadPostsTreeView;
import org.iatoki.judgels.raguel.views.html.forum.thread.listThreadPostsView;
import org.iatoki.judgels.raguel.views.html.forum.thread.replyThreadPostView;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
@Named
public final class ThreadPostController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ForumThreadService forumThreadService;
    private final ThreadPostService threadPostService;
    private final UserItemService userItemService;

    @Inject
    public ThreadPostController(ForumThreadService forumThreadService, ThreadPostService threadPostService, UserItemService userItemService) {
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
        List<ThreadPostWithLevel> threadPostsWithLevel = threadPostService.getAllThreadPostsWithLevel(forumThread);
        List<String> replyJids = threadPostsWithLevel.stream().map(e -> e.getThreadPost().getReplyJid()).collect(Collectors.toList());
        Map<String, Long> replyJidToIdMap = threadPostService.getThreadPostsJidToIdMap(replyJids);
        Map<String, String> replyJidToUserJidMap = threadPostService.getThreadPostsJidToUserJidMap(replyJids);

        userItemService.upsertUserItem(IdentityUtils.getUserJid(), forumThread.getJid(), UserItemStatus.VIEWED);

        LazyHtml content = new LazyHtml(listThreadPostsTreeView.render(forumThread, threadPostsWithLevel, replyJidToIdMap, replyJidToUserJidMap));
        content.appendLayout(c -> headingWithBackLayout.render(forumThread.getName(), new InternalLink(Messages.get("forum.thread.post.backTo") + " " + forumThread.getParentForum().getName(), routes.ForumController.viewForums(forumThread.getParentForum().getId())), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = forumThread.getParentForum();
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }
        internalLinkBuilder.add(new InternalLink(forumThread.getName(), routes.ThreadPostController.viewThreadPosts(forumThread.getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Thread " + forumThread.getName());

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = GuestView.class)
    @Transactional
    public Result viewPostVersions(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        LazyHtml content = new LazyHtml(listPostContentsView.render(threadPost));
        content.appendLayout(c -> headingWithBackLayout.render(Messages.get("forum.thread.post.versions"), new InternalLink(Messages.get("forum.thread.post.backTo") + " " + threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId())), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = threadPost.getThread().getParentForum();
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }
        internalLinkBuilder.add(new InternalLink(threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Post Version");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editThreadPost(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

        if (!IdentityUtils.getUserJid().equals(threadPost.getUserJid())) {
            return notFound();
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

        if (!IdentityUtils.getUserJid().equals(threadPost.getUserJid())) {
            return notFound();
        }

        Form<ThreadPostUpsertForm> threadPostUpsertForm = Form.form(ThreadPostUpsertForm.class).bindFromRequest();

        if (formHasErrors(threadPostUpsertForm)) {
            return showEditThreadPost(threadPost, threadPostUpsertForm);
        }

        ThreadPostUpsertForm threadPostUpsertData = threadPostUpsertForm.get();
        threadPostService.editPost(threadPost, IdentityUtils.getUserJid(), threadPostUpsertData.subject, threadPostUpsertData.content, IdentityUtils.getIpAddress());

        return redirect(routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result replyThreadPost(long threadPostId) throws ThreadPostNotFoundException {
        ThreadPost threadPost = threadPostService.findThreadPostById(threadPostId);

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

        Form<ThreadPostUpsertForm> threadPostUpsertForm = Form.form(ThreadPostUpsertForm.class).bindFromRequest();

        if (formHasErrors(threadPostUpsertForm)) {
            return showReplyThreadPost(threadPost, threadPostUpsertForm);
        }

        ThreadPostUpsertForm threadPostUpsertData = threadPostUpsertForm.get();
        threadPostService.replyPost(threadPost, IdentityUtils.getUserJid(), threadPostUpsertData.subject, threadPostUpsertData.content, IdentityUtils.getIpAddress());

        return redirect(routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId()));
    }

    private Result showListThreadPosts(long forumThreadId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumThreadNotFoundException {
        ForumThread forumThread = forumThreadService.findForumThreadById(forumThreadId);
        Page<ThreadPost> pageOfThreadPosts = threadPostService.getPageOfThreadPosts(forumThread, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        List<String> replyJids = pageOfThreadPosts.getData().stream().map(e -> e.getReplyJid()).collect(Collectors.toList());
        Map<String, Long> replyJidToIdMap = threadPostService.getThreadPostsJidToIdMap(replyJids);
        Map<String, String> replyJidToUserJidMap = threadPostService.getThreadPostsJidToUserJidMap(replyJids);

        userItemService.upsertUserItem(IdentityUtils.getUserJid(), forumThread.getJid(), UserItemStatus.VIEWED);

        LazyHtml content = new LazyHtml(listThreadPostsView.render(forumThread, pageOfThreadPosts, replyJidToIdMap, replyJidToUserJidMap, orderBy, orderDir, filterString));
        content.appendLayout(c -> headingWithBackLayout.render(forumThread.getName(), new InternalLink(Messages.get("forum.thread.post.backTo") + " " + forumThread.getParentForum().getName(), routes.ForumController.viewForums(forumThread.getParentForum().getId())), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = forumThread.getParentForum();
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }
        internalLinkBuilder.add(new InternalLink(forumThread.getName(), routes.ThreadPostController.viewThreadPosts(forumThread.getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Thread " + forumThread.getName());

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditThreadPost(ThreadPost threadPost, Form<ThreadPostUpsertForm> threadPostUpsertForm) {
        LazyHtml content = new LazyHtml(editThreadPostView.render(threadPost, threadPostUpsertForm));
        content.appendLayout(c -> headingWithBackLayout.render(Messages.get("forum.thread.post.edit"), new InternalLink(Messages.get("forum.thread.post.backTo") + " " + threadPost.getThread().getParentForum().getName(), routes.ForumController.viewForums(threadPost.getThread().getParentForum().getId())), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = threadPost.getThread().getParentForum();
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }
        internalLinkBuilder.add(new InternalLink(threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("forum.thread.post.edit"), routes.ThreadPostController.editThreadPost(threadPost.getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Post - Edit");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showReplyThreadPost(ThreadPost threadPost, Form<ThreadPostUpsertForm> threadPostUpsertForm) {
        LazyHtml content = new LazyHtml(replyThreadPostView.render(threadPost, threadPostUpsertForm));
        content.appendLayout(c -> headingWithBackLayout.render(Messages.get("forum.thread.post.reply"), new InternalLink(Messages.get("forum.thread.post.backTo") + " " + threadPost.getThread().getParentForum().getName(), routes.ForumController.viewForums(threadPost.getThread().getParentForum().getId())), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = threadPost.getThread().getParentForum();
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }
        internalLinkBuilder.add(new InternalLink(threadPost.getThread().getName(), routes.ThreadPostController.viewThreadPosts(threadPost.getThread().getId())));
        internalLinkBuilder.add(new InternalLink(Messages.get("forum.thread.post.reply"), routes.ThreadPostController.replyThreadPost(threadPost.getId())));
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Post - Reply");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }
}
