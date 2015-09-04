package org.iatoki.judgels.raguel.models.daos.impls;

import org.iatoki.judgels.play.models.daos.impls.AbstractJudgelsHibernateDao;
import org.iatoki.judgels.raguel.models.daos.ThreadPostDao;
import org.iatoki.judgels.raguel.models.entities.ThreadPostModel;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("threadPostDao")
public final class ThreadPostHibernateDao extends AbstractJudgelsHibernateDao<ThreadPostModel> implements ThreadPostDao {

    public ThreadPostHibernateDao() {
        super(ThreadPostModel.class);
    }
}
