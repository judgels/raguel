package org.iatoki.judgels.raguel.forum.thread.post;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

@ImplementedBy(ThreadPostHibernateDao.class)
public interface ThreadPostDao extends JudgelsDao<ThreadPostModel> {

}
