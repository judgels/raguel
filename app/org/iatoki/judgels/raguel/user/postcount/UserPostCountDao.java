package org.iatoki.judgels.raguel.user.postcount;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.List;

@ImplementedBy(UserPostCountHibernateDao.class)
public interface UserPostCountDao extends Dao<Long, UserPostCountModel> {

    boolean existsByUserJid(String userJid);

    UserPostCountModel getByUserJid(String userJid);

    List<UserPostCountModel> getByUserJids(List<String> userJids);
}
