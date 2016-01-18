package org.iatoki.judgels.raguel.forum;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ForumNotFoundException extends EntityNotFoundException {

    public ForumNotFoundException() {
        super();
    }

    public ForumNotFoundException(String s) {
        super(s);
    }

    public ForumNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForumNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Forum";
    }
}
