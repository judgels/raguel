package org.iatoki.judgels.raguel.forum.module;

import com.google.inject.ImplementedBy;

@ImplementedBy(ForumModuleServiceImpl.class)
public interface ForumModuleService {

    void enableModule(String forumJid, ForumModules forumModule);

    void disableModule(String forumJid, ForumModules forumModule);
}
