package org.iatoki.judgels.raguel;

import org.iatoki.judgels.play.jid.BaseJidCacheService;
import org.iatoki.judgels.raguel.services.impls.JidCacheServiceImpl;

import javax.inject.Provider;

public final class JidCacheServiceProvider implements Provider<BaseJidCacheService> {

    @Override
    public BaseJidCacheService get() {
        return JidCacheServiceImpl.getInstance();
    }
}
