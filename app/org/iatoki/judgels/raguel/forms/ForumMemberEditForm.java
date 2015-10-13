package org.iatoki.judgels.raguel.forms;

import play.data.validation.Constraints;

public final class ForumMemberEditForm {

    @Constraints.Required
    public String status;
}
