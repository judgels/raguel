package org.iatoki.judgels.raguel.forum.thread;

import play.data.validation.Constraints;

public final class ForumThreadCreateForm {

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String content;
}
