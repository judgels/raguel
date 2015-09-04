package org.iatoki.judgels.raguel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel;

import java.util.List;

public interface ForumModuleDao extends Dao<Long, ForumModuleModel> {

    boolean existsInForumByName(String forumJid, String forumModuleName);

    ForumModuleModel findInForumByName(String forumJid, String forumModuleName);

    List<ForumModuleModel> getEnabledInForum(String forumJid);

    List<ForumModuleModel> getEnabledByName(String forumModuleName);
}
