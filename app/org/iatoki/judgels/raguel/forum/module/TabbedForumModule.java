package org.iatoki.judgels.raguel.forum.module;

import org.iatoki.judgels.raguel.forum.Forum;
import play.api.mvc.Call;

import java.util.List;

public abstract class TabbedForumModule extends ForumModule {

    public abstract String getTabName();

    public abstract boolean isAllowedToViewTab(List<String> roles, Forum forum, String userJid);

    public abstract Call getDefaultJumpTo(long forumId);
}
