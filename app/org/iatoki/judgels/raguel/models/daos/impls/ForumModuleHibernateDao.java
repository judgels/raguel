package org.iatoki.judgels.raguel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ForumModuleDao;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel;
import org.iatoki.judgels.raguel.models.entities.ForumModuleModel_;
import play.db.jpa.JPA;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
@Named("forumModuleDao")
public final class ForumModuleHibernateDao extends AbstractHibernateDao<Long, ForumModuleModel> implements ForumModuleDao {

    public ForumModuleHibernateDao() {
        super(ForumModuleModel.class);
    }

    @Override
    public boolean existsInForumByName(String forumJid, String forumModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ForumModuleModel> root = query.from(ForumModuleModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ForumModuleModel_.forumJid), forumJid), cb.equal(root.get(ForumModuleModel_.name), forumModuleName)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public ForumModuleModel findInForumByName(String forumJid, String forumModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ForumModuleModel> query = cb.createQuery(ForumModuleModel.class);
        Root<ForumModuleModel> root = query.from(ForumModuleModel.class);

        query.where(cb.and(cb.equal(root.get(ForumModuleModel_.forumJid), forumJid), cb.equal(root.get(ForumModuleModel_.name), forumModuleName)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<ForumModuleModel> getEnabledInForum(String forumJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ForumModuleModel> query = cb.createQuery(ForumModuleModel.class);
        Root<ForumModuleModel> root = query.from(ForumModuleModel.class);

        query.where(cb.and(cb.equal(root.get(ForumModuleModel_.forumJid), forumJid), cb.equal(root.get(ForumModuleModel_.enabled), true)));

        return JPA.em().createQuery(query).getResultList();
    }

    @Override
    public List<ForumModuleModel> getEnabledByName(String forumModuleName) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ForumModuleModel> query = cb.createQuery(ForumModuleModel.class);
        Root<ForumModuleModel> root = query.from(ForumModuleModel.class);

        query.where(cb.and(cb.equal(root.get(ForumModuleModel_.name), forumModuleName), cb.equal(root.get(ForumModuleModel_.enabled), true)));

        return JPA.em().createQuery(query).getResultList();
    }
}
