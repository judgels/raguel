package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.raguel.User;
import org.iatoki.judgels.raguel.UserNotFoundException;

import java.util.List;

public interface UserService extends BaseUserService {

    User findUserById(long userId) throws UserNotFoundException;

    User findUserByJid(String userJid);

    Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    void createUser(String userJid, List<String> roles, String createUserJid, String createUserIpAddress);

    void updateUser(long userId, List<String> roles, String userJid, String userIpAddress);

    void deleteUser(long userId);

    void upsertUserFromJophielUser(JophielUser jophielUser, String userJid, String userIpAddress);

    void upsertUserFromJophielUser(JophielUser jophielUser, List<String> roles, String userJid, String userIpAddress);
}
