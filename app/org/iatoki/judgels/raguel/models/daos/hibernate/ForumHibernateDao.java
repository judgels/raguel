package org.iatoki.judgels.raguel.models.daos.hibernate;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ForumDao;
import org.iatoki.judgels.raguel.models.entities.ForumModel;
import org.iatoki.judgels.raguel.models.entities.ForumModel_;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("forumDao")
public final class ForumHibernateDao extends AbstractJudgelsHibernateDao<ForumModel> implements ForumDao {

    public ForumHibernateDao() {
        super(ForumModel.class);
    }

    @Override
    protected List<SingularAttribute<ForumModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ForumModel_.name, ForumModel_.description);
    }
}
