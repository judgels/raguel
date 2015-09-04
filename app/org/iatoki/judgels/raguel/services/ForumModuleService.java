package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.raguel.modules.ForumModules;

public interface ForumModuleService {

    void enableModule(String forumJid, ForumModules forumModule);

    void disableModule(String forumJid, ForumModules forumModule);
}
