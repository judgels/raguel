package org.iatoki.judgels.raguel.user.item;

import org.iatoki.judgels.play.IdentityUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public final class UserItemServiceImpl implements UserItemService {

    private final UserItemDao userItemDao;

    @Inject
    public UserItemServiceImpl(UserItemDao userItemDao) {
        this.userItemDao = userItemDao;
    }

    @Override
    public boolean userItemExistsByUserJidAndItemJid(String userJid, String itemJid) {
        return userItemDao.existsByUserJidAndItemJid(userJid, itemJid);
    }

    @Override
    public boolean userItemExistsByUserJidAndItemJidAndStatus(String userJid, String itemJid, UserItemStatus status) {
        return userItemDao.existsByUserJidItemJidAndStatus(userJid, itemJid, status.name());
    }

    @Override
    public void upsertUserItem(String userJid, String itemJid, UserItemStatus status) {
        if (userItemDao.existsByUserJidAndItemJid(userJid, itemJid)) {
            UserItemModel userItemModel = userItemDao.findByUserJidAndItemJid(userJid, itemJid);
            userItemModel.status = status.name();

            userItemDao.edit(userItemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } else {
            UserItemModel userItemModel = new UserItemModel();
            userItemModel.userJid = userJid;
            userItemModel.itemJid = itemJid;
            userItemModel.status = status.name();

            userItemDao.persist(userItemModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public List<UserItem> getUserItemsByUserJid(String userJid) {
        return userItemDao.getByUserJid(userJid).stream().map(u -> createFromModel(u)).collect(Collectors.toList());
    }

    @Override
    public List<UserItem> getUserItemsByItemJid(String itemJid) {
        return userItemDao.getByItemJid(itemJid).stream().map(u -> createFromModel(u)).collect(Collectors.toList());
    }

    private UserItem createFromModel(UserItemModel u) {
        return new UserItem(u.userJid, u.itemJid, UserItemStatus.valueOf(u.status));
    }
}
