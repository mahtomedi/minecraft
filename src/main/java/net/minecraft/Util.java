package net.minecraft;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ExecutorService BACKGROUND_EXECUTOR = makeBackgroundExecutor();
    public static LongSupplier timeSource = System::nanoTime;
    private static final Logger LOGGER = LogManager.getLogger();

    public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Entry::getKey, Entry::getValue);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> param0, Object param1) {
        return param0.getName((T)param1);
    }

    public static String makeDescriptionId(String param0, @Nullable ResourceLocation param1) {
        return param1 == null ? param0 + ".unregistered_sadface" : param0 + '.' + param1.getNamespace() + '.' + param1.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return getNanos() / 1000000L;
    }

    public static long getNanos() {
        return timeSource.getAsLong();
    }

    public static long getEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    private static ExecutorService makeBackgroundExecutor() {
        int var0 = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
        ExecutorService var1;
        if (var0 <= 0) {
            var1 = MoreExecutors.newDirectExecutorService();
        } else {
            var1 = new ForkJoinPool(var0, param0 -> {
                ForkJoinWorkerThread var0x = new ForkJoinWorkerThread(param0) {
                    @Override
                    protected void onTermination(Throwable param0) {
                        if (param0 != null) {
                            Util.LOGGER.warn("{} died", this.getName(), param0);
                        } else {
                            Util.LOGGER.debug("{} shutdown", this.getName());
                        }

                        super.onTermination(param0);
                    }
                };
                var0x.setName("Worker-" + WORKER_COUNT.getAndIncrement());
                return var0x;
            }, (param0, param1) -> {
                pauseInIde(param1);
                if (param1 instanceof CompletionException) {
                    param1 = param1.getCause();
                }

                if (param1 instanceof ReportedException) {
                    Bootstrap.realStdoutPrintln(((ReportedException)param1).getReport().getFriendlyReport());
                    System.exit(-1);
                }

                LOGGER.error(String.format("Caught exception in thread %s", param0), param1);
            }, true);
        }

        return var1;
    }

    public static Executor backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static void shutdownBackgroundExecutor() {
        BACKGROUND_EXECUTOR.shutdown();

        boolean var0;
        try {
            var0 = BACKGROUND_EXECUTOR.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException var21) {
            var0 = false;
        }

        if (!var0) {
            BACKGROUND_EXECUTOR.shutdownNow();
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static <T> CompletableFuture<T> failedFuture(Throwable param0) {
        CompletableFuture<T> var0 = new CompletableFuture<>();
        var0.completeExceptionally(param0);
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static void throwAsRuntime(Throwable param0) {
        throw param0 instanceof RuntimeException ? (RuntimeException)param0 : new RuntimeException(param0);
    }

    public static Util.OS getPlatform() {
        String var0 = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (var0.contains("win")) {
            return Util.OS.WINDOWS;
        } else if (var0.contains("mac")) {
            return Util.OS.OSX;
        } else if (var0.contains("solaris")) {
            return Util.OS.SOLARIS;
        } else if (var0.contains("sunos")) {
            return Util.OS.SOLARIS;
        } else if (var0.contains("linux")) {
            return Util.OS.LINUX;
        } else {
            return var0.contains("unix") ? Util.OS.LINUX : Util.OS.UNKNOWN;
        }
    }

    public static Stream<String> getVmArguments() {
        RuntimeMXBean var0 = ManagementFactory.getRuntimeMXBean();
        return var0.getInputArguments().stream().filter(param0 -> param0.startsWith("-X"));
    }

    public static <T> T lastOf(List<T> param0) {
        return param0.get(param0.size() - 1);
    }

    public static <T> T findNextInIterable(Iterable<T> param0, @Nullable T param1) {
        Iterator<T> var0 = param0.iterator();
        T var1 = var0.next();
        if (param1 != null) {
            T var2 = var1;

            while(var2 != param1) {
                if (var0.hasNext()) {
                    var2 = var0.next();
                }
            }

            if (var0.hasNext()) {
                return var0.next();
            }
        }

        return var1;
    }

    public static <T> T findPreviousInIterable(Iterable<T> param0, @Nullable T param1) {
        Iterator<T> var0 = param0.iterator();

        T var1;
        T var2;
        for(var1 = null; var0.hasNext(); var1 = var2) {
            var2 = var0.next();
            if (var2 == param1) {
                if (var1 == null) {
                    var1 = (T)(var0.hasNext() ? Iterators.getLast(var0) : param1);
                }
                break;
            }
        }

        return var1;
    }

    public static <T> T make(Supplier<T> param0) {
        return param0.get();
    }

    public static <T> T make(T param0, Consumer<T> param1) {
        param1.accept(param0);
        return param0;
    }

    public static <K> Strategy<K> identityStrategy() {
        return Util.IdentityStrategy.INSTANCE;
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<? extends V>> param0) {
        List<V> var0 = Lists.newArrayListWithCapacity(param0.size());
        CompletableFuture<?>[] var1 = new CompletableFuture[param0.size()];
        CompletableFuture<Void> var2 = new CompletableFuture<>();
        param0.forEach(param3 -> {
            int var0x = var0.size();
            var0.add((V)null);
            var1[var0x] = param3.whenComplete((param3x, param4) -> {
                if (param4 != null) {
                    var2.completeExceptionally(param4);
                } else {
                    var0.set(var0x, param3x);
                }

            });
        });
        return CompletableFuture.allOf(var1).applyToEither(var2, param1 -> var0);
    }

    public static <T> Stream<T> toStream(Optional<? extends T> param0) {
        return DataFixUtils.orElseGet(param0.map(Stream::of), Stream::empty);
    }

    public static <T> Optional<T> ifElse(Optional<T> param0, Consumer<T> param1, Runnable param2) {
        if (param0.isPresent()) {
            param1.accept(param0.get());
        } else {
            param2.run();
        }

        return param0;
    }

    public static Runnable name(Runnable param0, Supplier<String> param1) {
        return param0;
    }

    public static <T extends Throwable> T pauseInIde(T param0) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", param0);

            while(true) {
                try {
                    Thread.sleep(1000L);
                    LOGGER.error("paused");
                } catch (InterruptedException var2) {
                    return param0;
                }
            }
        } else {
            return param0;
        }
    }

    public static String describeError(Throwable param0) {
        if (param0.getCause() != null) {
            return describeError(param0.getCause());
        } else {
            return param0.getMessage() != null ? param0.getMessage() : param0.toString();
        }
    }

    public static <T> T getRandom(T[] param0, Random param1) {
        return param0[param1.nextInt(param0.length)];
    }

    public static int getRandom(int[] param0, Random param1) {
        return param0[param1.nextInt(param0.length)];
    }

    public static void safeReplaceFile(File param0, File param1, File param2) {
        if (param2.exists()) {
            param2.delete();
        }

        param0.renameTo(param2);
        if (param0.exists()) {
            param0.delete();
        }

        param1.renameTo(param0);
        if (param1.exists()) {
            param1.delete();
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static int offsetByCodepoints(String param0, int param1, int param2) {
        int var0 = param0.length();
        if (param2 >= 0) {
            for(int var1 = 0; param1 < var0 && var1 < param2; ++var1) {
                if (Character.isHighSurrogate(param0.charAt(param1++)) && param1 < var0 && Character.isLowSurrogate(param0.charAt(param1))) {
                    ++param1;
                }
            }
        } else {
            for(int var2 = param2; param1 > 0 && var2 < 0; ++var2) {
                --param1;
                if (Character.isLowSurrogate(param0.charAt(param1)) && param1 > 0 && Character.isHighSurrogate(param0.charAt(param1 - 1))) {
                    --param1;
                }
            }
        }

        return param1;
    }

    static enum IdentityStrategy implements Strategy<Object> {
        INSTANCE;

        @Override
        public int hashCode(Object param0) {
            return System.identityHashCode(param0);
        }

        @Override
        public boolean equals(Object param0, Object param1) {
            return param0 == param1;
        }
    }

    public static enum OS {
        LINUX,
        SOLARIS,
        WINDOWS {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected String[] getOpenUrlArguments(URL param0) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", param0.toString()};
            }
        },
        OSX {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected String[] getOpenUrlArguments(URL param0) {
                return new String[]{"open", param0.toString()};
            }
        },
        UNKNOWN;

        private OS() {
        }

        @OnlyIn(Dist.CLIENT)
        public void openUrl(URL param0) {
            try {
                Process var0 = AccessController.doPrivileged(
                    (PrivilegedExceptionAction<Process>)(() -> Runtime.getRuntime().exec(this.getOpenUrlArguments(param0)))
                );

                for(String var1 : IOUtils.readLines(var0.getErrorStream())) {
                    Util.LOGGER.error(var1);
                }

                var0.getInputStream().close();
                var0.getErrorStream().close();
                var0.getOutputStream().close();
            } catch (IOException | PrivilegedActionException var5) {
                Util.LOGGER.error("Couldn't open url '{}'", param0, var5);
            }

        }

        @OnlyIn(Dist.CLIENT)
        public void openUri(URI param0) {
            try {
                this.openUrl(param0.toURL());
            } catch (MalformedURLException var3) {
                Util.LOGGER.error("Couldn't open uri '{}'", param0, var3);
            }

        }

        @OnlyIn(Dist.CLIENT)
        public void openFile(File param0) {
            try {
                this.openUrl(param0.toURI().toURL());
            } catch (MalformedURLException var3) {
                Util.LOGGER.error("Couldn't open file '{}'", param0, var3);
            }

        }

        @OnlyIn(Dist.CLIENT)
        protected String[] getOpenUrlArguments(URL param0) {
            String var0 = param0.toString();
            if ("file".equals(param0.getProtocol())) {
                var0 = var0.replace("file:", "file://");
            }

            return new String[]{"xdg-open", var0};
        }

        @OnlyIn(Dist.CLIENT)
        public void openUri(String param0) {
            try {
                this.openUrl(new URI(param0).toURL());
            } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
                Util.LOGGER.error("Couldn't open uri '{}'", param0, var3);
            }

        }
    }
}
