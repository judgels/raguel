package org.iatoki.judgels.raguel.forms;

import play.data.validation.Constraints;

import java.io.File;

public final class ForumMemberUploadForm {

    @Constraints.Required
    public File usernames;
}
