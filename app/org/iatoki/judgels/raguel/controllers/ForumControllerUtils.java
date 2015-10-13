package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionsAndBackLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithBackLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.modules.forum.ForumModule;
import org.iatoki.judgels.raguel.modules.forum.ForumModuleComparator;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.modules.forum.TabbedForumModule;
import org.iatoki.judgels.raguel.services.ForumMemberService;
import play.api.mvc.Call;
import play.i18n.Messages;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public final class ForumControllerUtils {

    private static ForumControllerUtils instance;

    private ForumMemberService forumMemberService;

    private ForumControllerUtils(ForumMemberService forumMemberService) {
        this.forumMemberService = forumMemberService;
    }

    public static synchronized void buildInstance(ForumMemberService forumMemberService) {
        if (instance != null) {
            throw new UnsupportedOperationException("ForumControllerUtils instance has already been built");
        }

        instance = new ForumControllerUtils(forumMemberService);
    }

    static ForumControllerUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("ForumControllerUtils instance has not been built");
        }

        return instance;
    }

    static void appendUpdateLayout(LazyHtml content, Forum forum) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("forum.config.general"), routes.ForumController.editForumGeneralConfig(forum.getId())),
                    new InternalLink(Messages.get("forum.config.modules"), routes.ForumController.editForumModuleConfig(forum.getId())),
                    new InternalLink(Messages.get("forum.config.specific"), routes.ForumController.editForumSpecificConfig(forum.getId()))
                ), c)
        );
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("forum.forum") + "#" + forum.getId() + ": " + forum.getName(), new InternalLink(Messages.get("forum.enter"), routes.ForumController.viewForums(forum.getId())), c));
    }

    static ImmutableList.Builder<InternalLink> getBreadcrumbsBuilder() {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("forum.forums"), routes.ForumController.index()));

        return breadcrumbsBuilder;
    }

    static void appendBreadcrumbsLayout(LazyHtml content, List<InternalLink> lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = getBreadcrumbsBuilder();
        breadcrumbsBuilder.addAll(lastLinks);

        RaguelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }

    static void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = getBreadcrumbsBuilder();
        breadcrumbsBuilder.add(lastLinks);

        RaguelControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }

    public boolean isModeratorOrAbove() {
        return RaguelControllerUtils.getInstance().isModeratorOrAbove();
    }

    public boolean isAllowedToEnterForum(Forum forum, String userJid) {
        if (forum.containOrInheritModule(ForumModules.MEMBER)) {
            Forum forumWithMember = forum.getForumOrParentWithModule(ForumModules.MEMBER);

            return RaguelControllerUtils.getInstance().isModeratorOrAbove() || forumMemberService.isMemberInForum(forumWithMember.getJid(), userJid);
        }

        return true;
    }

    void appendTabsLayout(LazyHtml content, Forum forum, String userJid) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        internalLinkBuilder.add(new InternalLink(Messages.get("forum.forums"), routes.ForumController.viewForums(forum.getId())));

        List<TabbedForumModule> moduleWithTabs = Lists.newArrayList();
        for (ForumModule forumModule : forum.getModules()) {
            if ((forumModule instanceof TabbedForumModule) && (((TabbedForumModule) forumModule).isAllowedToViewTab(this, forum, IdentityUtils.getUserJid()))) {
                moduleWithTabs.add((TabbedForumModule) forumModule);
            }
        }
        Collections.sort(moduleWithTabs, new ForumModuleComparator());
        for (TabbedForumModule forumModule : moduleWithTabs) {
            internalLinkBuilder.add(new InternalLink(forumModule.getTabName(), forumModule.getDefaultJumpTo(forum.getId())));
        }

        List<InternalLink> internalLinks = internalLinkBuilder.build();
        if (internalLinks.size() > 1) {
            content.appendLayout(c -> tabLayout.render(internalLinkBuilder.build(), c));
        }

        Forum parentForum = forum.getParentForum();

        final String parentForumName;
        final Call backCall;
        if (parentForum == null) {
            parentForumName = Messages.get("forum.home");
            backCall = routes.ForumController.index();
        } else {
            parentForumName = parentForum.getName();
            backCall = routes.ForumController.viewForums(parentForum.getId());
        }

        if (RaguelControllerUtils.getInstance().isModeratorOrAbove()) {
            ImmutableList.Builder<InternalLink> actionsBuilder = ImmutableList.builder();
            actionsBuilder.add(new InternalLink(Messages.get("commons.update"), routes.ForumController.editForumGeneralConfig(forum.getId())));
            actionsBuilder.add(new InternalLink(Messages.get("forum.create"), routes.ForumController.createForum(forum.getId())));
            if (forum.containModule(ForumModules.THREAD)) {
                actionsBuilder.add(new InternalLink(Messages.get("forum.thread.create"), routes.ForumThreadController.createForumThread(forum.getId())));
            }

            content.appendLayout(c -> headingWithActionsAndBackLayout.render(forum.getName(), actionsBuilder.build(), new InternalLink(Messages.get("forum.backTo") + " " + parentForumName, backCall), c));
        } else {
            if (forum.containModule(ForumModules.THREAD) && (!forum.containOrInheritModule(ForumModules.EXCLUSIVE) || RaguelControllerUtils.getInstance().isModeratorOrAbove())) {
                content.appendLayout(c -> headingWithActionAndBackLayout.render(forum.getName(), new InternalLink(Messages.get("forum.thread.create"), routes.ForumThreadController.createForumThread(forum.getId())), new InternalLink(Messages.get("forum.backTo") + " " + parentForumName, backCall), c));
            } else {
                content.appendLayout(c -> headingWithBackLayout.render(forum.getName(), new InternalLink(Messages.get("forum.backTo") + " " + parentForumName, backCall), c));
            }
        }
    }

    static ImmutableList.Builder<InternalLink> getForumBreadcrumbsBuilder(Forum parentForum) {
        ImmutableList.Builder<InternalLink> internalLinkBuilder = ImmutableList.builder();
        Stack<InternalLink> internalLinkStack = new Stack<>();
        Forum currentParent = parentForum;
        while (currentParent != null) {
            internalLinkStack.push(new InternalLink(currentParent.getName(), routes.ForumController.viewForums(currentParent.getId())));
            currentParent = currentParent.getParentForum();
        }

        while (!internalLinkStack.isEmpty()) {
            internalLinkBuilder.add(internalLinkStack.pop());
        }

        return internalLinkBuilder;
    }
}
