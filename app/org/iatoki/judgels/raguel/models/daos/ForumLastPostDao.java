package org.iatoki.judgels.raguel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.raguel.models.entities.ForumLastPostModel;

public interface ForumLastPostDao extends Dao<Long, ForumLastPostModel> {

    boolean existsByForumJid(String forumJid);

    ForumLastPostModel findByForumJid(String forumJid);
}
