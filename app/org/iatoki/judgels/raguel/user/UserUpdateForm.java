package org.iatoki.judgels.raguel.user;

import play.data.validation.Constraints;

import java.util.Arrays;
import java.util.List;

public final class UserUpdateForm {

    @Constraints.Required
    public String roles;

    public List<String> getRolesAsList() {
        return Arrays.asList(this.roles.split(","));
    }
}
