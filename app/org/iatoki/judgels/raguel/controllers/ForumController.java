package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionsAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.ForumThreadWithStatistics;
import org.iatoki.judgels.raguel.RaguelUtils;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.Authorized;
import org.iatoki.judgels.raguel.controllers.securities.GuestView;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.forms.ForumUpsertForm;
import org.iatoki.judgels.raguel.modules.ForumModules;
import org.iatoki.judgels.raguel.services.ForumModuleService;
import org.iatoki.judgels.raguel.services.ForumService;
import org.iatoki.judgels.raguel.services.ForumThreadService;
import org.iatoki.judgels.raguel.views.html.forum.createForumView;
import org.iatoki.judgels.raguel.views.html.forum.listForumsAndThreadsView;
import org.iatoki.judgels.raguel.views.html.forum.listForumsView;
import org.iatoki.judgels.raguel.views.html.forum.modules.listModulesView;
import org.iatoki.judgels.raguel.views.html.forum.updateForumGeneralView;
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
import java.util.Stack;
import java.util.stream.Collectors;

@Singleton
@Named
public final class ForumController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final ForumModuleService forumModuleService;
    private final ForumService forumService;
    private final ForumThreadService forumThreadService;

    @Inject
    public ForumController(ForumModuleService forumModuleService, ForumService forumService, ForumThreadService forumThreadService) {
        this.forumModuleService = forumModuleService;
        this.forumService = forumService;
        this.forumThreadService = forumThreadService;
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result index() throws ForumNotFoundException {
        return viewForums(0);
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result viewForums(long forumId) throws ForumNotFoundException {
        return showListForumsThreads(forumId, 0, "timeUpdate", "desc", "");
    }

    @Authenticated(value = GuestView.class)
    @Transactional(readOnly = true)
    public Result listForumsThreads(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        return showListForumsThreads(forumId, pageIndex, orderBy, orderDir, filterString);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createForum() {
        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class);

        return showCreateForum(forumUpsertForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postCreateForum() {
        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).bindFromRequest();

        if (formHasErrors(forumUpsertForm)) {
            return showCreateForum(forumUpsertForm);
        }

        ForumUpsertForm forumUpsertData = forumUpsertForm.get();
        forumService.createForum(forumUpsertData.parentJid, forumUpsertData.name, forumUpsertData.description);

        return redirect(routes.ForumController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result updateForumGeneralConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        ForumUpsertForm forumUpsertData = new ForumUpsertForm();
        if (forum.getParentForum() != null) {
            forumUpsertData.parentJid = forum.getParentForum().getJid();
        }
        forumUpsertData.name = forum.getName();
        forumUpsertData.description = forum.getDescription();

        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).fill(forumUpsertData);

        return showUpdateForumGeneral(forumUpsertForm, forum);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postUpdateForumGeneralConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).bindFromRequest();

        if (formHasErrors(forumUpsertForm)) {
            return showUpdateForumGeneral(forumUpsertForm, forum);
        }

        ForumUpsertForm forumUpsertData = forumUpsertForm.get();
        forumService.updateForum(forum.getJid(), forumUpsertData.parentJid, forumUpsertData.name, forumUpsertData.description);

        return redirect(routes.ForumController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    public Result updateForumModuleConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        LazyHtml content = new LazyHtml(listModulesView.render(forum));
        ForumControllerUtils.appendUpdateLayout(content, forum);
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.update"), routes.ForumController.updateForumGeneralConfig(forum.getId())),
                new InternalLink(Messages.get("forum.config.modules"), routes.ForumController.updateForumModuleConfig(forum.getId()))
        );

        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Update Module");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result enableModule(long forumId, String forumModule) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        forumModuleService.enableModule(forum.getJid(), ForumModules.valueOf(forumModule));

        return redirect(routes.ForumController.updateForumModuleConfig(forum.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result disableModule(long forumId, String forumModule) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        forumModuleService.disableModule(forum.getJid(), ForumModules.valueOf(forumModule));

        return redirect(routes.ForumController.updateForumModuleConfig(forum.getId()));
    }

    private Result showListForumsThreads(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Forum currentForum;
        Forum parentForum;
        List<Forum> childForums;
        if (forumId == 0) {
            currentForum = null;
            parentForum = null;
            childForums = forumService.getChildForums("");
        } else {
            currentForum = forumService.findForumById(forumId);
            parentForum = currentForum.getParentForum();
            childForums = forumService.getChildForums(currentForum.getJid());
        }

        LazyHtml content;
        if ((currentForum != null) && currentForum.containsModule(ForumModules.THREAD)) {
            Page<ForumThreadWithStatistics> pageOfForumThreads = forumThreadService.getPageOfForumThreadsWithStatistic(currentForum, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            content = new LazyHtml(listForumsAndThreadsView.render(currentForum, childForums, pageOfForumThreads, orderBy, orderDir, filterString));
        } else {
            content = new LazyHtml(listForumsView.render(currentForum, childForums));
        }

        if (currentForum != null) {
            final String parentForumName;
            final long parentForumId;
            if (parentForum == null) {
                parentForumName = Messages.get("forum.home");
                parentForumId = 0;
            } else {
                parentForumName = parentForum.getName();
                parentForumId = parentForum.getId();
            }

            if (RaguelUtils.hasRole("admin")) {
                ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();
                actionsBuilder.add(new InternalLink(Messages.get("commons.update"), routes.ForumController.updateForumGeneralConfig(forumId)));
                actionsBuilder.add(new InternalLink(Messages.get("forum.create"), routes.ForumController.createForum()));
                if (currentForum.containsModule(ForumModules.THREAD)) {
                    actionsBuilder.add(new InternalLink(Messages.get("forum.thread.create"), routes.ForumThreadController.createForumThread(currentForum.getId())));
                }

                content.appendLayout(c -> headingWithActionsAndBackLayout.render(Messages.get("forum.forum") + " " + currentForum.getName(), actionsBuilder.build(), new InternalLink(Messages.get("forum.backTo") + " " + parentForumName, routes.ForumController.viewForums(parentForumId)), c));
            } else {
                if (currentForum.containsModule(ForumModules.THREAD)) {
                    content.appendLayout(c -> headingWithActionAndBackLayout.render(Messages.get("forum.forum") + " " + currentForum.getName(), new InternalLink(Messages.get("forum.thread.create"), routes.ForumThreadController.createForumThread(currentForum.getId())), new InternalLink(Messages.get("forum.backTo") + " " + parentForumName, routes.ForumController.viewForums(parentForumId)), c));
                } else {
                    content.appendLayout(c -> headingWithBackLayout.render(Messages.get("forum.forum") + " " + currentForum.getName(), new InternalLink(Messages.get("forum.backTo") + " " + parentForumName, routes.ForumController.viewForums(parentForumId)), c));
                }
            }
        } else {
            if (RaguelUtils.hasRole("admin")) {
                content.appendLayout(c -> headingWithActionLayout.render(Messages.get("forum.forums"), new InternalLink(Messages.get("commons.create"), routes.ForumController.createForum()), c));
            } else {
                content.appendLayout(c -> headingLayout.render(Messages.get("forum.forums"), c));
            }
        }

        RaguelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        if (parentForum != null) {
            Stack<InternalLink> internalLinkStack = new Stack<>();
            Forum currentParent = parentForum;
            while (currentParent != null) {
                internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
                currentParent = currentParent.getParentForum();
            }

            while (!internalLinkStack.isEmpty()) {
                internalLinkBuilder.add(internalLinkStack.pop());
            }
        }
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forums");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showCreateForum(Form<ForumUpsertForm> forumUpsertForm) {
        LazyHtml content = new LazyHtml(createForumView.render(forumUpsertForm, forumService.getAllForums()));
        content.appendLayout(c -> headingLayout.render(Messages.get("forum.create"), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.create"), routes.ForumController.createForum())
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Create");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateForumGeneral(Form<ForumUpsertForm> forumUpsertForm, Forum forum) {
        LazyHtml content = new LazyHtml(updateForumGeneralView.render(forumUpsertForm, forum.getId(), forumService.getAllForums().stream().filter(f -> !f.containsJidInHierarchy(forum.getJid())).collect(Collectors.toList())));
        ForumControllerUtils.appendUpdateLayout(content, forum);
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.update"), routes.ForumController.updateForumGeneralConfig(forum.getId())),
                new InternalLink(Messages.get("forum.config.general"), routes.ForumController.updateForumGeneralConfig(forum.getId()))
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Update");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }
}
