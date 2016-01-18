package org.iatoki.judgels.raguel.jid;

import org.iatoki.judgels.play.jid.BaseJidCacheService;

import javax.inject.Provider;

public final class JidCacheServiceProvider implements Provider<BaseJidCacheService> {

    @Override
    public BaseJidCacheService get() {
        return JidCacheServiceImpl.getInstance();
    }
}
