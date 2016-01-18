package org.iatoki.judgels.raguel.forum.module;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ForumModuleHibernateDao.class)
public interface ForumModuleDao extends Dao<Long, ForumModuleModel> {

    boolean existsInForumByName(String forumJid, String forumModuleName);

    ForumModuleModel findInForumByName(String forumJid, String forumModuleName);

    List<ForumModuleModel> getEnabledInForum(String forumJid);

    List<ForumModuleModel> getEnabledByName(String forumModuleName);
}
