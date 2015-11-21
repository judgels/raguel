package org.iatoki.judgels.raguel.models.entities;

import org.iatoki.judgels.play.models.entities.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserPostCountModel.class)
public class UserPostCountModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<UserPostCountModel, String> userJid;
    public static volatile SingularAttribute<UserPostCountModel, Long> postCount;

}
