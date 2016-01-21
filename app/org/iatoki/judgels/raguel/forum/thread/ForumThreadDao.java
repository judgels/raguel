package org.iatoki.judgels.raguel.forum.thread;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

@ImplementedBy(ForumThreadHibernateDao.class)
public interface ForumThreadDao extends JudgelsDao<ForumThreadModel> {

}
