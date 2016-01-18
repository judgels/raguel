package org.iatoki.judgels.raguel;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.controllers.JophielClientControllerUtils;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.raguel.avatar.AvatarCacheDao;
import org.iatoki.judgels.raguel.jid.JidCacheDao;
import org.iatoki.judgels.raguel.forum.member.ForumMemberService;
import org.iatoki.judgels.raguel.avatar.AvatarCacheServiceImpl;
import org.iatoki.judgels.raguel.jid.JidCacheServiceImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class RaguelSingletonsBuilder {

    @Inject
    public RaguelSingletonsBuilder(JidCacheDao jidCacheDao, AvatarCacheDao avatarCacheDao, JophielClientAPI jophielClientAPI, JophielPublicAPI jophielPublicAPI, ForumMemberService forumMemberService) {
        JidCacheServiceImpl.buildInstance(jidCacheDao);
        AvatarCacheServiceImpl.buildInstance(avatarCacheDao);
        UserActivityMessageServiceImpl.buildInstance();

        JophielClientControllerUtils.buildInstance(RaguelProperties.getInstance().getJophielBaseUrl());
    }
}
