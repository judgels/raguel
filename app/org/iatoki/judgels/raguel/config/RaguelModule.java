package org.iatoki.judgels.raguel.config;

import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.raguel.RaguelProperties;
import org.iatoki.judgels.raguel.services.impls.UserServiceImpl;

public final class RaguelModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        bind(Jophiel.class).toInstance(jophiel());
        bind(BaseUserService.class).to(UserServiceImpl.class);
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.raguel.models.daos.impls";
    }

    @Override
    protected String getServicesImplPackage() {
        return "org.iatoki.judgels.raguel.services.impls";
    }

    private RaguelProperties raguelProperties() {
        return RaguelProperties.getInstance();
    }

    private Jophiel jophiel() {
        return new Jophiel(raguelProperties().getJophielBaseUrl(), raguelProperties().getJophielClientJid(), raguelProperties().getJophielClientSecret());
    }
}
