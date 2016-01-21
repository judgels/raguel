package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.play.model.AbstractJudgelsJedisHibernateDao;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ThreadPostJedisHibernateDao extends AbstractJudgelsJedisHibernateDao<ThreadPostModel> implements ThreadPostDao {

    @Inject
    public ThreadPostJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, ThreadPostModel.class);
    }
}
