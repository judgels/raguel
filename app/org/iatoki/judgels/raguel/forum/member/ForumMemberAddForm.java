package org.iatoki.judgels.raguel.forum.member;

import play.data.validation.Constraints;

public final class ForumMemberAddForm {

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String status;
}
