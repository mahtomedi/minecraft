package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ExecutorService BOOTSTRAP_EXECUTOR = makeExecutor("Bootstrap");
    private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
    private static final ExecutorService IO_POOL = makeIoExecutor();
    public static LongSupplier timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker() {
        @Override
        public long read() {
            return Util.timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders()
        .stream()
        .filter(param0 -> param0.getScheme().equalsIgnoreCase("jar"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> thePauser = param0 -> {
    };

    public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Entry::getKey, Entry::getValue);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> param0, Object param1) {
        return param0.getName((T)param1);
    }

    public static String makeDescriptionId(String param0, @Nullable ResourceLocation param1) {
        return param1 == null ? param0 + ".unregistered_sadface" : param0 + "." + param1.getNamespace() + "." + param1.getPath().replace('/', '.');
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

    private static ExecutorService makeExecutor(String param0) {
        int var0 = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
        ExecutorService var1;
        if (var0 <= 0) {
            var1 = MoreExecutors.newDirectExecutorService();
        } else {
            var1 = new ForkJoinPool(var0, param1 -> {
                ForkJoinWorkerThread var0x = new ForkJoinWorkerThread(param1) {
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
                var0x.setName("Worker-" + param0 + "-" + WORKER_COUNT.getAndIncrement());
                return var0x;
            }, Util::onThreadException, true);
        }

        return var1;
    }

    private static int getMaxThreads() {
        String var0 = System.getProperty("max.bg.threads");
        if (var0 != null) {
            try {
                int var1 = Integer.parseInt(var0);
                if (var1 >= 1 && var1 <= 255) {
                    return var1;
                }

                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", var0, 255);
            } catch (NumberFormatException var21) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", var0, 255);
            }
        }

        return 255;
    }

    public static ExecutorService bootstrapExecutor() {
        return BOOTSTRAP_EXECUTOR;
    }

    public static ExecutorService backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static ExecutorService ioPool() {
        return IO_POOL;
    }

    public static void shutdownExecutors() {
        shutdownExecutor(BACKGROUND_EXECUTOR);
        shutdownExecutor(IO_POOL);
    }

    private static void shutdownExecutor(ExecutorService param0) {
        param0.shutdown();

        boolean var0;
        try {
            var0 = param0.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException var3) {
            var0 = false;
        }

        if (!var0) {
            param0.shutdownNow();
        }

    }

    private static ExecutorService makeIoExecutor() {
        return Executors.newCachedThreadPool(param0 -> {
            Thread var0 = new Thread(param0);
            var0.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
            var0.setUncaughtExceptionHandler(Util::onThreadException);
            return var0;
        });
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable param0) {
        CompletableFuture<T> var0 = new CompletableFuture<>();
        var0.completeExceptionally(param0);
        return var0;
    }

    public static void throwAsRuntime(Throwable param0) {
        throw param0 instanceof RuntimeException ? (RuntimeException)param0 : new RuntimeException(param0);
    }

    private static void onThreadException(Thread param0x, Throwable param1) {
        pauseInIde(param1);
        if (param1 instanceof CompletionException) {
            param1 = param1.getCause();
        }

        if (param1 instanceof ReportedException) {
            Bootstrap.realStdoutPrintln(((ReportedException)param1).getReport().getFriendlyReport());
            System.exit(-1);
        }

        LOGGER.error(String.format("Caught exception in thread %s", param0x), param1);
    }

    @Nullable
    public static Type<?> fetchChoiceType(TypeReference param0, String param1) {
        return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(param0, param1);
    }

    @Nullable
    private static Type<?> doFetchChoiceType(TypeReference param0, String param1) {
        Type<?> var0 = null;

        try {
            var0 = DataFixers.getDataFixer()
                .getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion()))
                .getChoiceType(param0, param1);
        } catch (IllegalArgumentException var4) {
            LOGGER.error("No data fixer registered for {}", param1);
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw var4;
            }
        }

        return var0;
    }

    public static Runnable wrapThreadWithTaskName(String param0, Runnable param1) {
        return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
            Thread var0x = Thread.currentThread();
            String var1x = var0x.getName();
            var0x.setName(param0);

            try {
                param1.run();
            } finally {
                var0x.setName(var1x);
            }

        } : param1;
    }

    public static <V> Supplier<V> wrapThreadWithTaskName(String param0, Supplier<V> param1) {
        return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
            Thread var0x = Thread.currentThread();
            String var1x = var0x.getName();
            var0x.setName(param0);

            Object var4;
            try {
                var4 = param1.get();
            } finally {
                var0x.setName(var1x);
            }

            return (V)var4;
        } : param1;
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

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> param0) {
        if (param0.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        } else if (param0.size() == 1) {
            return param0.get(0).thenApply(List::of);
        } else {
            CompletableFuture<Void> var0 = CompletableFuture.allOf(param0.toArray(new CompletableFuture[0]));
            return var0.thenApply(param1 -> param0.stream().map(CompletableFuture::join).toList());
        }
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> param0) {
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

    public static <T> Optional<T> ifElse(Optional<T> param0, Consumer<T> param1, Runnable param2) {
        if (param0.isPresent()) {
            param1.accept(param0.get());
        } else {
            param2.run();
        }

        return param0;
    }

    public static <T> Supplier<T> name(Supplier<T> param0, Supplier<String> param1) {
        return param0;
    }

    public static Runnable name(Runnable param0, Supplier<String> param1) {
        return param0;
    }

    public static void logAndPauseIfInIde(String param0) {
        LOGGER.error(param0);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            doPause(param0);
        }

    }

    public static void logAndPauseIfInIde(String param0, Throwable param1) {
        LOGGER.error(param0, param1);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            doPause(param0);
        }

    }

    public static <T extends Throwable> T pauseInIde(T param0) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", param0);
            doPause(param0.getMessage());
        }

        return param0;
    }

    public static void setPause(Consumer<String> param0) {
        thePauser = param0;
    }

    private static void doPause(String param0) {
        Instant var0 = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean var1 = Duration.between(var0, Instant.now()).toMillis() > 500L;
        if (!var1) {
            thePauser.accept(param0);
        }

    }

    public static String describeError(Throwable param0) {
        if (param0.getCause() != null) {
            return describeError(param0.getCause());
        } else {
            return param0.getMessage() != null ? param0.getMessage() : param0.toString();
        }
    }

    public static <T> T getRandom(T[] param0, RandomSource param1) {
        return param0[param1.nextInt(param0.length)];
    }

    public static int getRandom(int[] param0, RandomSource param1) {
        return param0[param1.nextInt(param0.length)];
    }

    public static <T> T getRandom(List<T> param0, RandomSource param1) {
        return param0.get(param1.nextInt(param0.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> param0, RandomSource param1) {
        return param0.isEmpty() ? Optional.empty() : Optional.of(getRandom(param0, param1));
    }

    private static BooleanSupplier createRenamer(final Path param0, final Path param1) {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(param0, param1);
                    return true;
                } catch (IOException var2) {
                    Util.LOGGER.error("Failed to rename", (Throwable)var2);
                    return false;
                }
            }

            @Override
            public String toString() {
                return "rename " + param0 + " to " + param1;
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path param0) {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(param0);
                    return true;
                } catch (IOException var2) {
                    Util.LOGGER.warn("Failed to delete", (Throwable)var2);
                    return false;
                }
            }

            @Override
            public String toString() {
                return "delete old " + param0;
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path param0) {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return !Files.exists(param0);
            }

            @Override
            public String toString() {
                return "verify that " + param0 + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path param0) {
        return new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(param0);
            }

            @Override
            public String toString() {
                return "verify that " + param0 + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier... param0) {
        for(BooleanSupplier var0 : param0) {
            if (!var0.getAsBoolean()) {
                LOGGER.warn("Failed to execute {}", var0);
                return false;
            }
        }

        return true;
    }

    private static boolean runWithRetries(int param0, String param1, BooleanSupplier... param2) {
        for(int var0 = 0; var0 < param0; ++var0) {
            if (executeInSequence(param2)) {
                return true;
            }

            LOGGER.error("Failed to {}, retrying {}/{}", param1, var0, param0);
        }

        LOGGER.error("Failed to {}, aborting, progress might be lost", param1);
        return false;
    }

    public static void safeReplaceFile(File param0, File param1, File param2) {
        safeReplaceFile(param0.toPath(), param1.toPath(), param2.toPath());
    }

    public static void safeReplaceFile(Path param0, Path param1, Path param2) {
        safeReplaceOrMoveFile(param0, param1, param2, false);
    }

    public static void safeReplaceOrMoveFile(File param0, File param1, File param2, boolean param3) {
        safeReplaceOrMoveFile(param0.toPath(), param1.toPath(), param2.toPath(), param3);
    }

    public static void safeReplaceOrMoveFile(Path param0, Path param1, Path param2, boolean param3) {
        int var0 = 10;
        if (!Files.exists(param0)
            || runWithRetries(10, "create backup " + param2, createDeleter(param2), createRenamer(param0, param2), createFileCreatedCheck(param2))) {
            if (runWithRetries(10, "remove old " + param0, createDeleter(param0), createFileDeletedCheck(param0))) {
                if (!runWithRetries(10, "replace " + param0 + " with " + param1, createRenamer(param1, param0), createFileCreatedCheck(param0)) && !param3) {
                    runWithRetries(10, "restore " + param0 + " from " + param2, createRenamer(param2, param0), createFileCreatedCheck(param0));
                }

            }
        }
    }

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

    public static Consumer<String> prefix(String param0, Consumer<String> param1) {
        return param2 -> param1.accept(param0 + param2);
    }

    public static DataResult<int[]> fixedSize(IntStream param0, int param1) {
        int[] var0 = param0.limit((long)(param1 + 1)).toArray();
        if (var0.length != param1) {
            String var1 = "Input is not a list of " + param1 + " ints";
            return var0.length >= param1 ? DataResult.error(var1, Arrays.copyOf(var0, param1)) : DataResult.error(var1);
        } else {
            return DataResult.success(var0);
        }
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> param0, int param1) {
        if (param0.size() != param1) {
            String var0 = "Input is not a list of " + param1 + " elements";
            return param0.size() >= param1 ? DataResult.error(var0, param0.subList(0, param1)) : DataResult.error(var0);
        } else {
            return DataResult.success(param0);
        }
    }

    public static void startTimerHackThread() {
        Thread var0 = new Thread("Timer hack thread") {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException var2) {
                        Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                        return;
                    }
                }
            }
        };
        var0.setDaemon(true);
        var0.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
    }

    public static void copyBetweenDirs(Path param0, Path param1, Path param2) throws IOException {
        Path var0 = param0.relativize(param2);
        Path var1 = param1.resolve(var0);
        Files.copy(param2, var1);
    }

    public static String sanitizeName(String param0, CharPredicate param1) {
        return param0.toLowerCase(Locale.ROOT)
            .chars()
            .mapToObj(param1x -> param1.test((char)param1x) ? Character.toString((char)param1x) : "_")
            .collect(Collectors.joining());
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> param0) {
        return new Function<T, R>() {
            private final Map<T, R> cache = Maps.newHashMap();

            @Override
            public R apply(T param0x) {
                return this.cache.computeIfAbsent(param0, param0);
            }

            @Override
            public String toString() {
                return "memoize/1[function=" + param0 + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> param0) {
        return new BiFunction<T, U, R>() {
            private final Map<Pair<T, U>, R> cache = Maps.newHashMap();

            @Override
            public R apply(T param0x, U param1) {
                return this.cache.computeIfAbsent(Pair.of(param0, param1), param1x -> param0.apply(param1x.getFirst(), param1x.getSecond()));
            }

            @Override
            public String toString() {
                return "memoize/2[function=" + param0 + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> shuffledCopy(List<T> param0, RandomSource param1) {
        List<T> var0 = new ArrayList<>(param0);
        shuffle(var0, param1);
        return var0;
    }

    public static <T> void shuffle(List<T> param0, RandomSource param1) {
        int var0 = param0.size();

        for(int var1 = var0; var1 > 1; --var1) {
            int var2 = param1.nextInt(var1);
            param0.set(var1 - 1, param0.set(var2, param0.get(var1 - 1)));
        }

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
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows") {
            @Override
            protected String[] getOpenUrlArguments(URL param0) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", param0.toString()};
            }
        },
        OSX("mac") {
            @Override
            protected String[] getOpenUrlArguments(URL param0) {
                return new String[]{"open", param0.toString()};
            }
        },
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(String param0) {
            this.telemetryName = param0;
        }

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

        public void openUri(URI param0) {
            try {
                this.openUrl(param0.toURL());
            } catch (MalformedURLException var3) {
                Util.LOGGER.error("Couldn't open uri '{}'", param0, var3);
            }

        }

        public void openFile(File param0) {
            try {
                this.openUrl(param0.toURI().toURL());
            } catch (MalformedURLException var3) {
                Util.LOGGER.error("Couldn't open file '{}'", param0, var3);
            }

        }

        protected String[] getOpenUrlArguments(URL param0) {
            String var0 = param0.toString();
            if ("file".equals(param0.getProtocol())) {
                var0 = var0.replace("file:", "file://");
            }

            return new String[]{"xdg-open", var0};
        }

        public void openUri(String param0) {
            try {
                this.openUrl(new URI(param0).toURL());
            } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
                Util.LOGGER.error("Couldn't open uri '{}'", param0, var3);
            }

        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }
}
