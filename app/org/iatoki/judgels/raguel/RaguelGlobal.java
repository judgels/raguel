package org.iatoki.judgels.raguel;

import org.iatoki.judgels.play.AbstractJudgelsGlobal;
import play.Application;

public final class RaguelGlobal extends AbstractJudgelsGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        application.injector().instanceOf(RaguelThreadsScheduler.class);
    }
}
