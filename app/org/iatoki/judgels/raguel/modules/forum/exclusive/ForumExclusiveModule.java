package org.iatoki.judgels.raguel.modules.forum.exclusive;

import org.iatoki.judgels.raguel.modules.forum.ForumModule;
import org.iatoki.judgels.raguel.modules.forum.ForumModules;

public final class ForumExclusiveModule extends ForumModule {

    @Override
    public ForumModules getType() {
        return ForumModules.EXCLUSIVE;
    }
}
