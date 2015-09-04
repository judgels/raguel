package org.iatoki.judgels.raguel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_user_item")
public final class UserItemModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public String itemJid;

    public String status;
}
