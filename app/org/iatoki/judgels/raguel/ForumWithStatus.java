package org.iatoki.judgels.raguel;

public final class ForumWithStatus {

    private final Forum forum;
    private final boolean hasNewPost;

    public ForumWithStatus(Forum forum, boolean hasNewPost) {
        this.forum = forum;
        this.hasNewPost = hasNewPost;
    }

    public Forum getForum() {
        return forum;
    }

    public boolean isHasNewPost() {
        return hasNewPost;
    }
}
