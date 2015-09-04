package org.iatoki.judgels.raguel;

public final class ForumThreadWithStatistics {

    private final ForumThread forumThread;
    private final long viewCount;
    private final long replyCount;

    public ForumThreadWithStatistics(ForumThread forumThread, long viewCount, long replyCount) {
        this.forumThread = forumThread;
        this.viewCount = viewCount;
        this.replyCount = replyCount;
    }

    public ForumThread getForumThread() {
        return forumThread;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getReplyCount() {
        return replyCount;
    }
}
