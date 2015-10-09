package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.ForumThreadNotFoundException;
import org.iatoki.judgels.raguel.ForumThreadWithStatistics;
import org.iatoki.judgels.raguel.ForumThreadWithStatisticsAndStatus;

public interface ForumThreadService {

    Page<ForumThreadWithStatistics> getPageOfForumThreadsWithStatistic(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<ForumThreadWithStatisticsAndStatus> getPageOfForumThreadsWithStatisticAndStatus(Forum forum, String userJid, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ForumThread findForumThreadById(long forumThreadId) throws ForumThreadNotFoundException;

    ForumThread createForumThread(Forum forum, String name, String userJid, String userIpAddress);
}
