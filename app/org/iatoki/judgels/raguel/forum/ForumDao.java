package org.iatoki.judgels.raguel.forum;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.models.daos.JudgelsDao;

@ImplementedBy(ForumHibernateDao.class)
public interface ForumDao extends JudgelsDao<ForumModel> {

}
