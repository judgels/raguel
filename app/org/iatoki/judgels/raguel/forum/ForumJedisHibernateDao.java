package org.iatoki.judgels.raguel.forum;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ForumJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ForumModel> implements ForumDao {

    @Inject
    public ForumJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ForumModel.class);
    }

    @Override
    protected List<SingularAttribute<ForumModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ForumModel_.name, ForumModel_.description);
    }
}
