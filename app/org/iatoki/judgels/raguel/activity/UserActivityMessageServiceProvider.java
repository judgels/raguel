package org.iatoki.judgels.raguel.activity;

import org.iatoki.judgels.jophiel.activity.UserActivityMessageService;
import org.iatoki.judgels.jophiel.activity.UserActivityMessageServiceImpl;

import javax.inject.Provider;

public final class UserActivityMessageServiceProvider implements Provider<UserActivityMessageService> {

    @Override
    public UserActivityMessageService get() {
        return UserActivityMessageServiceImpl.getInstance();
    }
}
