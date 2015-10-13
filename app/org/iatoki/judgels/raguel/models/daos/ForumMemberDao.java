package org.iatoki.judgels.raguel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.raguel.models.entities.ForumMemberModel;

import java.util.List;

public interface ForumMemberDao extends Dao<Long, ForumMemberModel> {

    boolean existsInForumByMemberJid(String forumJid, String memberJid);

    ForumMemberModel findInForumByMemberJid(String forumJid, String memberJid);

    List<String> getForumJidsByJid(String memberJid);
}
