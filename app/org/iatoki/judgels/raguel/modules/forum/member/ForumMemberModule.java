package org.iatoki.judgels.raguel.modules.forum.member;

import play.i18n.Messages;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.controllers.routes;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.modules.forum.TabbedForumModule;
import play.api.mvc.Call;

import java.util.List;

public final class ForumMemberModule extends TabbedForumModule {

    @Override
    public ForumModules getType() {
        return ForumModules.MEMBER;
    }

    @Override
    public String getTabName() {
        return Messages.get("forum.text.members");
    }

    @Override
    public boolean isAllowedToViewTab(List<String> roles, Forum forum, String userJid) {
        return roles.contains("admin") || roles.contains("moderator");
    }

    @Override
    public Call getDefaultJumpTo(long forumId) {
        return routes.ForumMemberController.viewMembers(forumId);
    }
}
