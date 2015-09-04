package org.iatoki.judgels.raguel;

public final class Post {

    private final long id;
    private final String jid;
    private final ForumThread thread;
    private final String name;
    private final int version;

    public Post(long id, String jid, ForumThread thread, String name, int version) {
        this.id = id;
        this.jid = jid;
        this.thread = thread;
        this.name = name;
        this.version = version;
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

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }
}
