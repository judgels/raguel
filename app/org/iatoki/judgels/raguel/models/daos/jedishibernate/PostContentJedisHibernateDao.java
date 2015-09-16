package org.iatoki.judgels.raguel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import org.iatoki.judgels.raguel.models.daos.PostContentDao;
import org.iatoki.judgels.raguel.models.entities.PostContentModel;
import org.iatoki.judgels.raguel.models.entities.PostContentModel_;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("postContentDao")
public final class PostContentJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<PostContentModel> implements PostContentDao {

    @Inject
    public PostContentJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, PostContentModel.class);
    }

    @Override
    protected List<SingularAttribute<PostContentModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(PostContentModel_.subject, PostContentModel_.content);
    }
}
