package org.iatoki.judgels.raguel.forum.thread;

import org.iatoki.judgels.raguel.forum.Forum;

import java.util.Date;

public final class ForumThreadServiceUtils {

    private ForumThreadServiceUtils() {
        //prevent instantiation
    }

    public static ForumThread createForumThreadFromModelAndForum(ForumThreadModel forumThreadModel, Forum forum) {
        return new ForumThread(forumThreadModel.id, forumThreadModel.jid, forum, forumThreadModel.name, forumThreadModel.userCreate, new Date(forumThreadModel.timeUpdate), forumThreadModel.userUpdate);
    }
}
