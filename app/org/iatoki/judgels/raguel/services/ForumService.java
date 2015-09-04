package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;

import java.util.List;

public interface ForumService {

    boolean forumExistsByJid(String forumJid);

    List<Forum> getAllForums();

    List<Forum> getChildForums(String parentJid);

    Forum findForumById(long forumId) throws ForumNotFoundException;

    void createForum(String parentJid, String name, String description);

    void updateForum(String forumJid, String parentJid, String name, String description);
}
