package org.iatoki.judgels.raguel.forum.thread;

public final class ForumThreadWithStatisticsAndStatus {

    private final ForumThreadWithStatistics forumThreadWithStatistics;
    private final boolean hasNewPost;

    public ForumThreadWithStatisticsAndStatus(ForumThreadWithStatistics forumThreadWithStatistics, boolean hasNewPost) {
        this.forumThreadWithStatistics = forumThreadWithStatistics;
        this.hasNewPost = hasNewPost;
    }

    public ForumThreadWithStatistics getForumThreadWithStatistics() {
        return forumThreadWithStatistics;
    }

    public boolean isHasNewPost() {
        return hasNewPost;
    }
}
