package org.iatoki.judgels.raguel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.raguel.Forum;
import play.i18n.Messages;

import java.util.List;

public final class ForumControllerUtils {

    private ForumControllerUtils() {
        // prevent instantiation
    }

    static void appendUpdateLayout(LazyHtml content, Forum forum) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                    new InternalLink(Messages.get("forum.config.general"), routes.ForumController.updateForumGeneralConfig(forum.getId())),
                    new InternalLink(Messages.get("forum.config.modules"), routes.ForumController.updateForumModuleConfig(forum.getId()))
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
}
