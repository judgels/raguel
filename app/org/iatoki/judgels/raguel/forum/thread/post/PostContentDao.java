package org.iatoki.judgels.raguel.forum.thread.post;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

@ImplementedBy(PostContentHibernateDao.class)
public interface PostContentDao extends JudgelsDao<PostContentModel> {

    long getCountByUserJid(String userJid);
}
