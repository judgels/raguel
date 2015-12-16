package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.Lists;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.services.BaseAvatarCacheService;
import org.iatoki.judgels.jophiel.services.UserActivityMessageService;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.services.BaseJidCacheService;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.modules.forum.ForumModule;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleComparator;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.modules.forum.TabbedForumModule;
import org.iatoki.judgels.raguel.services.ForumMemberService;
import play.api.mvc.Call;
import org.iatoki.judgels.play.JudgelsPlayMessages;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public abstract class AbstractForumController extends AbstractRaguelController {

    private final ForumMemberService forumMemberService;

    public AbstractForumController(BaseJidCacheService jidCacheService, BaseAvatarCacheService avatarCacheService, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, UserActivityMessageService userActivityMessageService, ForumMemberService forumMemberService) {
        super(jidCacheService, avatarCacheService, jophielClientAPI, jophielPublicAPI, userActivityMessageService);
        this.forumMemberService = forumMemberService;
    }

    protected boolean isCurrentUserAllowedToEnterForum(Forum forum) {
        if (forum.containOrInheritModule(ForumModules.MEMBER)) {
            Forum forumWithMember = forum.getForumOrParentWithModule(ForumModules.MEMBER);

            return isCurrentUserModeratorOrAdmin() || forumMemberService.isMemberInForum(forumWithMember.getJid(), getCurrentUserJid());
        }

        return true;
    }

    protected HtmlTemplate getBaseHtmlTemplate(Forum forum) {
        HtmlTemplate htmlTemplate = getBaseHtmlTemplate();

        fillTemplateForForumView(htmlTemplate, forum);

        markForumBreadcrumbs(htmlTemplate, forum);

        return htmlTemplate;
    }

    @Override
    protected HtmlTemplate getBaseHtmlTemplate() {
        HtmlTemplate htmlTemplate = super.getBaseHtmlTemplate();

        htmlTemplate.markBreadcrumbLocation(JudgelsPlayMessages.get("forum.text.forums"), routes.ForumController.index());

        return htmlTemplate;
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        return super.renderTemplate(template);
    }

    protected final void markForumBreadcrumbs(HtmlTemplate htmlTemplate, Forum forum) {
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = forum;
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            InternalLink internalLink = internalLinkStack.pop();
            htmlTemplate.markBreadcrumbLocation(internalLink.getLabel(), internalLink.getTarget());
        }
    }

    private void fillTemplateForForumView(HtmlTemplate htmlTemplate, Forum forum) {
        htmlTemplate.addMainTab(JudgelsPlayMessages.get("forum.text.forums"), routes.ForumController.viewForums(forum.getId()));

        List<TabbedForumModule> moduleWithTabs = Lists.newArrayList();
        for (ForumModule forumModule : forum.getModules()) {
            if ((forumModule instanceof TabbedForumModule) && (((TabbedForumModule) forumModule).isAllowedToViewTab(getCurrentUserRoles(), forum, getCurrentUserJid()))) {
                moduleWithTabs.add((TabbedForumModule) forumModule);
            }
        }
        Collections.sort(moduleWithTabs, new ForumModuleComparator());

        for (TabbedForumModule forumModule : moduleWithTabs) {
            htmlTemplate.addMainTab(forumModule.getTabName(), forumModule.getDefaultJumpTo(forum.getId()));
        }

        Forum parentForum = forum.getParentForum();

        final String parentForumName;
        final Call backCall;
        if (parentForum == null) {
            parentForumName = JudgelsPlayMessages.get("forum.text.home");
            backCall = routes.ForumController.index();
        } else {
            parentForumName = parentForum.getName();
            backCall = routes.ForumController.viewForums(parentForum.getId());
        }

        htmlTemplate.setMainTitle(forum.getName());
        htmlTemplate.setMainBackButton(JudgelsPlayMessages.get("commons.button.backTo1", parentForumName), backCall);

        if (isCurrentUserModeratorOrAdmin()) {
            htmlTemplate.addMainButton(JudgelsPlayMessages.get("commons.button.edit"), routes.ForumController.editForumGeneralConfig(forum.getId()));
            htmlTemplate.addMainButton(JudgelsPlayMessages.get("commons.button.new1", JudgelsPlayMessages.get("forum.text.forum")), routes.ForumController.createForum(forum.getId()));
            if (forum.containModule(ForumModules.THREAD)) {
                htmlTemplate.addMainButton(JudgelsPlayMessages.get("commons.button.new1", JudgelsPlayMessages.get("forum.text.thread")), routes.ForumThreadController.createForumThread(forum.getId()));
            }

        } else {
            if (forum.containModule(ForumModules.THREAD) && (!forum.containOrInheritModule(ForumModules.EXCLUSIVE))) {
                htmlTemplate.addMainButton(JudgelsPlayMessages.get("commons.button.new1", JudgelsPlayMessages.get("forum.text.thread")), routes.ForumThreadController.createForumThread(forum.getId()));
            }
        }
    }
}
