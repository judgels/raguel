package org.iatoki.judgels.raguel.modules.forum.thread;

import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.controllers.ForumControllerUtils;
import org.iatoki.judgels.raguel.controllers.routes;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;
import org.iatoki.judgels.raguel.modules.forum.TabbedForumModule;
import play.api.mvc.Call;
import play.i18n.Messages;

public final class ForumThreadModule extends TabbedForumModule {

    @Override
    public ForumModules getType() {
        return ForumModules.THREAD;
    }

    @Override
    public String getTabName() {
        return Messages.get("forum.threads");
    }

    @Override
    public boolean isAllowedToViewTab(ForumControllerUtils forumControllerUtils, Forum forum, String userJid) {
        return forumControllerUtils.isModeratorOrAbove();
    }

    @Override
    public Call getDefaultJumpTo(long forumId) {
        return routes.ForumThreadController.viewThreads(forumId);
    }
}
