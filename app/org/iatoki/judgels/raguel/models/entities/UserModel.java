package org.iatoki.judgels.raguel.models.entities;

import org.iatoki.judgels.jophiel.models.entities.AbstractUserModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_user")
public final class UserModel extends AbstractUserModel {

    public String roles;
}
