package org.iatoki.judgels.raguel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.raguel.models.entities.UserPostCountModel;

import java.util.List;

public interface UserPostCountDao extends Dao<Long, UserPostCountModel> {

    boolean existsByUserJid(String userJid);

    UserPostCountModel getByUserJid(String userJid);

    List<UserPostCountModel> getByUserJids(List<String> userJids);
}