package org.iatoki.judgels.raguel.user.postcount;

import org.iatoki.judgels.play.model.AbstractJedisHibernateDao;
import play.db.jpa.JPA;
import redis.clients.jedis.JedisPool;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public class UserPostCountJedisHibernateDao extends AbstractJedisHibernateDao<Long, UserPostCountModel> implements UserPostCountDao {

    @Inject
    public UserPostCountJedisHibernateDao(JedisPool jedisPool) {
        super(jedisPool, UserPostCountModel.class);
    }

    @Override
    public boolean existsByUserJid(String userJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<UserPostCountModel> root = query.from(getModelClass());

        query.select(cb.count(root)).where(cb.equal(root.get(UserPostCountModel_.userJid), userJid));
        return JPA.em().createQuery(query).getSingleResult() != 0;
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
