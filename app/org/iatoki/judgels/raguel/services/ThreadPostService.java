package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.ThreadPost;
import org.iatoki.judgels.raguel.ThreadPostWithLevel;

import java.util.List;

public interface ThreadPostService {

    ThreadPost findThreadPostById(long threadPostId);

    List<ThreadPostWithLevel> getAllThreadPostsWithLevel(ForumThread forumThread);

    Page<ThreadPost> getPageOfThreadPosts(ForumThread forumThread, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createPost(String threadJid, String userJid, String subject, String body);

    void replyPost(String threadJid, String postJid, String userJid, String subject, String body);
}
