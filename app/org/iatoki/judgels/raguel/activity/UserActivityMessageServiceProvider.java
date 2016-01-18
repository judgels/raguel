package org.iatoki.judgels.raguel.activity;

import org.iatoki.judgels.jophiel.services.UserActivityMessageService;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;

import javax.inject.Provider;

public final class UserActivityMessageServiceProvider implements Provider<UserActivityMessageService> {

    @Override
    public UserActivityMessageService get() {
        return UserActivityMessageServiceImpl.getInstance();
    }
}
