package org.iatoki.judgels.raguel.user.postcount;

import org.iatoki.judgels.play.model.AbstractModel_;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserPostCountModel.class)
public class UserPostCountModel_ extends AbstractModel_ {

    public static volatile SingularAttribute<UserPostCountModel, Long> id;
    public static volatile SingularAttribute<UserPostCountModel, String> userJid;
    public static volatile SingularAttribute<UserPostCountModel, Long> postCount;

}
