package org.iatoki.judgels.raguel.forum;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(ForumLastPostHibernateDao.class)
public interface ForumLastPostDao extends Dao<Long, ForumLastPostModel> {

    boolean existsByForumJid(String forumJid);

    ForumLastPostModel findByForumJid(String forumJid);
}
