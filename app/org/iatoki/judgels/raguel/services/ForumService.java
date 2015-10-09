package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumNotFoundException;
import org.iatoki.judgels.raguel.ForumWithStatus;

import java.util.List;

public interface ForumService {

    boolean forumExistsByJid(String forumJid);

    List<Forum> getAllForums();

    List<Forum> getChildForums(String parentJid);

    List<ForumWithStatus> getChildForumsWithStatus(String parentJid, String userJid);

    Forum findForumById(long forumId) throws ForumNotFoundException;

    Forum findForumByJid(String forumJid);

    void createForum(Forum parentForum, String name, String description, String userJid, String userIpAddress);

    void updateForum(Forum forum, Forum parentForum, String name, String description, String userJid, String userIpAddress);
}
