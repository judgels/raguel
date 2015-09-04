package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.ForumThreadNotFoundException;
import org.iatoki.judgels.raguel.ForumThreadWithStatistics;

public interface ForumThreadService {

    Page<ForumThreadWithStatistics> getPageOfForumThreadsWithStatistic(Forum forum, long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    ForumThread findForumThreadById(long forumThreadId) throws ForumThreadNotFoundException;

    String createForumThread(String forumJid, String name);
}
