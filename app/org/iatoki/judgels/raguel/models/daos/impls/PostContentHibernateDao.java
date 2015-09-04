package org.iatoki.judgels.raguel.models.daos.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.raguel.models.daos.PostContentDao;
import org.iatoki.judgels.raguel.models.entities.PostContentModel;
import org.iatoki.judgels.raguel.models.entities.PostContentModel_;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

@Singleton
@Named("postContentDao")
public final class PostContentHibernateDao extends AbstractJudgelsHibernateDao<PostContentModel> implements PostContentDao {

    public PostContentHibernateDao() {
        super(PostContentModel.class);
    }

    @Override
    protected List<SingularAttribute<PostContentModel, String>> getColumnsFilterableByString() {
        return ImmutableList.of(PostContentModel_.subject, PostContentModel_.content);
    }
}
