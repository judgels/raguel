package org.iatoki.judgels.raguel;

import java.util.Date;
import java.util.List;

public final class ThreadPost {

    private final long id;
    private final String jid;
    private final ForumThread thread;
    private final List<PostContent> contents;
    private final String userJid;
    private final String replyJid;
    private final Date timeCreate;

    public ThreadPost(long id, String jid, ForumThread thread, List<PostContent> contents, String userJid, String replyJid, Date timeCreate) {
        this.id = id;
        this.jid = jid;
        this.thread = thread;
        this.contents = contents;
        this.userJid = userJid;
        this.replyJid = replyJid;
        this.timeCreate = timeCreate;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public ForumThread getThread() {
        return thread;
    }

    public List<PostContent> getContents() {
        return contents;
    }

    public PostContent getLatestContent() {
        return contents.get(contents.size() - 1);
    }

    public String getUserJid() {
        return userJid;
    }

    public String getReplyJid() {
        return replyJid;
    }

    public Date getTimeCreate() {
        return timeCreate;
    }
}
