package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.EnumUtils;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.services.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.services.UserActivityMessageService;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.services.BaseJidCacheService;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.ForumThreadWithStatistics;
import org.iatoki.judgels.raguel.ForumThreadWithStatisticsAndStatus;
import org.iatoki.judgels.raguel.ForumWithStatus;
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
import org.iatoki.judgels.raguel.services.ForumMemberService;
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
import org.iatoki.judgels.play.JudgelsPlayMessages;
import play.mvc.Result;
import play.twirl.api.Html;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@Named
public final class ForumController extends AbstractForumController {

    private static final long PAGE_SIZE = 20;

    private final ForumModuleService forumModuleService;
    private final ForumService forumService;
    private final ForumThreadService forumThreadService;

    @Inject
    public ForumController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, ForumMemberService forumMemberService, ForumModuleService forumModuleService, ForumService forumService, ForumThreadService forumThreadService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService, forumMemberService);
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

        forumService.createForum(forum, forumUpsertData.name, forumUpsertData.description, getCurrentUserJid(), getCurrentUserIpAddress());

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

        forumService.updateForum(forum, parentForum, forumUpsertData.name, forumUpsertData.description, getCurrentUserJid(), getCurrentUserIpAddress());

        return redirect(routes.ForumController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"moderator", "admin"})
    @Transactional(readOnly = true)
    public Result editForumModuleConfig(long forumId) throws ForumNotFoundException {
        Forum forum = forumService.findForumById(forumId);

        return showEditForumModuleConfig(forum);
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
            flashError(JudgelsPlayMessages.get("forum.module.enable.error.contradiction", ForumModuleUtils.getModuleContradiction(forumModuleType).toString()));
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        if (!forum.getModulesSet().containsAll(ForumModuleUtils.getModuleDependencies(forumModuleType))) {
            flashError(JudgelsPlayMessages.get("forum.module.enable.error.dependencies", ForumModuleUtils.getModuleDependencies(forumModuleType).toString()));
            return redirect(routes.ForumController.editForumModuleConfig(forum.getId()));
        }

        // error if inherited
        if (EnumUtils.isValidEnum(InheritedForumModules.class, forumModule) && (forum.inheritModule(forumModuleType))) {
            flashError(JudgelsPlayMessages.get("forum.module.enable.error.inherited", ForumModuleUtils.getModuleDependencies(forumModuleType).toString()));
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
            flashError(JudgelsPlayMessages.get("forum.module.disable.error.dependencies", ForumModuleUtils.getDependedModules(forumModuleType).toString()));
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

        forumService.updateForumModuleConfiguration(forum.getJid(), updatedForumModuleBuilder.build(), getCurrentUserJid(), getCurrentUserIpAddress());

        return redirect(routes.ForumController.editForumSpecificConfig(forum.getId()));
    }

    private Result showListForumsThreads(long forumId, long pageIndex, String orderBy, String orderDir, String filterString) throws ForumNotFoundException {
        Forum currentForum;
        Html content;

        if (forumId == 0) {
            currentForum = null;
            content = getBaseForumContent();
        } else {
            currentForum = forumService.findForumById(forumId);

            if (!isCurrentUserModeratorOrAdmin() && !isCurrentUserAllowedToEnterForum(currentForum)) {
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

        HtmlTemplate htmlTemplate ;

        if (currentForum != null) {
            htmlTemplate = super.getBaseHtmlTemplate(currentForum);
            if (!currentForum.getDescription().isEmpty()) {
                htmlTemplate.setDescription(currentForum.getDescription());
            }
        } else {
            htmlTemplate = super.getBaseHtmlTemplate();
            if (isCurrentUserModeratorOrAdmin()) {
                htmlTemplate.addMainButton(JudgelsPlayMessages.get("commons.button.new1", JudgelsPlayMessages
                .get("forum.text.forum")), routes.ForumController.createForum(0));
            }
        }

        htmlTemplate.setContent(content);
        htmlTemplate.setMainTitle(JudgelsPlayMessages.get("forum.text.forum"));

        return renderTemplate(htmlTemplate);
    }

    private Html getBaseForumContent() {
        if (isCurrentUserGuest()) {
            List<Forum> childForums = forumService.getAllowedChildForums("", 2);
            return listBaseForumsView.render(childForums);
        }

        List<Forum> childForums;
        if (isCurrentUserModeratorOrAdmin()) {
            childForums = forumService.getChildForums("", 2);
        } else {
            childForums = forumService.getAllowedChildForums("", 2);
        }
        ImmutableMap.Builder<String, List<ForumWithStatus>> mapForumJidToForumsWithStatusBuilder = ImmutableMap.builder();
        for (Forum childForum : childForums) {
            if (isCurrentUserModeratorOrAdmin()) {
                mapForumJidToForumsWithStatusBuilder.put(childForum.getJid(), forumService.getChildForumsWithStatus(childForum.getJid(), getCurrentUserJid(), 2));
            } else {
                mapForumJidToForumsWithStatusBuilder.put(childForum.getJid(), forumService.getAllowedChildForumsWithStatus(childForum.getJid(), getCurrentUserJid(), 2));
            }
        }

        return listBaseForumsWithStatusView.render(childForums, mapForumJidToForumsWithStatusBuilder.build());
    }

    private Html getNonBaseForumContent(Forum forum, long pageIndex, String orderBy, String orderDir, String filterString) {
        if (isCurrentUserGuest()) {
            List<Forum> childForums = forumService.getAllowedChildForums(forum.getJid(), 1);
            if (forum.containModule(ForumModules.THREAD)) {
                Page<ForumThreadWithStatistics> pageOfForumThreads = forumThreadService.getPageOfForumThreadsWithStatistic(forum, pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

                return listForumsAndThreadsView.render(forum, childForums, pageOfForumThreads, orderBy, orderDir, filterString);
            }

            return listForumsView.render(forum, childForums);
        }

        List<ForumWithStatus> childForumsWithStatus;
        if (isCurrentUserModeratorOrAdmin()) {
            childForumsWithStatus = forumService.getChildForumsWithStatus(forum.getJid(), getCurrentUserJid(), 1);
        } else {
            childForumsWithStatus = forumService.getAllowedChildForumsWithStatus(forum.getJid(), getCurrentUserJid(), 1);
        }
        if (forum.containModule(ForumModules.THREAD)) {
            Page<ForumThreadWithStatisticsAndStatus> pageOfForumThreads = forumThreadService.getPageOfForumThreadsWithStatisticAndStatus(forum, getCurrentUserJid(), pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

            return listForumsAndThreadsWithStatusView.render(forum, childForumsWithStatus, pageOfForumThreads, orderBy, orderDir, filterString);
        }

        return listForumsWithStatusView.render(forum, childForumsWithStatus);
    }

    private Result showCreateForum(long parentId, Form<ForumUpsertForm> forumUpsertForm) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        htmlTemplate.setContent(createForumView.render(forumUpsertForm, forumService.getAllForumsForReferences()));
        htmlTemplate.setMainTitle(JudgelsPlayMessages.get("commons.text.new1", JudgelsPlayMessages.get("forum.text.forum")));
        htmlTemplate.markBreadcrumbLocation(JudgelsPlayMessages.get("commons.text.new"), routes.ForumController.createForum(parentId));

        return renderTemplate(htmlTemplate);
    }

    private Result showEditForumGeneral(Form<ForumUpsertForm> forumUpsertForm, Forum forum) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        htmlTemplate.setContent(editForumGeneralView.render(forumUpsertForm, forum.getId(), forumService.getAllForumsForReferences().stream().filter(f -> !f.containsJidInHierarchy(forum.getJid())).collect(Collectors.toList())));
        addUpdateView(htmlTemplate, forum);
        htmlTemplate.addMainButton(JudgelsPlayMessages.get("commons.text.view"), routes.ForumController.viewForums(forum.getId()));

        htmlTemplate.markBreadcrumbLocation(JudgelsPlayMessages.get("forum.config.text.general"), routes.ForumController.editForumGeneralConfig(forum.getId()));

        return renderTemplate(htmlTemplate);
    }

    private Result showEditForumModuleConfig(Forum forum) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        htmlTemplate.setContent(listModulesView.render(forum));
        addUpdateView(htmlTemplate, forum);

        htmlTemplate.markBreadcrumbLocation(JudgelsPlayMessages.get("forum.config.text.specific"), routes.ForumController.editForumModuleConfig(forum.getId()));

        return renderTemplate(htmlTemplate);
    }

    private Result showEditForumSpecificConfig(Forum forum, Map<ForumModule, Form<?>> moduleFormMap) {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        htmlTemplate.setContent(editForumSpecificView.render(forum, moduleFormMap));
        addUpdateView(htmlTemplate, forum);

        htmlTemplate.markBreadcrumbLocation(JudgelsPlayMessages.get("forum.config.text.specific"), routes.ForumController.editForumSpecificConfig(forum.getId()));

        return renderTemplate(htmlTemplate);
    }

    private void addUpdateView(HtmlTemplate htmlTemplate, Forum forum) {
        htmlTemplate.addCategoryTab(JudgelsPlayMessages.get("forum.config.text.general"), routes.ForumController.editForumGeneralConfig(forum.getId()));
        htmlTemplate.addCategoryTab(JudgelsPlayMessages.get("forum.config.text.modules"), routes.ForumController.editForumModuleConfig(forum.getId()));
        htmlTemplate.addCategoryTab(JudgelsPlayMessages.get("forum.config.text.specific"), routes.ForumController.editForumSpecificConfig(forum.getId()));

        htmlTemplate.setMainTitle(JudgelsPlayMessages.get("forum.text.forum1", forum.getName()));
        htmlTemplate.markBreadcrumbLocation(JudgelsPlayMessages.get("commons.text.edit"), routes.ForumController.editForumGeneralConfig(forum.getId()));
    }
}
