package org.iatoki.judgels.raguel;

import org.iatoki.judgels.jophiel.services.BaseAvatarCacheService;
import org.iatoki.judgels.raguel.services.impls.AvatarCacheServiceImpl;

import javax.inject.Provider;

public final class AvatarCacheServiceProvider implements Provider<BaseAvatarCacheService> {

    @Override
    public BaseAvatarCacheService get() {
        return AvatarCacheServiceImpl.getInstance();
    }
}
