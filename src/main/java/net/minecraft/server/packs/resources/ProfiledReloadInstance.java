package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProfiledReloadInstance extends SimpleReloadInstance<ProfiledReloadInstance.State> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Stopwatch total = Stopwatch.createUnstarted();

    public ProfiledReloadInstance(
        ResourceManager param0, List<PreparableReloadListener> param1, Executor param2, Executor param3, CompletableFuture<Unit> param4
    ) {
        super(
            param2,
            param3,
            param0,
            param1,
            (param1x, param2x, param3x, param4x, param5) -> {
                AtomicLong var0 = new AtomicLong();
                AtomicLong var1x = new AtomicLong();
                ActiveProfiler var2x = new ActiveProfiler(Util.getNanos(), () -> 0, false);
                ActiveProfiler var3x = new ActiveProfiler(Util.getNanos(), () -> 0, false);
                CompletableFuture<Void> var4x = param3x.reload(param1x, param2x, var2x, var3x, param2xx -> param4x.execute(() -> {
                        long var0x = Util.getNanos();
                        param2xx.run();
                        var0.addAndGet(Util.getNanos() - var0x);
                    }), param2xx -> param5.execute(() -> {
                        long var0x = Util.getNanos();
                        param2xx.run();
                        var1x.addAndGet(Util.getNanos() - var0x);
                    }));
                return var4x.thenApplyAsync(
                    param5x -> new ProfiledReloadInstance.State(param3x.getName(), var2x.getResults(), var3x.getResults(), var0, var1x), param3
                );
            },
            param4
        );
        this.total.start();
        this.allDone.thenAcceptAsync(this::finish, param3);
    }

    private void finish(List<ProfiledReloadInstance.State> param0x) {
        this.total.stop();
        int var0 = 0;
        LOGGER.info("Resource reload finished after " + this.total.elapsed(TimeUnit.MILLISECONDS) + " ms");

        for(ProfiledReloadInstance.State var1 : param0x) {
            ProfileResults var2 = var1.preparationResult;
            ProfileResults var3 = var1.reloadResult;
            int var4 = (int)((double)var1.preparationNanos.get() / 1000000.0);
            int var5 = (int)((double)var1.reloadNanos.get() / 1000000.0);
            int var6 = var4 + var5;
            String var7 = var1.name;
            LOGGER.info(var7 + " took approximately " + var6 + " ms (" + var4 + " ms preparing, " + var5 + " ms applying)");
            var0 += var5;
        }

        LOGGER.info("Total blocking time: " + var0 + " ms");
    }

    public static class State {
        private final String name;
        private final ProfileResults preparationResult;
        private final ProfileResults reloadResult;
        private final AtomicLong preparationNanos;
        private final AtomicLong reloadNanos;

        private State(String param0, ProfileResults param1, ProfileResults param2, AtomicLong param3, AtomicLong param4) {
            this.name = param0;
            this.preparationResult = param1;
            this.reloadResult = param2;
            this.preparationNanos = param3;
            this.reloadNanos = param4;
        }
    }
}
