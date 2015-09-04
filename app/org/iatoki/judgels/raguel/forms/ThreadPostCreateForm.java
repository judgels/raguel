package org.iatoki.judgels.raguel.forms;

import play.data.validation.Constraints;

public final class ThreadPostCreateForm {

    @Constraints.Required
    public String subject;

    @Constraints.Required
    public String content;
}
