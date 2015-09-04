package org.iatoki.judgels.raguel.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.raguel.RaguelProperties;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

public final class RaguelApplicationLoader extends GuiceApplicationLoader {

    @Override
    public GuiceApplicationBuilder builder(Context context) {
        org.iatoki.judgels.raguel.BuildInfo$ buildInfo = org.iatoki.judgels.raguel.BuildInfo$.MODULE$;
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());

        Config config = ConfigFactory.load();
        RaguelProperties.buildInstance(config);

        return super.builder(context);
    }
}
