package org.iatoki.judgels.raguel.forum.module;

import com.google.gson.Gson;
import org.iatoki.judgels.raguel.forum.module.exclusive.ForumExclusiveModule;
import org.iatoki.judgels.raguel.forum.module.member.ForumMemberModule;
import org.iatoki.judgels.raguel.forum.module.thread.ForumThreadModule;

public final class ForumModuleFactory {

    public ForumModule createDefaultForumModule(ForumModules forumModules) {
        switch (forumModules) {
            case THREAD:
                return new ForumThreadModule();
            case MEMBER:
                return new ForumMemberModule();
            case EXCLUSIVE:
                return new ForumExclusiveModule();
            default:
                throw new RuntimeException();
        }
    }

    public ForumModule parseFromConfig(ForumModules forumModules, String config) {
        switch (forumModules) {
            case THREAD:
                return new Gson().fromJson(config, ForumThreadModule.class);
            case MEMBER:
                return new Gson().fromJson(config, ForumMemberModule.class);
            case EXCLUSIVE:
                return new Gson().fromJson(config, ForumExclusiveModule.class);
            default:
                throw new RuntimeException();
        }
    }
}
