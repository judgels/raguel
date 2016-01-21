package org.iatoki.judgels.raguel.avatar;

import org.iatoki.judgels.jophiel.avatar.BaseAvatarCacheService;

import javax.inject.Provider;

public final class AvatarCacheServiceProvider implements Provider<BaseAvatarCacheService> {

    @Override
    public BaseAvatarCacheService get() {
        return AvatarCacheServiceImpl.getInstance();
    }
}
