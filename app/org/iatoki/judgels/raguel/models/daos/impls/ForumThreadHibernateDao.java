package org.iatoki.judgels.raguel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ForumThreadDao;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel;
import org.iatoki.judgels.raguel.models.entities.ForumThreadModel_;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("threadDao")
public final class ForumThreadHibernateDao extends AbstractJudgelsHibernateDao<ForumThreadModel> implements ForumThreadDao {

    public ForumThreadHibernateDao() {
        super(ForumThreadModel.class);
    }

    @Override
    protected List<SingularAttribute<ForumThreadModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(ForumThreadModel_.name);
    }
}
