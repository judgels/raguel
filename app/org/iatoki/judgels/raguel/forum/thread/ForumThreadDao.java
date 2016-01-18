package org.iatoki.judgels.raguel.forum.thread;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.models.daos.JudgelsDao;

@ImplementedBy(ForumThreadHibernateDao.class)
public interface ForumThreadDao extends JudgelsDao<ForumThreadModel> {

}
