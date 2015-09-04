package org.iatoki.judgels.raguel.modules;

import com.google.gson.Gson;
import org.iatoki.judgels.raguel.modules.thread.ForumThreadModule;

public final class ForumModuleFactory {

    public ForumModule createDefaultForumModule(ForumModules forumModules) {
        switch (forumModules) {
            case THREAD:
                return new ForumThreadModule();
            default:
                throw new RuntimeException();
        }
    }

    public ForumModule parseFromConfig(ForumModules forumModules, String config) {
        switch (forumModules) {
            case THREAD:
                return new Gson().fromJson(config, ForumThreadModule.class);
            default:
                throw new RuntimeException();
        }
    }
}
