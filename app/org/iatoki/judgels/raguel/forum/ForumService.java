package org.iatoki.judgels.raguel.forum;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.raguel.forum.module.ForumModule;

import java.util.Collection;
import java.util.List;

@ImplementedBy(ForumServiceImpl.class)
public interface ForumService {

    boolean forumExistsByJid(String forumJid);

    List<Forum> getAllForumsForReferences();

    List<Forum> getChildForums(String parentJid, long maxDepth);

    List<Forum> getAllowedChildForums(String parentJid, long maxDepth);

    List<ForumWithStatus> getChildForumsWithStatus(String parentJid, String userJid, long maxDepth);

    List<ForumWithStatus> getAllowedChildForumsWithStatus(String parentJid, String userJid, long maxDepth);

    Forum findForumById(long forumId) throws ForumNotFoundException;

    Forum findForumByJid(String forumJid);

    void createForum(Forum parentForum, String name, String description, String userJid, String userIpAddress);

    void updateForum(Forum forum, Forum parentForum, String name, String description, String userJid, String userIpAddress);

    void updateForumModuleConfiguration(String forumJid, Collection<ForumModule> forumModules, String userJid, String userIpAddress);
}
