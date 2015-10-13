package org.iatoki.judgels.raguel;

import org.iatoki.judgels.play.EntityNotFoundException;

public final class ForumMemberNotFoundException extends EntityNotFoundException {

    public ForumMemberNotFoundException() {
        super();
    }

    public ForumMemberNotFoundException(String s) {
        super(s);
    }

    public ForumMemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForumMemberNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getEntityName() {
        return "Forum Member";
    }
}
