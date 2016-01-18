package org.iatoki.judgels.raguel.forum.member;

public final class ForumMember {

    private final long id;
    private final String forumJid;
    private final String userJid;

    public ForumMember(long id, String forumJid, String userJid) {
        this.id = id;
        this.forumJid = forumJid;
        this.userJid = userJid;
    }

    public long getId() {
        return id;
    }

    public String getForumJid() {
        return forumJid;
    }

    public String getUserJid() {
        return userJid;
    }
}
