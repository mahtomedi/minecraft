package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.slf4j.Logger;

public class LevelStorageSource {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendLiteral('-')
        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(ChronoField.DAY_OF_MONTH, 2)
        .appendLiteral('_')
        .appendValue(ChronoField.HOUR_OF_DAY, 2)
        .appendLiteral('-')
        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
        .appendLiteral('-')
        .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
        .toFormatter();
    private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of(
        "RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest"
    );
    private static final String TAG_DATA = "Data";
    final Path baseDir;
    private final Path backupDir;
    final DataFixer fixerUpper;

    public LevelStorageSource(Path param0, Path param1, DataFixer param2) {
        this.fixerUpper = param2;

        try {
            FileUtil.createDirectoriesSafe(param0);
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        this.baseDir = param0;
        this.backupDir = param1;
    }

    public static LevelStorageSource createDefault(Path param0) {
        return new LevelStorageSource(param0, param0.resolve("../backups"), DataFixers.getDataFixer());
    }

    private static <T> DataResult<WorldGenSettings> readWorldGenSettings(Dynamic<T> param0, DataFixer param1, int param2) {
        Dynamic<T> var0 = param0.get("WorldGenSettings").orElseEmptyMap();

        for(String var1 : OLD_SETTINGS_KEYS) {
            Optional<? extends Dynamic<?>> var2 = param0.get(var1).result();
            if (var2.isPresent()) {
                var0 = var0.set(var1, var2.get());
            }
        }

        Dynamic<T> var3 = param1.update(References.WORLD_GEN_SETTINGS, var0, param2, SharedConstants.getCurrentVersion().getWorldVersion());
        return WorldGenSettings.CODEC.parse(var3);
    }

    private static WorldDataConfiguration readDataConfig(Dynamic<?> param0) {
        return (WorldDataConfiguration)WorldDataConfiguration.CODEC.parse(param0).resultOrPartial(LOGGER::error).orElse(WorldDataConfiguration.DEFAULT);
    }

    public String getName() {
        return "Anvil";
    }

    public LevelStorageSource.LevelCandidates findLevelCandidates() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir)) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
        } else {
            try {
                List<LevelStorageSource.LevelDirectory> var0 = Files.list(this.baseDir)
                    .filter(param0 -> Files.isDirectory(param0))
                    .map(LevelStorageSource.LevelDirectory::new)
                    .filter(param0 -> Files.isRegularFile(param0.dataFile()) || Files.isRegularFile(param0.oldDataFile()))
                    .toList();
                return new LevelStorageSource.LevelCandidates(var0);
            } catch (IOException var2) {
                throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
            }
        }
    }

    public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelStorageSource.LevelCandidates param0) {
        List<CompletableFuture<LevelSummary>> var0 = new ArrayList<>(param0.levels.size());

        for(LevelStorageSource.LevelDirectory var1 : param0.levels) {
            var0.add(
                CompletableFuture.supplyAsync(
                    () -> {
                        boolean var0x;
                        try {
                            var2x = DirectoryLock.isLocked(var1.path());
                        } catch (Exception var6) {
                            LOGGER.warn("Failed to read {} lock", var1.path(), var6);
                            return null;
                        }
        
                        try {
                            LevelSummary var3 = this.readLevelData(var1, this.levelSummaryReader(var1, var2x));
                            return var3 != null ? var3 : null;
                        } catch (OutOfMemoryError var4x) {
                            MemoryReserve.release();
                            System.gc();
                            LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", var1.directoryName());
                            throw var4x;
                        } catch (StackOverflowError var51) {
                            LOGGER.error(
                                LogUtils.FATAL_MARKER,
                                "Ran out of stack trying to read summary of {}. Assuming corruption; attempting to restore from from level.dat_old.",
                                var1.directoryName()
                            );
                            Util.safeReplaceOrMoveFile(var1.dataFile(), var1.oldDataFile(), var1.corruptedDataFile(LocalDateTime.now()), true);
                            throw var51;
                        }
                    },
                    Util.backgroundExecutor()
                )
            );
        }

        return Util.sequenceFailFastAndCancel(var0).thenApply(param0x -> param0x.stream().filter(Objects::nonNull).sorted().toList());
    }

    private int getStorageVersion() {
        return 19133;
    }

    @Nullable
    <T> T readLevelData(LevelStorageSource.LevelDirectory param0, BiFunction<Path, DataFixer, T> param1) {
        if (!Files.exists(param0.path())) {
            return null;
        } else {
            Path var0 = param0.dataFile();
            if (Files.exists(var0)) {
                T var1 = param1.apply(var0, this.fixerUpper);
                if (var1 != null) {
                    return var1;
                }
            }

            var0 = param0.oldDataFile();
            return Files.exists(var0) ? param1.apply(var0, this.fixerUpper) : null;
        }
    }

    @Nullable
    private static WorldDataConfiguration getDataConfiguration(Path param0, DataFixer param1) {
        try {
            Tag var0 = readLightweightData(param0);
            if (var0 instanceof CompoundTag var1) {
                CompoundTag var2 = var1.getCompound("Data");
                int var3 = var2.contains("DataVersion", 99) ? var2.getInt("DataVersion") : -1;
                Dynamic<Tag> var4 = param1.update(
                    DataFixTypes.LEVEL.getType(), new Dynamic<>(NbtOps.INSTANCE, var2), var3, SharedConstants.getCurrentVersion().getWorldVersion()
                );
                return readDataConfig(var4);
            }
        } catch (Exception var7) {
            LOGGER.error("Exception reading {}", param0, var7);
        }

        return null;
    }

    static BiFunction<Path, DataFixer, Pair<WorldData, WorldDimensions.Complete>> getLevelData(
        DynamicOps<Tag> param0, WorldDataConfiguration param1, Registry<LevelStem> param2, Lifecycle param3
    ) {
        return (param4, param5) -> {
            CompoundTag var0;
            try {
                var2x = NbtIo.readCompressed(param4.toFile());
            } catch (IOException var17) {
                throw new UncheckedIOException(var17);
            }

            CompoundTag var3x = var2x.getCompound("Data");
            CompoundTag var4 = var3x.contains("Player", 10) ? var3x.getCompound("Player") : null;
            var3x.remove("Player");
            int var5 = var3x.contains("DataVersion", 99) ? var3x.getInt("DataVersion") : -1;
            Dynamic<Tag> var6 = param5.update(
                DataFixTypes.LEVEL.getType(), new Dynamic<>(param0, var3x), var5, SharedConstants.getCurrentVersion().getWorldVersion()
            );
            WorldGenSettings var7 = (WorldGenSettings)readWorldGenSettings(var6, param5, var5)
                .getOrThrow(false, Util.prefix("WorldGenSettings: ", LOGGER::error));
            LevelVersion var8 = LevelVersion.parse(var6);
            LevelSettings var9 = LevelSettings.parse(var6, param1);
            WorldDimensions.Complete var10 = var7.dimensions().bake(param2);
            Lifecycle var11 = var10.lifecycle().add(param3);
            PrimaryLevelData var12 = PrimaryLevelData.parse(var6, param5, var5, var4, var9, var8, var10.specialWorldProperty(), var7.options(), var11);
            return Pair.of(var12, var10);
        };
    }

    BiFunction<Path, DataFixer, LevelSummary> levelSummaryReader(LevelStorageSource.LevelDirectory param0, boolean param1) {
        return (param2, param3) -> {
            try {
                Tag var0 = readLightweightData(param2);
                if (var0 instanceof CompoundTag var1x) {
                    CompoundTag var2x = var1x.getCompound("Data");
                    int var3 = var2x.contains("DataVersion", 99) ? var2x.getInt("DataVersion") : -1;
                    Dynamic<Tag> var4 = param3.update(
                        DataFixTypes.LEVEL.getType(), new Dynamic<>(NbtOps.INSTANCE, var2x), var3, SharedConstants.getCurrentVersion().getWorldVersion()
                    );
                    LevelVersion var5 = LevelVersion.parse(var4);
                    int var6 = var5.levelDataVersion();
                    if (var6 == 19132 || var6 == 19133) {
                        boolean var7 = var6 != this.getStorageVersion();
                        Path var8 = param0.iconFile();
                        WorldDataConfiguration var9 = readDataConfig(var4);
                        LevelSettings var10 = LevelSettings.parse(var4, var9);
                        FeatureFlagSet var11 = parseFeatureFlagsFromSummary(var4);
                        boolean var12 = FeatureFlags.isExperimental(var11);
                        return new LevelSummary(var10, var5, param0.directoryName(), var7, param1, var12, var8);
                    }
                } else {
                    LOGGER.warn("Invalid root tag in {}", param2);
                }

                return null;
            } catch (Exception var18) {
                LOGGER.error("Exception reading {}", param2, var18);
                return null;
            }
        };
    }

    private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<Tag> param0) {
        Set<ResourceLocation> var0 = param0.get("enabled_features")
            .asStream()
            .flatMap(param0x -> param0x.asString().result().map(ResourceLocation::tryParse).stream())
            .collect(Collectors.toSet());
        return FeatureFlags.REGISTRY.fromNames(var0, param0x -> {
        });
    }

    @Nullable
    private static Tag readLightweightData(Path param0) throws IOException {
        SkipFields var0 = new SkipFields(new FieldSelector("Data", CompoundTag.TYPE, "Player"), new FieldSelector("Data", CompoundTag.TYPE, "WorldGenSettings"));
        NbtIo.parseCompressed(param0.toFile(), var0);
        return var0.getResult();
    }

    public boolean isNewLevelIdAcceptable(String param0) {
        try {
            Path var0 = this.baseDir.resolve(param0);
            Files.createDirectory(var0);
            Files.deleteIfExists(var0);
            return true;
        } catch (IOException var3) {
            return false;
        }
    }

    public boolean levelExists(String param0) {
        return Files.isDirectory(this.baseDir.resolve(param0));
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageSource.LevelStorageAccess createAccess(String param0) throws IOException {
        return new LevelStorageSource.LevelStorageAccess(param0);
    }

    public static record LevelCandidates(List<LevelStorageSource.LevelDirectory> levels) implements Iterable<LevelStorageSource.LevelDirectory> {
        public boolean isEmpty() {
            return this.levels.isEmpty();
        }

        @Override
        public Iterator<LevelStorageSource.LevelDirectory> iterator() {
            return this.levels.iterator();
        }
    }

    public static record LevelDirectory(Path path) {
        public String directoryName() {
            return this.path.getFileName().toString();
        }

        public Path dataFile() {
            return this.resourcePath(LevelResource.LEVEL_DATA_FILE);
        }

        public Path oldDataFile() {
            return this.resourcePath(LevelResource.OLD_LEVEL_DATA_FILE);
        }

        public Path corruptedDataFile(LocalDateTime param0) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + param0.format(LevelStorageSource.FORMATTER));
        }

        public Path iconFile() {
            return this.resourcePath(LevelResource.ICON_FILE);
        }

        public Path lockFile() {
            return this.resourcePath(LevelResource.LOCK_FILE);
        }

        public Path resourcePath(LevelResource param0) {
            return this.path.resolve(param0.getId());
        }
    }

    public class LevelStorageAccess implements AutoCloseable {
        final DirectoryLock lock;
        final LevelStorageSource.LevelDirectory levelDirectory;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        public LevelStorageAccess(String param1) throws IOException {
            this.levelId = param1;
            this.levelDirectory = new LevelStorageSource.LevelDirectory(LevelStorageSource.this.baseDir.resolve(param1));
            this.lock = DirectoryLock.create(this.levelDirectory.path());
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource param0) {
            return this.resources.computeIfAbsent(param0, this.levelDirectory::resourcePath);
        }

        public Path getDimensionPath(ResourceKey<Level> param0) {
            return DimensionType.getStorageFolder(param0, this.levelDirectory.path());
        }

        private void checkLock() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public PlayerDataStorage createPlayerStorage() {
            this.checkLock();
            return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
        }

        @Nullable
        public LevelSummary getSummary() {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource.this.levelSummaryReader(this.levelDirectory, false));
        }

        @Nullable
        public Pair<WorldData, WorldDimensions.Complete> getDataTag(
            DynamicOps<Tag> param0, WorldDataConfiguration param1, Registry<LevelStem> param2, Lifecycle param3
        ) {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource.getLevelData(param0, param1, param2, param3));
        }

        @Nullable
        public WorldDataConfiguration getDataConfiguration() {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelDirectory, LevelStorageSource::getDataConfiguration);
        }

        public void saveDataTag(RegistryAccess param0, WorldData param1) {
            this.saveDataTag(param0, param1, null);
        }

        public void saveDataTag(RegistryAccess param0, WorldData param1, @Nullable CompoundTag param2) {
            File var0 = this.levelDirectory.path().toFile();
            CompoundTag var1 = param1.createTag(param0, param2);
            CompoundTag var2 = new CompoundTag();
            var2.put("Data", var1);

            try {
                File var3 = File.createTempFile("level", ".dat", var0);
                NbtIo.writeCompressed(var2, var3);
                File var4 = this.levelDirectory.oldDataFile().toFile();
                File var5 = this.levelDirectory.dataFile().toFile();
                Util.safeReplaceFile(var5, var3, var4);
            } catch (Exception var10) {
                LevelStorageSource.LOGGER.error("Failed to save level {}", var0, var10);
            }

        }

        public Optional<Path> getIconFile() {
            return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelDirectory.iconFile());
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path var0 = this.levelDirectory.lockFile();
            LevelStorageSource.LOGGER.info("Deleting level {}", this.levelId);

            for(int var1 = 1; var1 <= 5; ++var1) {
                LevelStorageSource.LOGGER.info("Attempt {}...", var1);

                try {
                    Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                        public FileVisitResult visitFile(Path param0, BasicFileAttributes param1) throws IOException {
                            if (!param0.equals(var0)) {
                                LevelStorageSource.LOGGER.debug("Deleting {}", param0);
                                Files.delete(param0);
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        public FileVisitResult postVisitDirectory(Path param0, IOException param1) throws IOException {
                            if (param1 != null) {
                                throw param1;
                            } else {
                                if (param0.equals(LevelStorageAccess.this.levelDirectory.path())) {
                                    LevelStorageAccess.this.lock.close();
                                    Files.deleteIfExists(var0);
                                }

                                Files.delete(param0);
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    });
                    break;
                } catch (IOException var6) {
                    if (var1 >= 5) {
                        throw var6;
                    }

                    LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelDirectory.path(), var6);

                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException var5) {
                    }
                }
            }

        }

        public void renameLevel(String param0) throws IOException {
            this.checkLock();
            Path var0 = this.levelDirectory.dataFile();
            if (Files.exists(var0)) {
                CompoundTag var1 = NbtIo.readCompressed(var0.toFile());
                CompoundTag var2 = var1.getCompound("Data");
                var2.putString("LevelName", param0);
                NbtIo.writeCompressed(var1, var0.toFile());
            }

        }

        public long makeWorldBackup() throws IOException {
            this.checkLock();
            String var0 = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
            Path var1 = LevelStorageSource.this.getBackupPath();

            try {
                FileUtil.createDirectoriesSafe(var1);
            } catch (IOException var9) {
                throw new RuntimeException(var9);
            }

            Path var3 = var1.resolve(FileUtil.findAvailableName(var1, var0, ".zip"));

            try (final ZipOutputStream var4 = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(var3)))) {
                final Path var5 = Paths.get(this.levelId);
                Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path param0, BasicFileAttributes param1) throws IOException {
                        if (param0.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            String var0 = var5.resolve(LevelStorageAccess.this.levelDirectory.path().relativize(param0)).toString().replace('\\', '/');
                            ZipEntry var1 = new ZipEntry(var0);
                            var4.putNextEntry(var1);
                            com.google.common.io.Files.asByteSource(param0.toFile()).copyTo(var4);
                            var4.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
            }

            return Files.size(var3);
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }
    }
}
