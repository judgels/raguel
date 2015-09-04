package org.iatoki.judgels.raguel.models.entities;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_post")
@JidPrefix("POST")
public final class ThreadPostModel extends AbstractJudgelsModel {

    public String threadJid;

    public String replyJid;
}
