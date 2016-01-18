package org.iatoki.judgels.raguel.forum.thread.post;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.forum.thread.ForumThread;

import java.util.List;
import java.util.Map;

@ImplementedBy(ThreadPostServiceImpl.class)
public interface ThreadPostService {

    ThreadPost findThreadPostById(long threadPostId);

    Map<String, Long> getThreadPostsJidToIdMap(List<String> threadPostJids);

    Map<String, String> getThreadPostsJidToUserJidMap(List<String> threadPostJids);

    long getUserPostCountByUserJid(String userJid);

    long countThreadPost(ForumThread forumThread);

    List<ThreadPostWithLevel> getAllThreadPostsWithLevel(ForumThread forumThread);

    Page<ThreadPost> getPageOfThreadPosts(ForumThread forumThread, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createPost(ForumThread forumThread, String userJid, String subject, String body, String userIpAddress);

    void editPost(ThreadPost threadPost, String userJid, String subject, String body, String userIpAddress);

    void replyPost(ThreadPost threadPost, String userJid, String subject, String body, String userIpAddress);
}
