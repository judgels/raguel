package org.iatoki.judgels.raguel.forum.thread;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.forum.Forum;

@ImplementedBy(ForumThreadServiceImpl.class)
public interface ForumThreadService {

    Page<ForumThread> getPageOfForumThreads(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ForumThreadWithStatistics> getPageOfForumThreadsWithStatistic(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ForumThreadWithStatisticsAndStatus> getPageOfForumThreadsWithStatisticAndStatus(Forum forum, String userJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ForumThread findForumThreadById(long forumThreadId) throws ForumThreadNotFoundException;

    ForumThread createForumThread(Forum forum, String name, String userJid, String userIpAddress);
}
