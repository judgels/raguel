package org.iatoki.judgels.raguel.forum;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_forum_last_post")
public final class ForumLastPostModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String forumJid;
}
