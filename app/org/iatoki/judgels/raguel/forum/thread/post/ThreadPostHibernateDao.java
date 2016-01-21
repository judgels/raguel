package org.iatoki.judgels.raguel.forum.thread.post;

import org.iatoki.judgels.play.model.AbstractJudgelsHibernateDao;

import javax.inject.Singleton;

@Singleton
public final class ThreadPostHibernateDao extends AbstractJudgelsHibernateDao<ThreadPostModel> implements ThreadPostDao {

    public ThreadPostHibernateDao() {
        super(ThreadPostModel.class);
    }
}
