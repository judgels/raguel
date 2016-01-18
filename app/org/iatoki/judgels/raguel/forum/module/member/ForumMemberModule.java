package org.iatoki.judgels.raguel.forum.module.member;

import play.i18n.Messages;
import org.iatoki.judgels.raguel.forum.Forum;
import org.iatoki.judgels.raguel.forum.module.ForumModules;
import org.iatoki.judgels.raguel.forum.module.TabbedForumModule;
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
        return org.iatoki.judgels.raguel.forum.member.routes.ForumMemberController.viewMembers(forumId);
    }
}
