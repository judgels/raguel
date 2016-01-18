package org.iatoki.judgels.raguel.forum.member;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(ForumMemberHibernateDao.class)
public interface ForumMemberDao extends Dao<Long, ForumMemberModel> {

    boolean existsInForumByMemberJid(String forumJid, String memberJid);

    ForumMemberModel findInForumByMemberJid(String forumJid, String memberJid);

    List<String> getForumJidsByJid(String memberJid);
}
