package org.iatoki.judgels.raguel.forum.module;

import org.iatoki.judgels.play.model.AbstractModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "raguel_forum_module")
public final class ForumModuleModel extends AbstractModel {

    @Id
    @GeneratedValue
    public long id;

    public String forumJid;

    public String name;

    @Column(columnDefinition = "TEXT")
    public String config;

    public boolean enabled;
}
