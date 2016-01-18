package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class PostNotFoundException extends EntityNotFoundException {

    public PostNotFoundException() {
        super();
    }

    public PostNotFoundException(String s) {
        super(s);
    }

    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PostNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Post";
    }
}
