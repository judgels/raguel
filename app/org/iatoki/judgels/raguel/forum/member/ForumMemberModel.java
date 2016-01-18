package org.iatoki.judgels.raguel.forum.member;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_forum_member")
public final class ForumMemberModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String forumJid;

    public String userJid;
}
