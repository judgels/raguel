package org.iatoki.judgels.raguel.models.daos.jedishibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractJedisHibernateDao;
import org.iatoki.judgels.raguel.models.daos.UserPostCountDao;
import org.iatoki.judgels.raguel.models.entities.UserPostCountModel;
import org.iatoki.judgels.raguel.models.entities.UserPostCountModel_;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("UserPostCountDao")
public class UserPostCountJedisHibernateDao extends AbstractJedisHibernateDao<Long, UserPostCountModel> implements UserPostCountDao {

    @Inject
    public UserPostCountJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, UserPostCountModel.class);
    }

    @Override
    public UserPostCountModel getByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserPostCountModel> query = cb.createQuery(getModelClass());
        Root<UserPostCountModel> root = query.from(getModelClass());

        query.where(cb.equal(root.get(UserPostCountModel_.userJid), userJid));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<UserPostCountModel> getByUserJids(List<String> userJids) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<UserPostCountModel> query = cb.createQuery(getModelClass());
        Root<UserPostCountModel> root = query.from(getModelClass());

        query.where(root.get(UserPostCountModel_.userJid).in(userJids));

        return JPA.em().createQuery(query).getResultList();
    }
}
