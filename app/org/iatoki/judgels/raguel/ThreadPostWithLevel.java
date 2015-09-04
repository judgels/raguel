package org.iatoki.judgels.raguel;

public final class ThreadPostWithLevel {

    private final ThreadPost threadPost;
    private final int level;

    public ThreadPostWithLevel(ThreadPost threadPost, int level) {
        this.threadPost = threadPost;
        this.level = level;
    }

    public ThreadPost getThreadPost() {
        return threadPost;
    }

    public int getLevel() {
        return level;
    }
}
