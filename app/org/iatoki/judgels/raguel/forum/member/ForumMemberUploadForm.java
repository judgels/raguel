package org.iatoki.judgels.raguel.forum.member;

import play.data.validation.Constraints;

import java.io.File;

public final class ForumMemberUploadForm {

    @Constraints.Required
    public File usernames;
}
