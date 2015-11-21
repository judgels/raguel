package org.iatoki.judgels.raguel.services.impls;

import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.PostContent;
import org.iatoki.judgels.raguel.ThreadPost;
import org.iatoki.judgels.raguel.models.entities.ThreadPostModel;

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
