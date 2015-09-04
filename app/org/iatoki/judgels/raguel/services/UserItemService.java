package org.iatoki.judgels.raguel.services;

import org.iatoki.judgels.raguel.UserItem;
import org.iatoki.judgels.raguel.UserItemStatus;

import java.util.List;

public interface UserItemService {

    boolean userItemExistsByUserJidAndItemJid(String userJid, String itemJid);

    boolean userItemExistsByUserJidAndItemJidAndStatus(String userJid, String itemJid, UserItemStatus status);

    void upsertUserItem(String userJid, String itemJid, UserItemStatus status);

    List<UserItem> getUserItemsByUserJid(String userJid);

    List<UserItem> getUserItemsByItemJid(String itemJid);
}
