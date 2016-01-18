package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.raguel.forum.thread.ForumThread;

import java.util.Date;
import java.util.List;

public final class ThreadPostServiceUtils {

    private ThreadPostServiceUtils() {
        //prevent instantiation
    }

    static ThreadPost createThreadPostFromModel(ThreadPostModel threadPostModel, ForumThread forumThread, List<PostContent> postContents, long userPostCount) {
        return new ThreadPost(threadPostModel.id, threadPostModel.jid, forumThread, postContents, threadPostModel.userCreate, userPostCount, threadPostModel.replyJid, new Date(threadPostModel.timeCreate));
    }
}
