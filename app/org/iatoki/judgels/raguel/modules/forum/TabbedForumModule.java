package org.iatoki.judgels.raguel.modules.forum;

import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.controllers.ForumControllerUtils;
import play.api.mvc.Call;

public abstract class TabbedForumModule extends ForumModule {

    public abstract String getTabName();

    public abstract boolean isAllowedToViewTab(ForumControllerUtils forumControllerUtils, Forum forum, String userJid);

    public abstract Call getDefaultJumpTo(long forumId);
}
