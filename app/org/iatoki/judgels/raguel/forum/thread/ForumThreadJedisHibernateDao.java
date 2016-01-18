package org.iatoki.judgels.raguel.forum.thread;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ForumThreadJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ForumThreadModel> implements ForumThreadDao {

    @Inject
    public ForumThreadJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ForumThreadModel.class);
    }

    @Override
    protected List<SingularAttribute<ForumThreadModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ForumThreadModel_.name);
    }
}
