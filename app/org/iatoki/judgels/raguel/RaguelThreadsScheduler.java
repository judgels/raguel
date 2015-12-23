package org.iatoki.judgels.raguel;

import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import org.iatoki.judgels.api.jophiel.JophielClientAPI;
import org.iatoki.judgels.jophiel.runnables.UserActivityMessagePusher;
import org.iatoki.judgels.jophiel.services.impls.UserActivityMessageServiceImpl;
import play.db.jpa.JPAApi;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class RaguelThreadsScheduler {

    @Inject
    public RaguelThreadsScheduler(ActorSystem actorSystem, JPAApi jpaApi, JophielClientAPI jophielClientAPI) {
        Scheduler scheduler = actorSystem.scheduler();
        ExecutionContextExecutor context = actorSystem.dispatcher();

        UserActivityMessagePusher userActivityMessagePusher = new UserActivityMessagePusher(jpaApi, jophielClientAPI, UserActivityMessageServiceImpl.getInstance());

        scheduler.schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES), userActivityMessagePusher, context);
    }
}
