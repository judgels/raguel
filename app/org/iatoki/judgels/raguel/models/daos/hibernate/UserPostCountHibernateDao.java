package org.iatoki.judgels.raguel.models.daos.hibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.raguel.models.daos.UserPostCountDao;
import org.iatoki.judgels.raguel.models.entities.UserPostCountModel;
import org.iatoki.judgels.raguel.models.entities.UserPostCountModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("UserPostCountDao")
public class UserPostCountHibernateDao extends AbstractHibernateDao<Long, UserPostCountModel> implements UserPostCountDao {

    public UserPostCountHibernateDao() {
        super(UserPostCountModel.class);
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
