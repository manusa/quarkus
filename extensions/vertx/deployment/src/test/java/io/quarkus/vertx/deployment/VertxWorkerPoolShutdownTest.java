package io.quarkus.vertx.deployment;

import java.util.concurrent.ExecutorService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.QuarkusUnitTest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class VertxWorkerPoolShutdownTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(MyBean.class));

    @Test
    public void test() {
        MyBean bean = Arc.container().instance(MyBean.class).get();
        Assertions.assertTrue(bean.isOk());
    }

    @ApplicationScoped
    public static class MyBean {

        @Inject
        Vertx vertx;

        @Inject
        ExecutorService executorService;

        boolean ok;

        public boolean isOk() {
            return ok;
        }

        public void init(@Observes StartupEvent ev) {
            executorService.shutdownNow();
            ((io.vertx.core.impl.ContextInternal) vertx.getOrCreateContext()).workerPool().executor().shutdownNow();
            Future<Boolean> ok1 = vertx.executeBlocking(() -> true);
            ok = ok1.toCompletionStage().toCompletableFuture().join();
        }
    }
}
