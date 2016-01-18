package org.iatoki.judgels.raguel.forum.member;

import play.data.validation.Constraints;

public final class ForumMemberEditForm {

    @Constraints.Required
    public String status;
}
