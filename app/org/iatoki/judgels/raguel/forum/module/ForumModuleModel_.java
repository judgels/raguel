package org.iatoki.judgels.raguel.forum.module;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ForumModuleModel.class)
public abstract class ForumModuleModel_ extends org.iatoki.judgels.play.model.AbstractModel_ {

	public static volatile SingularAttribute<ForumModuleModel, Long> id;
	public static volatile SingularAttribute<ForumModuleModel, String> forumJid;
	public static volatile SingularAttribute<ForumModuleModel, String> name;
	public static volatile SingularAttribute<ForumModuleModel, String> config;
	public static volatile SingularAttribute<ForumModuleModel, Boolean> enabled;
}
