package org.iatoki.judgels.raguel.forum.thread;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;

import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ForumThreadHibernateDao extends AbstractJudgelsHibernateDao<ForumThreadModel> implements ForumThreadDao {

    public ForumThreadHibernateDao() {
        super(ForumThreadModel.class);
    }

    @Override
    protected List<SingularAttribute<ForumThreadModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ForumThreadModel_.name);
    }
}
