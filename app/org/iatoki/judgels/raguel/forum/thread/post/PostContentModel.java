package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.play.models.JidPrefix;
import org.iatoki.judgels.play.models.entities.AbstractJudgelsModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_post_content")
@JidPrefix("POCO")
public final class PostContentModel extends AbstractJudgelsModel {

    public String postJid;

    public String subject;

    @Column(columnDefinition = "TEXT")
    public String content;
}
