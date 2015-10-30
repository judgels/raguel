package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.EnumUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.descriptionHtmlLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.ForumThreadWithStatistics;
import org.iatoki.judgels.raguel.ForumThreadWithStatisticsAndStatus;
import org.iatoki.judgels.raguel.ForumWithStatus;
import org.iatoki.judgels.raguel.RaguelUtils;
import org.iatoki.judgels.raguel.controllers.securities.Authenticated;
import org.iatoki.judgels.raguel.controllers.securities.Authorized;
import org.iatoki.judgels.raguel.controllers.securities.GuestView;
import org.iatoki.judgels.raguel.controllers.securities.HasRole;
import org.iatoki.judgels.raguel.controllers.securities.LoggedIn;
import org.iatoki.judgels.raguel.forms.ForumUpsertForm;
import org.iatoki.judgels.raguel.modules.forum.ForumModule;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleUtils;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.modules.forum.InheritedForumModules;
import org.iatoki.judgels.raguel.services.ForumModuleService;
import org.iatoki.judgels.raguel.services.ForumService;
import org.iatoki.judgels.raguel.services.ForumThreadService;
import org.iatoki.judgels.raguel.views.html.forum.createForumView;
import org.iatoki.judgels.raguel.views.html.forum.editForumGeneralView;
import org.iatoki.judgels.raguel.views.html.forum.editForumSpecificView;
import org.iatoki.judgels.raguel.views.html.forum.listBaseForumsView;
import org.iatoki.judgels.raguel.views.html.forum.listBaseForumsWithStatusView;
import org.iatoki.judgels.raguel.views.html.forum.listForumsAndThreadsView;
import org.iatoki.judgels.raguel.views.html.forum.listForumsAndThreadsWithStatusView;
import org.iatoki.judgels.raguel.views.html.forum.listForumsView;
import org.iatoki.judgels.raguel.views.html.forum.listForumsWithStatusView;
import org.iatoki.judgels.raguel.views.html.forum.modules.listModulesView;
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

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    public Result jumpToMembers(long forumId) {
        return redirect(routes.ForumMemberController.viewMembers(forumId));
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
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result createForum(long parentId) throws ForumNotFoundException {
        ForumUpsertForm forumUpsertData = new ForumUpsertForm();
        if (parentId != 0) {
            Forum forum = forumService.findForumById(parentId);
            forumUpsertData.parentJid = forum.getJid();
        }
        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).fill(forumUpsertData);

        return showCreateForum(parentId, forumUpsertForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional
    @RequireCSRFCheck
    public Result postCreateForum() {
        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).bindFromRequest();

        if (formHasErrors(forumUpsertForm)) {
            return showCreateForum(0, forumUpsertForm);
        }

        ForumUpsertForm forumUpsertData = forumUpsertForm.get();
        Forum forum = null;
        if (forumService.forumExistsByJid(forumUpsertData.parentJid)) {
            forum = forumService.findForumByJid(forumUpsertData.parentJid);
        }

        forumService.createForum(forum, forumUpsertData.name, forumUpsertData.description, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ForumController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editForumGeneralConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        ForumUpsertForm forumUpsertData = new ForumUpsertForm();
        if (forum.getParentForum() != null) {
            forumUpsertData.parentJid = forum.getParentForum().getJid();
        }
        forumUpsertData.name = forum.getName();
        forumUpsertData.description = forum.getDescription();

        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).fill(forumUpsertData);

        return showEditForumGeneral(forumUpsertForm, forum);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional
    @RequireCSRFCheck
    public Result postEditForumGeneralConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);
        Form<ForumUpsertForm> forumUpsertForm = Form.form(ForumUpsertForm.class).bindFromRequest();

        if (formHasErrors(forumUpsertForm)) {
            return showEditForumGeneral(forumUpsertForm, forum);
        }

        ForumUpsertForm forumUpsertData = forumUpsertForm.get();
        Forum parentForum = null;
        if (forumService.forumExistsByJid(forumUpsertData.parentJid)) {
            parentForum = forumService.findForumByJid(forumUpsertData.parentJid);
        }

        forumService.updateForum(forum, parentForum, forumUpsertData.name, forumUpsertData.description, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ForumController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    public Result editForumModuleConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        LazyHtml content = new LazyHtml(listModulesView.render(forum));
        ForumControllerUtils.appendUpdateLayout(content, forum);
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.update"), routes.ForumController.editForumGeneralConfig(forum.getId())),
                new InternalLink(Messages.get("forum.config.modules"), routes.ForumController.editForumModuleConfig(forum.getId()))
        );

        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Update Module");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional
    public Result enableModule(long forumId, String forumModule) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        if (!EnumUtils.isValidEnum(ForumModules.class, forumModule)) {
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        ForumModules forumModuleType = ForumModules.valueOf(forumModule);
        if (!ForumModuleUtils.getModuleContradiction(forumModuleType).isEmpty() && forum.getModulesSet().containsAll(ForumModuleUtils.getModuleContradiction(forumModuleType))) {
            flashError(Messages.get("forum.module.enable.error.contradiction", ForumModuleUtils.getModuleContradiction(forumModuleType).toString()));
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        if (!forum.getModulesSet().containsAll(ForumModuleUtils.getModuleDependencies(forumModuleType))) {
            flashError(Messages.get("forum.module.enable.error.dependencies", ForumModuleUtils.getModuleDependencies(forumModuleType).toString()));
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        // error if inherited
        if (EnumUtils.isValidEnum(InheritedForumModules.class, forumModule) && (forum.inheritModule(forumModuleType))) {
            flashError(Messages.get("forum.module.enable.error.inherited", ForumModuleUtils.getModuleDependencies(forumModuleType).toString()));
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        forumModuleService.enableModule(forum.getJid(), ForumModules.valueOf(forumModule));

        return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional
    public Result disableModule(long forumId, String forumModule) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        if (!EnumUtils.isValidEnum(ForumModules.class, forumModule)) {
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        ForumModules forumModuleType = ForumModules.valueOf(forumModule);
        if (forum.getModulesSet().containsAll(ForumModuleUtils.getDependedModules(forumModuleType)) && !ForumModuleUtils.getDependedModules(forumModuleType).isEmpty()) {
            flashError(Messages.get("forum.module.disable.error.dependencies", ForumModuleUtils.getDependedModules(forumModuleType).toString()));
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        forumModuleService.disableModule(forum.getJid(), ForumModules.valueOf(forumModule));

        return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editForumSpecificConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        ImmutableMap.Builder<ForumModule, Form<?>> moduleFormMapBuilder = ImmutableMap.builder();
        for (ForumModule forumModule : forum.getModules()) {
            moduleFormMapBuilder.put(forumModule, forumModule.generateConfigForm());
        }

        return showEditForumSpecificConfig(forum, moduleFormMapBuilder.build());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional
    @RequireCSRFCheck
    public Result postEditForumSpecificConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        boolean checkError = false;
        ImmutableMap.Builder<ForumModule, Form<?>> moduleFormMap = ImmutableMap.builder();
        ImmutableList.Builder<ForumModule> updatedForumModuleBuilder = ImmutableList.builder();
        for (ForumModule forumModule : forum.getModules()) {
            Form<?> moduleForm = forumModule.updateModuleByFormFromRequest(request());
            moduleFormMap.put(forumModule, moduleForm);
            updatedForumModuleBuilder.add(forumModule);
            if (formHasErrors(moduleForm)) {
                checkError = true;
            }
        }

        if (checkError) {
            return showEditForumSpecificConfig(forum, moduleFormMap.build());
        }

        forumService.updateForumModuleConfiguration(forum.getJid(), updatedForumModuleBuilder.build(), IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return redirect(routes.ForumController.editForumSpecificConfig(forum.getId()));
    }

    private Result showListForumsThreads(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Forum currentForum;
        LazyHtml content;

        if (forumId == 0) {
            currentForum = null;
            content = getBaseForumContent();
        } else {
            currentForum = forumService.findForumById(forumId);

            if (!RaguelControllerUtils.getInstance().isModeratorOrAbove() && !ForumControllerUtils.getInstance().isAllowedToEnterForum(currentForum, IdentityUtils.getUserJid())) {
                long parentForumId;
                if (currentForum.getParentForum() != null) {
                    parentForumId = currentForum.getParentForum().getId();
                } else {
                    parentForumId = 0;
                }

                return redirect(routes.ForumController.viewForums(parentForumId));
            }

            content = getNonBaseForumContent(currentForum, pageIndex, orderBy, orderDir, filterString);
        }

        if (currentForum != null) {
            if (!currentForum.getDescription().isEmpty()) {
                content.appendLayout(c -> descriptionHtmlLayout.render(currentForum.getDescription(), c));
            }
            ForumControllerUtils.getInstance().appendTabsLayout(content, currentForum, IdentityUtils.getUserJid());
        } else {
            if (RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
                content.appendLayout(c -> headingWithActionLayout.render(Messages.get("forum.forums"), new InternalLink(Messages.get("commons.create"), routes.ForumController.createForum(0)), c));
            } else {
                content.appendLayout(c -> headingLayout.render(Messages.get("forum.forums"), c));
            }
        }

        RaguelControllerUtils.getInstance().appendSidebarLayout(content);

        ImmutableList.Builder<InternalLink> internalLinkBuilder;
        if (currentForum != null) {
            internalLinkBuilder = ForumControllerUtils.getForumBreadcrumbsBuilder(currentForum);
        } else {
            internalLinkBuilder = ImmutableList.builder();
        }
        ForumControllerUtils.appendBreadcrumbsLayout(content, internalLinkBuilder.build());
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forums");

        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private LazyHtml getBaseForumContent() {
        if (RaguelUtils.isGuest()) {
            List<Forum> childForums = forumService.getAllowedChildForums("", 2);
            return new LazyHtml(listBaseForumsView.render(childForums));
        }

        List<Forum> childForums;
        if (RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            childForums = forumService.getChildForums("", 2);
        } else {
            childForums = forumService.getAllowedChildForums("", 2);
        }
        ImmutableMap.Builder<String, List<ForumWithStatus>> mapForumJidToForumsWithStatusBuilder = ImmutableMap.builder();
        for (Forum childForum : childForums) {
            if (RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
                mapForumJidToForumsWithStatusBuilder.put(childForum.getJid(), forumService.getChildForumsWithStatus(childForum.getJid(), IdentityUtils.getUserJid(), 2));
            } else {
                mapForumJidToForumsWithStatusBuilder.put(childForum.getJid(), forumService.getAllowedChildForumsWithStatus(childForum.getJid(), IdentityUtils.getUserJid(), 2));
            }
        }

        return new LazyHtml(listBaseForumsWithStatusView.render(childForums, mapForumJidToForumsWithStatusBuilder.build()));
    }

    private LazyHtml getNonBaseForumContent(Forum forum, long pageIndex, String orderBy, String orderDir, String filterString) {
        if (RaguelUtils.isGuest()) {
            List<Forum> childForums = forumService.getAllowedChildForums(forum.getJid(), 1);
            if (forum.containModule(ForumModules.THREAD)) {
                Page<ForumThreadWithStatistics> pageOfForumThreads = forumThreadService.getPageOfForumThreadsWithStatistic(forum, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

                return new LazyHtml(listForumsAndThreadsView.render(forum, childForums, pageOfForumThreads, orderBy, orderDir, filterString));
            }

            return new LazyHtml(listForumsView.render(forum, childForums));
        }

        List<ForumWithStatus> childForumsWithStatus;
        if (RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            childForumsWithStatus = forumService.getChildForumsWithStatus(forum.getJid(), IdentityUtils.getUserJid(), 1);
        } else {
            childForumsWithStatus = forumService.getAllowedChildForumsWithStatus(forum.getJid(), IdentityUtils.getUserJid(), 1);
        }
        if (forum.containModule(ForumModules.THREAD)) {
            Page<ForumThreadWithStatisticsAndStatus> pageOfForumThreads = forumThreadService.getPageOfForumThreadsWithStatisticAndStatus(forum, IdentityUtils.getUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            return new LazyHtml(listForumsAndThreadsWithStatusView.render(forum, childForumsWithStatus, pageOfForumThreads, orderBy, orderDir, filterString));
        }

        return new LazyHtml(listForumsWithStatusView.render(forum, childForumsWithStatus));
    }

    private Result showCreateForum(long parentId, Form<ForumUpsertForm> forumUpsertForm) {
        LazyHtml content = new LazyHtml(createForumView.render(forumUpsertForm, forumService.getAllForumsForReferences()));
        content.appendLayout(c -> headingLayout.render(Messages.get("forum.create"), c));
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.create"), routes.ForumController.createForum(parentId))
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Create");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditForumGeneral(Form<ForumUpsertForm> forumUpsertForm, Forum forum) {
        LazyHtml content = new LazyHtml(editForumGeneralView.render(forumUpsertForm, forum.getId(), forumService.getAllForumsForReferences().stream().filter(f -> !f.containsJidInHierarchy(forum.getJid())).collect(Collectors.toList())));
        ForumControllerUtils.appendUpdateLayout(content, forum);
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.edit"), routes.ForumController.editForumGeneralConfig(forum.getId())),
                new InternalLink(Messages.get("forum.config.general"), routes.ForumController.editForumGeneralConfig(forum.getId()))
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Edit General");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }

    private Result showEditForumSpecificConfig(Forum forum, Map<ForumModule, Form<?>> moduleFormMap) {
        LazyHtml content = new LazyHtml(editForumSpecificView.render(forum, moduleFormMap));
        ForumControllerUtils.appendUpdateLayout(content, forum);
        RaguelControllerUtils.getInstance().appendSidebarLayout(content);
        ForumControllerUtils.appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("forum.edit"), routes.ForumController.editForumGeneralConfig(forum.getId())),
                new InternalLink(Messages.get("forum.config.specific"), routes.ForumController.editForumSpecificConfig(forum.getId()))
        );
        RaguelControllerUtils.getInstance().appendTemplateLayout(content, "Forum - Edit Specific");
        return RaguelControllerUtils.getInstance().lazyOk(content);
    }
}
