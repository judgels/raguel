package org.iatoki.judgels.raguel.models.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PostContentModel.class)
public abstract class PostContentModel_ extends org.iatoki.judgels.play.models.entities.AbstractJudgelsModel_ {

	public static volatile SingularAttribute<PostContentModel, String> postJid;
	public static volatile SingularAttribute<PostContentModel, String> subject;
	public static volatile SingularAttribute<PostContentModel, String> content;
}
