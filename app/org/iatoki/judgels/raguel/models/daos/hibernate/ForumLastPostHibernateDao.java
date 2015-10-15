package org.iatoki.judgels.raguel.models.daos.hibernate;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ForumLastPostDao;
import org.iatoki.judgels.raguel.models.entities.ForumLastPostModel;
import org.iatoki.judgels.raguel.models.entities.ForumLastPostModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
@Named("forumLastPostDao")
public final class ForumLastPostHibernateDao extends AbstractHibernateDao<Long, ForumLastPostModel> implements ForumLastPostDao {

    public ForumLastPostHibernateDao() {
        super(ForumLastPostModel.class);
    }

    @Override
    public boolean existsByForumJid(String forumJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ForumLastPostModel> root = query.from(ForumLastPostModel.class);

        query.select(cb.count(root)).where(cb.equal(root.get(ForumLastPostModel_.forumJid), forumJid));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public ForumLastPostModel findByForumJid(String forumJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ForumLastPostModel> query = cb.createQuery(ForumLastPostModel.class);
        Root<ForumLastPostModel> root = query.from(ForumLastPostModel.class);

        query.where(cb.equal(root.get(ForumLastPostModel_.forumJid), forumJid));

        return getFirstResultAndDeleteTheRest(query);
    }
}
