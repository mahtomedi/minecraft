package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final int DEFAULT_SAFE_FILE_OPERATION_RETRIES = 10;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
    private static final ExecutorService IO_POOL = makeIoExecutor("IO-Worker-", false);
    private static final ExecutorService DOWNLOAD_POOL = makeIoExecutor("Download-", true);
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    private static final int LINEAR_LOOKUP_THRESHOLD = 8;
    public static final long NANOS_PER_MILLI = 1000000L;
    public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
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

    public static String getFilenameFormattedDateTime() {
        return FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static ExecutorService makeExecutor(String param0) {
        int var0 = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
        ExecutorService var1;
        if (var0 <= 0) {
            var1 = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger var2 = new AtomicInteger(1);
            var1 = new ForkJoinPool(var0, param2 -> {
                ForkJoinWorkerThread var0x = new ForkJoinWorkerThread(param2) {
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
                var0x.setName("Worker-" + param0 + "-" + var2.getAndIncrement());
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

    public static ExecutorService backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static ExecutorService ioPool() {
        return IO_POOL;
    }

    public static ExecutorService nonCriticalIoPool() {
        return DOWNLOAD_POOL;
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

    private static ExecutorService makeIoExecutor(String param0, boolean param1) {
        AtomicInteger var0 = new AtomicInteger(1);
        return Executors.newCachedThreadPool(param3 -> {
            Thread var0x = new Thread(param3);
            var0x.setName(param0 + var0.getAndIncrement());
            var0x.setDaemon(param1);
            var0x.setUncaughtExceptionHandler(Util::onThreadException);
            return var0x;
        });
    }

    public static void throwAsRuntime(Throwable param0) {
        throw param0 instanceof RuntimeException ? (RuntimeException)param0 : new RuntimeException(param0);
    }

    private static void onThreadException(Thread param0x, Throwable param1) {
        pauseInIde(param1);
        if (param1 instanceof CompletionException) {
            param1 = param1.getCause();
        }

        if (param1 instanceof ReportedException var0x) {
            Bootstrap.realStdoutPrintln(var0x.getReport().getFriendlyReport());
            System.exit(-1);
        }

        LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", param0x), param1);
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
                .getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))
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

    public static <T> T make(T param0, Consumer<? super T> param1) {
        param1.accept(param0);
        return param0;
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
        CompletableFuture<List<V>> var0 = new CompletableFuture<>();
        return fallibleSequence(param0, var0::completeExceptionally).applyToEither(var0, Function.identity());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> param0) {
        CompletableFuture<List<V>> var0 = new CompletableFuture<>();
        return fallibleSequence(param0, param2 -> {
            if (var0.completeExceptionally(param2)) {
                for(CompletableFuture<? extends V> var0x : param0) {
                    var0x.cancel(true);
                }
            }

        }).applyToEither(var0, Function.identity());
    }

    private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> param0, Consumer<Throwable> param1) {
        List<V> var0 = Lists.newArrayListWithCapacity(param0.size());
        CompletableFuture<?>[] var1 = new CompletableFuture[param0.size()];
        param0.forEach(param3 -> {
            int var0x = var0.size();
            var0.add((V)null);
            var1[var0x] = param3.whenComplete((param3x, param4) -> {
                if (param4 != null) {
                    param1.accept(param4);
                } else {
                    var0.set(var0x, param3x);
                }

            });
        });
        return CompletableFuture.allOf(var1).thenApply(param1x -> var0);
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

    public static void safeReplaceFile(Path param0, Path param1, Path param2) {
        safeReplaceOrMoveFile(param0, param1, param2, false);
    }

    public static boolean safeReplaceOrMoveFile(Path param0, Path param1, Path param2, boolean param3) {
        if (Files.exists(param0)
            && !runWithRetries(10, "create backup " + param2, createDeleter(param2), createRenamer(param0, param2), createFileCreatedCheck(param2))) {
            return false;
        } else if (!runWithRetries(10, "remove old " + param0, createDeleter(param0), createFileDeletedCheck(param0))) {
            return false;
        } else if (!runWithRetries(10, "replace " + param0 + " with " + param1, createRenamer(param1, param0), createFileCreatedCheck(param0)) && !param3) {
            runWithRetries(10, "restore " + param0 + " from " + param2, createRenamer(param2, param0), createFileCreatedCheck(param0));
            return false;
        } else {
            return true;
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
            Supplier<String> var1 = () -> "Input is not a list of " + param1 + " ints";
            return var0.length >= param1 ? DataResult.error(var1, Arrays.copyOf(var0, param1)) : DataResult.error(var1);
        } else {
            return DataResult.success(var0);
        }
    }

    public static DataResult<long[]> fixedSize(LongStream param0, int param1) {
        long[] var0 = param0.limit((long)(param1 + 1)).toArray();
        if (var0.length != param1) {
            Supplier<String> var1 = () -> "Input is not a list of " + param1 + " longs";
            return var0.length >= param1 ? DataResult.error(var1, Arrays.copyOf(var0, param1)) : DataResult.error(var1);
        } else {
            return DataResult.success(var0);
        }
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> param0, int param1) {
        if (param0.size() != param1) {
            Supplier<String> var0 = () -> "Input is not a list of " + param1 + " elements";
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

    public static <K, V> SingleKeyCache<K, V> singleKeyCache(Function<K, V> param0) {
        return new SingleKeyCache<>(param0);
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> param0) {
        return new Function<T, R>() {
            private final Map<T, R> cache = new ConcurrentHashMap<>();

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
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap<>();

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

    public static <T> List<T> toShuffledList(Stream<T> param0, RandomSource param1) {
        ObjectArrayList<T> var0 = param0.collect(ObjectArrayList.toList());
        shuffle(var0, param1);
        return var0;
    }

    public static IntArrayList toShuffledList(IntStream param0, RandomSource param1) {
        IntArrayList var0 = IntArrayList.wrap(param0.toArray());
        int var1 = var0.size();

        for(int var2 = var1; var2 > 1; --var2) {
            int var3 = param1.nextInt(var2);
            var0.set(var2 - 1, var0.set(var3, var0.getInt(var2 - 1)));
        }

        return var0;
    }

    public static <T> List<T> shuffledCopy(T[] param0, RandomSource param1) {
        ObjectArrayList<T> var0 = new ObjectArrayList<>(param0);
        shuffle(var0, param1);
        return var0;
    }

    public static <T> List<T> shuffledCopy(ObjectArrayList<T> param0, RandomSource param1) {
        ObjectArrayList<T> var0 = new ObjectArrayList<>(param0);
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

    public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> param0) {
        return blockUntilDone(param0, CompletableFuture::isDone);
    }

    public static <T> T blockUntilDone(Function<Executor, T> param0, Predicate<T> param1) {
        BlockingQueue<Runnable> var0 = new LinkedBlockingQueue<>();
        T var1 = param0.apply(var0::add);

        while(!param1.test(var1)) {
            try {
                Runnable var2 = var0.poll(100L, TimeUnit.MILLISECONDS);
                if (var2 != null) {
                    var2.run();
                }
            } catch (InterruptedException var5) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }

        int var4 = var0.size();
        if (var4 > 0) {
            LOGGER.warn("Tasks left in queue: {}", var4);
        }

        return var1;
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> param0) {
        int var0 = param0.size();
        if (var0 < 8) {
            return param0::indexOf;
        } else {
            Object2IntMap<T> var1 = new Object2IntOpenHashMap<>(var0);
            var1.defaultReturnValue(-1);

            for(int var2 = 0; var2 < var0; ++var2) {
                var1.put(param0.get(var2), var2);
            }

            return var1;
        }
    }

    public static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> param0) {
        int var0 = param0.size();
        if (var0 < 8) {
            ReferenceList<T> var1 = new ReferenceImmutableList<>(param0);
            return var1::indexOf;
        } else {
            Reference2IntMap<T> var2 = new Reference2IntOpenHashMap<>(var0);
            var2.defaultReturnValue(-1);

            for(int var3 = 0; var3 < var0; ++var3) {
                var2.put(param0.get(var3), var3);
            }

            return var2;
        }
    }

    public static <T, E extends Throwable> T getOrThrow(DataResult<T> param0, Function<String, E> param1) throws E {
        Optional<PartialResult<T>> var0 = param0.error();
        if (var0.isPresent()) {
            throw param1.apply(var0.get().message());
        } else {
            return param0.result().orElseThrow();
        }
    }

    public static <T, E extends Throwable> T getPartialOrThrow(DataResult<T> param0, Function<String, E> param1) throws E {
        Optional<PartialResult<T>> var0 = param0.error();
        if (var0.isPresent()) {
            Optional<T> var1 = param0.resultOrPartial(param0x -> {
            });
            if (var1.isPresent()) {
                return var1.get();
            } else {
                throw param1.apply(var0.get().message());
            }
        } else {
            return param0.result().orElseThrow();
        }
    }

    public static <A, B> Typed<B> writeAndReadTypedOrThrow(Typed<A> param0, Type<B> param1, UnaryOperator<Dynamic<?>> param2) {
        Dynamic<?> var0 = getOrThrow(param0.write(), IllegalStateException::new);
        return readTypedOrThrow(param1, param2.apply(var0), true);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> param0, Dynamic<?> param1) {
        return readTypedOrThrow(param0, param1, false);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> param0, Dynamic<?> param1, boolean param2) {
        DataResult<Typed<T>> var0 = param0.readTyped(param1).map(Pair::getFirst);

        try {
            return param2 ? getPartialOrThrow(var0, IllegalStateException::new) : getOrThrow(var0, IllegalStateException::new);
        } catch (IllegalStateException var7) {
            CrashReport var2 = CrashReport.forThrowable(var7, "Reading type");
            CrashReportCategory var3 = var2.addCategory("Info");
            var3.setDetail("Data", param1);
            var3.setDetail("Type", param0);
            throw new ReportedException(var2);
        }
    }

    public static boolean isWhitespace(int param0) {
        return Character.isWhitespace(param0) || Character.isSpaceChar(param0);
    }

    public static boolean isBlank(@Nullable String param0) {
        return param0 != null && param0.length() != 0 ? param0.chars().allMatch(Util::isWhitespace) : true;
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
                var0.getInputStream().close();
                var0.getErrorStream().close();
                var0.getOutputStream().close();
            } catch (IOException | PrivilegedActionException var3) {
                Util.LOGGER.error("Couldn't open url '{}'", param0, var3);
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
