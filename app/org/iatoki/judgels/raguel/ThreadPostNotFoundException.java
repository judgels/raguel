package org.iatoki.judgels.raguel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ThreadPostNotFoundException extends EntityNotFoundException {

    public ThreadPostNotFoundException() {
        super();
    }

    public ThreadPostNotFoundException(String s) {
        super(s);
    }

    public ThreadPostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThreadPostNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Thread Post";
    }
}
