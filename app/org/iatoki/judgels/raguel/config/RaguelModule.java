package org.iatoki.judgels.raguel.config;

import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielFactory;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.JophielAuthAPI;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.raguel.RaguelProperties;
import org.iatoki.judgels.raguel.services.impls.UserServiceImpl;

public class RaguelModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        bind(JophielAuthAPI.class).toInstance(jophielAuthAPI());
        bind(JophielClientAPI.class).toInstance(jophielClientAPI());
        bind(JophielPublicAPI.class).toInstance(jophielPublicAPI());
        bind(BaseUserService.class).to(UserServiceImpl.class);
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.raguel.models.daos.hibernate";
    }

    @Override
    protected String getServicesImplPackage() {
        return "org.iatoki.judgels.raguel.services.impls";
    }

    private RaguelProperties raguelProperties() {
        return RaguelProperties.getInstance();
    }

    private JophielAuthAPI jophielAuthAPI() {
        return new JophielAuthAPI(raguelProperties().getJophielBaseUrl(), raguelProperties().getJophielClientJid(), raguelProperties().getJophielClientSecret());
    }

    private JophielClientAPI jophielClientAPI() {
        return JophielFactory.createJophiel(raguelProperties().getJophielBaseUrl()).connectToClientAPI(raguelProperties().getJophielClientJid(), raguelProperties().getJophielClientSecret());
    }

    private JophielPublicAPI jophielPublicAPI() {
        return JophielFactory.createJophiel(raguelProperties().getJophielBaseUrl()).connectToPublicAPI();
    }
}
