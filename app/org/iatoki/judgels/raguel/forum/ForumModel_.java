package org.iatoki.judgels.raguel.forum;

import org.iatoki.judgels.play.model.AbstractJudgelsModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ForumModel.class)
public abstract class ForumModel_ extends AbstractJudgelsModel_ {

	public static volatile SingularAttribute<ForumModel, String> parentJid;
	public static volatile SingularAttribute<ForumModel, String> name;
	public static volatile SingularAttribute<ForumModel, String> description;
}
