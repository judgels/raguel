package org.iatoki.judgels.raguel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_thread")
@JidPrefix("THRE")
public final class ForumThreadModel extends AbstractJudgelsModel {

    public String forumJid;

    public String name;
}
