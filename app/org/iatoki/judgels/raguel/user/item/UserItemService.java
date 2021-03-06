package org.iatoki.judgels.raguel.user.item;

import com.google.inject.ImplementedBy;

import java.util.List;

@ImplementedBy(UserItemServiceImpl.class)
public interface UserItemService {

    boolean userItemExistsByUserJidAndItemJid(String userJid, String itemJid);

    boolean userItemExistsByUserJidAndItemJidAndStatus(String userJid, String itemJid, UserItemStatus status);

    void upsertUserItem(String userJid, String itemJid, UserItemStatus status);

    List<UserItem> getUserItemsByUserJid(String userJid);

    List<UserItem> getUserItemsByItemJid(String itemJid);
}
