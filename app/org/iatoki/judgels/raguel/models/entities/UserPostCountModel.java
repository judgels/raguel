package org.iatoki.judgels.raguel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_user_post_count", indexes = {@Index(name = "upc_userjid", columnList = "userJid")})
public final class UserPostCountModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String userJid;

    public long postCount;
}
