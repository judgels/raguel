package org.iatoki.judgels.raguel.forum.thread.post;

import java.util.Date;

public final class PostContent {

    private final long id;
    private final String jid;
    private final String subject;
    private final String content;
    private final Date timeCreate;

    public PostContent(long id, String jid, String subject, String content, Date timeCreate) {
        this.id = id;
        this.jid = jid;
        this.subject = subject;
        this.content = content;
        this.timeCreate = timeCreate;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public Date getTimeCreate() {
        return timeCreate;
    }
}
