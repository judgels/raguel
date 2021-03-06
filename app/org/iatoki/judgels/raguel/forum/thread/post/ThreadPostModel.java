package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_post")
@JidPrefix("POST")
public final class ThreadPostModel extends AbstractJudgelsModel {

    public String threadJid;

    public String replyJid;
}
