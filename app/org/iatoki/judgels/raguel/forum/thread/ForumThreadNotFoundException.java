package org.iatoki.judgels.raguel.forum.thread;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ForumThreadNotFoundException extends EntityNotFoundException {

    public ForumThreadNotFoundException() {
        super();
    }

    public ForumThreadNotFoundException(String s) {
        super(s);
    }

    public ForumThreadNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForumThreadNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Thread";
    }
}
