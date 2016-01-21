package org.iatoki.judgels.raguel.forum;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.model.AbstractJudgelsHibernateDao;

import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
public final class ForumHibernateDao extends AbstractJudgelsHibernateDao<ForumModel> implements ForumDao {

    public ForumHibernateDao() {
        super(ForumModel.class);
    }

    @Override
    protected List<SingularAttribute<ForumModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ForumModel_.name, ForumModel_.description);
    }
}
