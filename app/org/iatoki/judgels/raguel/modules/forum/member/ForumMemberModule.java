package org.iatoki.judgels.raguel.modules.forum.member;

import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.controllers.ForumControllerUtils;
import org.iatoki.judgels.raguel.controllers.routes;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.modules.forum.TabbedForumModule;
import play.api.mvc.Call;
import play.i18n.Messages;

public final class ForumMemberModule extends TabbedForumModule {

    @Override
    public ForumModules getType() {
        return ForumModules.MEMBER;
    }

    @Override
    public String getTabName() {
        return Messages.get("forum.members");
    }

    @Override
    public boolean isAllowedToViewTab(ForumControllerUtils forumControllerUtils, Forum forum, String userJid) {
        return forumControllerUtils.isModeratorOrAbove();
    }

    @Override
    public Call getDefaultJumpTo(long forumId) {
        return routes.ForumMemberController.viewMembers(forumId);
    }
}
