package org.iatoki.judgels.raguel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.api.jophiel.JophielUser;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.play.Page;

import java.util.List;

@ImplementedBy(UserServiceImpl.class)
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
