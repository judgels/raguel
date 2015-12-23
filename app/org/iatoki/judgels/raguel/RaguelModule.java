package org.iatoki.judgels.raguel;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.api.jophiel.JophielFactory;
import org.iatoki.judgels.api.jophiel.JophielPublicAPI;
import org.iatoki.judgels.jophiel.JophielAuthAPI;
import org.iatoki.judgels.jophiel.services.BaseUserService;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.play.general.GeneralName;
import org.iatoki.judgels.play.general.GeneralVersion;
import org.iatoki.judgels.play.migration.BaseDataMigrationService;
import org.iatoki.judgels.raguel.services.impls.RaguelDataMigrationServiceImpl;
import org.iatoki.judgels.raguel.services.impls.UserServiceImpl;

public class RaguelModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        org.iatoki.judgels.raguel.BuildInfo$ buildInfo = org.iatoki.judgels.raguel.BuildInfo$.MODULE$;

        bindConstant().annotatedWith(GeneralName.class).to(buildInfo.name());
        bindConstant().annotatedWith(GeneralVersion.class).to(buildInfo.version());

        // <DEPRECATED>
        Config config = ConfigFactory.load();
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), config);
        RaguelProperties.buildInstance(config);
        bind(RaguelSingletonsBuilder.class).asEagerSingleton();
        bind(RaguelThreadsScheduler.class).asEagerSingleton();
        // </DEPRECATED>

        bind(BaseDataMigrationService.class).to(RaguelDataMigrationServiceImpl.class);

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
