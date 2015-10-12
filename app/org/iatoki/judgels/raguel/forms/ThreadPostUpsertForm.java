package org.iatoki.judgels.raguel.forms;

import play.data.validation.Constraints;

public final class ThreadPostUpsertForm {

    @Constraints.Required
    public String subject;

    @Constraints.Required
    public String content;
}