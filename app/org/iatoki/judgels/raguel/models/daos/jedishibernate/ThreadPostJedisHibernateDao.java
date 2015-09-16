package org.iatoki.judgels.raguel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ThreadPostDao;
import org.iatoki.judgels.raguel.models.entities.ThreadPostModel;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("threadPostDao")
public final class ThreadPostJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ThreadPostModel> implements ThreadPostDao {

    @Inject
    public ThreadPostJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ThreadPostModel.class);
    }
}
