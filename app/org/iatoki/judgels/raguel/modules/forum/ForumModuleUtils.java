package org.iatoki.judgels.raguel.modules.forum;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class ForumModuleUtils {

    private ForumModuleUtils() {
        // prevent instantiation
    }

    public static List<ForumModules> getDependedModules(ForumModules forumModules) {
        switch (forumModules) {
            case THREAD:
                return ImmutableList.of(ForumModules.EXCLUSIVE);
            default:
                return ImmutableList.of();
        }
    }

    public static List<ForumModules> getModuleDependencies(ForumModules forumModules) {
        switch (forumModules) {
            case EXCLUSIVE:
                return ImmutableList.of(ForumModules.THREAD);
            default:
                return ImmutableList.of();
        }
    }

    public static List<ForumModules> getModuleContradiction(ForumModules forumModules) {
        switch (forumModules) {
            default:
                return ImmutableList.of();
        }
    }
}
