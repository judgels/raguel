package org.iatoki.judgels.raguel.forum.member;

import org.iatoki.judgels.play.model.AbstractHibernateDao;
import play.db.jpa.JPA;

import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Singleton
public final class ForumMemberJedisHibernateDao extends AbstractHibernateDao<Long, ForumMemberModel> implements ForumMemberDao {

    public ForumMemberJedisHibernateDao() {
        super(ForumMemberModel.class);
    }

    @Override
    public boolean existsInForumByMemberJid(String forumJid, String memberJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ForumMemberModel> root = query.from(ForumMemberModel.class);

        query.select(cb.count(root)).where(cb.and(cb.equal(root.get(ForumMemberModel_.forumJid), forumJid), cb.equal(root.get(ForumMemberModel_.userJid), memberJid)));

        return JPA.em().createQuery(query).getSingleResult() != 0;
    }

    @Override
    public ForumMemberModel findInForumByMemberJid(String forumJid, String memberJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<ForumMemberModel> query = cb.createQuery(ForumMemberModel.class);
        Root<ForumMemberModel> root = query.from(ForumMemberModel.class);

        query.where(cb.and(cb.equal(root.get(ForumMemberModel_.forumJid), forumJid), cb.equal(root.get(ForumMemberModel_.userJid), memberJid)));

        return JPA.em().createQuery(query).getSingleResult();
    }

    @Override
    public List<String> getForumJidsByJid(String memberJid) {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<ForumMemberModel> root = query.from(ForumMemberModel.class);

        query.select(root.get(ForumMemberModel_.forumJid)).where(cb.equal(root.get(ForumMemberModel_.userJid), memberJid));

        return JPA.em().createQuery(query).getResultList();
    }
}
