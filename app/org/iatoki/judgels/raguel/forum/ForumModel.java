package org.iatoki.judgels.raguel.forum;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_forum")
@JidPrefix("FORU")
public final class ForumModel extends AbstractJudgelsModel {

    public String parentJid;

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;
}
