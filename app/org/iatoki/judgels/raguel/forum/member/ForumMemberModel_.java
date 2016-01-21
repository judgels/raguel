package org.iatoki.judgels.raguel.forum.member;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ForumMemberModel.class)
public abstract class ForumMemberModel_ extends AbstractModel_ {

	public static volatile SingularAttribute<ForumMemberModel, Long> id;
	public static volatile SingularAttribute<ForumMemberModel, String> forumJid;
	public static volatile SingularAttribute<ForumMemberModel, String> userJid;
}
