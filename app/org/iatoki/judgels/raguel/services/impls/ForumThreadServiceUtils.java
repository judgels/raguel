package org.iatoki.judgels.raguel.services.impls;

import org.iatoki.judgels.raguel.Forum;
import org.iatoki.judgels.raguel.ForumThread;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel;

import java.util.Date;

public final class ForumThreadServiceUtils {

    private ForumThreadServiceUtils() {
        //prevent instantiation
    }

    static ForumThread createForumThreadFromModelAndForum(ForumThreadModel forumThreadModel, Forum forum) {
        return new ForumThread(forumThreadModel.id, forumThreadModel.jid, forum, forumThreadModel.name, forumThreadModel.userCreate, new Date(forumThreadModel.timeUpdate), forumThreadModel.userUpdate);
    }
}
