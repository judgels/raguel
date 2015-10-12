package org.iatoki.judgels.raguel.forms;

import play.data.validation.Constraints;

public final class ForumUpsertForm {

    public String parentJid;

    @Constraints.Required
    public String name;

    public String description;
}
