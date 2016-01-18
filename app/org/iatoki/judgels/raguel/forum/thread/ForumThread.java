package org.iatoki.judgels.raguel.forum.thread;

import org.iatoki.judgels.raguel.forum.Forum;

import java.util.Date;

public final class ForumThread {

    private final long id;
    private final String jid;
    private final Forum parentForum;
    private final String name;
    private final String authorJid;
    private final Date lastUpdate;
    private final String lastUpdateUserJid;

    public ForumThread(long id, String jid, Forum parentForum, String name, String authorJid, Date lastUpdate, String lastUpdateUserJid) {
        this.id = id;
        this.jid = jid;
        this.parentForum = parentForum;
        this.name = name;
        this.authorJid = authorJid;
        this.lastUpdate = lastUpdate;
        this.lastUpdateUserJid = lastUpdateUserJid;
    }

    public long getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public Forum getParentForum() {
        return parentForum;
    }

    public String getName() {
        return name;
    }

    public String getAuthorJid() {
        return authorJid;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String getLastUpdateUserJid() {
        return lastUpdateUserJid;
    }
}
