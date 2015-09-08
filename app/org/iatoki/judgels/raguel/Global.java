package org.iatoki.judgels.raguel;

import akka.actor.Scheduler;
import org.iatoki.judgels.jophiel.Jophiel;
import org.iatoki.judgels.jophiel.runnables.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import org.iatoki.judgels.play.AbstractGlobal;
import org.iatoki.judgels.play.services.BaseDataMigrationService;
import org.iatoki.judgels.raguel.controllers.RaguelControllerUtils;
import org.iatoki.judgels.raguel.models.daos.AvatarCacheDao;
import org.iatoki.judgels.raguel.models.daos.JidCacheDao;
import org.iatoki.judgels.raguel.services.UserService;
import org.iatoki.judgels.raguel.services.impls.AvatarCacheServiceImpl;
import org.iatoki.judgels.raguel.services.impls.JidCacheServiceImpl;
import org.iatoki.judgels.raguel.services.impls.RaguelDataMigrationServiceImpl;
import play.Application;
import play.inject.Injector;
import play.libs.Akka;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public final class Global extends AbstractGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        buildServices(application.injector());
        buildUtils(application.injector());
        scheduleThreads(application.injector());
    }

    @Override
    protected BaseDataMigrationService getDataMigrationService() {
        return new RaguelDataMigrationServiceImpl();
    }

    private void buildServices(Injector injector) {
        JidCacheServiceImpl.buildInstance(injector.instanceOf(JidCacheDao.class));
        AvatarCacheServiceImpl.buildInstance(injector.instanceOf(Jophiel.class), injector.instanceOf(AvatarCacheDao.class));
        UserActivityMessageServiceImpl.buildInstance();
    }

    private void buildUtils(Injector injector) {
        RaguelControllerUtils.buildInstance(injector.instanceOf(Jophiel.class));
    }

    private void scheduleThreads(Injector injector) {
        Scheduler scheduler = Akka.system().scheduler();
        ExecutionContextExecutor context = Akka.system().dispatcher();

        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(injector.instanceOf(Jophiel.class), injector.instanceOf(UserService.class), UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
    }
}
