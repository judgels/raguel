package org.iatoki.judgels.raguel.models.daos.jedishibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsJedisHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ForumThreadDao;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel_;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("threadDao")
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
