package org.iatoki.judgels.raguel.forum.thread;

import org.iatoki.judgels.play.jid.JidPrefix;
import org.iatoki.judgels.play.model.AbstractJudgelsModel;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_thread")
@JidPrefix("THRE")
public final class ForumThreadModel extends AbstractJudgelsModel {

    public String forumJid;

    public String name;
}
