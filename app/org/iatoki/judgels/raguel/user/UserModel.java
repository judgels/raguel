package org.iatoki.judgels.raguel.user;

import org.iatoki.judgels.jophiel.user.AbstractUserModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_user")
public final class UserModel extends AbstractUserModel {

    public String roles;
}
