package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
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
    private static final String ICON_FILENAME = "icon.png";
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
            Files.createDirectories(Files.exists(param0) ? param0.toRealPath() : param0);
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        this.baseDir = param0;
        this.backupDir = param1;
    }

    public static LevelStorageSource createDefault(Path param0) {
        return new LevelStorageSource(param0, param0.resolve("../backups"), DataFixers.getDataFixer());
    }

    private static <T> Pair<WorldGenSettings, Lifecycle> readWorldGenSettings(Dynamic<T> param0, DataFixer param1, int param2) {
        Dynamic<T> var0 = param0.get("WorldGenSettings").orElseEmptyMap();

        for(String var1 : OLD_SETTINGS_KEYS) {
            Optional<? extends Dynamic<?>> var2 = param0.get(var1).result();
            if (var2.isPresent()) {
                var0 = var0.set(var1, var2.get());
            }
        }

        Dynamic<T> var3 = param1.update(References.WORLD_GEN_SETTINGS, var0, param2, SharedConstants.getCurrentVersion().getWorldVersion());
        DataResult<WorldGenSettings> var4 = WorldGenSettings.CODEC.parse(var3);
        return Pair.of(var4.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).orElseGet(() -> {
            RegistryAccess var0x = RegistryAccess.RegistryHolder.readFromDisk(var3);
            return WorldGenSettings.makeDefault(var0x);
        }), var4.lifecycle());
    }

    private static DataPackConfig readDataPackConfig(Dynamic<?> param0) {
        return DataPackConfig.CODEC.parse(param0).resultOrPartial(LOGGER::error).orElse(DataPackConfig.DEFAULT);
    }

    public String getName() {
        return "Anvil";
    }

    public List<LevelSummary> getLevelList() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir)) {
            throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
        } else {
            List<LevelSummary> var0 = Lists.newArrayList();
            File[] var1 = this.baseDir.toFile().listFiles();

            for(File var2 : var1) {
                if (var2.isDirectory()) {
                    boolean var3;
                    try {
                        var3 = DirectoryLock.isLocked(var2.toPath());
                    } catch (Exception var10) {
                        LOGGER.warn("Failed to read {} lock", var2, var10);
                        continue;
                    }

                    try {
                        LevelSummary var6 = this.readLevelData(var2, this.levelSummaryReader(var2, var3));
                        if (var6 != null) {
                            var0.add(var6);
                        }
                    } catch (OutOfMemoryError var9) {
                        MemoryReserve.release();
                        System.gc();
                        LOGGER.error(LogUtils.FATAL_MARKER, "Ran out of memory trying to read summary of {}", var2);
                        throw var9;
                    }
                }
            }

            return var0;
        }
    }

    private int getStorageVersion() {
        return 19133;
    }

    @Nullable
    <T> T readLevelData(File param0, BiFunction<File, DataFixer, T> param1) {
        if (!param0.exists()) {
            return null;
        } else {
            File var0 = new File(param0, "level.dat");
            if (var0.exists()) {
                T var1 = param1.apply(var0, this.fixerUpper);
                if (var1 != null) {
                    return var1;
                }
            }

            var0 = new File(param0, "level.dat_old");
            return var0.exists() ? param1.apply(var0, this.fixerUpper) : null;
        }
    }

    @Nullable
    private static DataPackConfig getDataPacks(File param0, DataFixer param1) {
        try {
            Tag var0 = readLightweightData(param0);
            if (var0 instanceof CompoundTag var1) {
                CompoundTag var2 = var1.getCompound("Data");
                int var3 = var2.contains("DataVersion", 99) ? var2.getInt("DataVersion") : -1;
                Dynamic<Tag> var4 = param1.update(
                    DataFixTypes.LEVEL.getType(), new Dynamic<>(NbtOps.INSTANCE, var2), var3, SharedConstants.getCurrentVersion().getWorldVersion()
                );
                return var4.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT);
            }
        } catch (Exception var7) {
            LOGGER.error("Exception reading {}", param0, var7);
        }

        return null;
    }

    static BiFunction<File, DataFixer, PrimaryLevelData> getLevelData(DynamicOps<Tag> param0, DataPackConfig param1) {
        return (param2, param3) -> {
            try {
                CompoundTag var0 = NbtIo.readCompressed(param2);
                CompoundTag var1x = var0.getCompound("Data");
                CompoundTag var2 = var1x.contains("Player", 10) ? var1x.getCompound("Player") : null;
                var1x.remove("Player");
                int var3 = var1x.contains("DataVersion", 99) ? var1x.getInt("DataVersion") : -1;
                Dynamic<Tag> var4 = param3.update(
                    DataFixTypes.LEVEL.getType(), new Dynamic<>(param0, var1x), var3, SharedConstants.getCurrentVersion().getWorldVersion()
                );
                Pair<WorldGenSettings, Lifecycle> var5 = readWorldGenSettings(var4, param3, var3);
                LevelVersion var6 = LevelVersion.parse(var4);
                LevelSettings var7 = LevelSettings.parse(var4, param1);
                return PrimaryLevelData.parse(var4, param3, var3, var2, var7, var6, var5.getFirst(), var5.getSecond());
            } catch (Exception var12) {
                LOGGER.error("Exception reading {}", param2, var12);
                return null;
            }
        };
    }

    BiFunction<File, DataFixer, LevelSummary> levelSummaryReader(File param0, boolean param1) {
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
                        File var8 = new File(param0, "icon.png");
                        DataPackConfig var9 = var4.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT);
                        LevelSettings var10 = LevelSettings.parse(var4, var9);
                        return new LevelSummary(var10, var5, param0.getName(), var7, param1, var8);
                    }
                } else {
                    LOGGER.warn("Invalid root tag in {}", param2);
                }

                return null;
            } catch (Exception var16) {
                LOGGER.error("Exception reading {}", param2, var16);
                return null;
            }
        };
    }

    @Nullable
    private static Tag readLightweightData(File param0) throws IOException {
        SkipFields var0 = new SkipFields(new FieldSelector("Data", CompoundTag.TYPE, "Player"), new FieldSelector("Data", CompoundTag.TYPE, "WorldGenSettings"));
        NbtIo.parseCompressed(param0, var0);
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

    public class LevelStorageAccess implements AutoCloseable {
        final DirectoryLock lock;
        final Path levelPath;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        public LevelStorageAccess(String param1) throws IOException {
            this.levelId = param1;
            this.levelPath = LevelStorageSource.this.baseDir.resolve(param1);
            this.lock = DirectoryLock.create(this.levelPath);
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource param0) {
            return this.resources.computeIfAbsent(param0, param0x -> this.levelPath.resolve(param0x.getId()));
        }

        public Path getDimensionPath(ResourceKey<Level> param0) {
            return DimensionType.getStorageFolder(param0, this.levelPath);
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
            return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.this.levelSummaryReader(this.levelPath.toFile(), false));
        }

        @Nullable
        public WorldData getDataTag(DynamicOps<Tag> param0, DataPackConfig param1) {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.getLevelData(param0, param1));
        }

        @Nullable
        public DataPackConfig getDataPacks() {
            this.checkLock();
            return LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource::getDataPacks);
        }

        public void saveDataTag(RegistryAccess param0, WorldData param1) {
            this.saveDataTag(param0, param1, null);
        }

        public void saveDataTag(RegistryAccess param0, WorldData param1, @Nullable CompoundTag param2) {
            File var0 = this.levelPath.toFile();
            CompoundTag var1 = param1.createTag(param0, param2);
            CompoundTag var2 = new CompoundTag();
            var2.put("Data", var1);

            try {
                File var3 = File.createTempFile("level", ".dat", var0);
                NbtIo.writeCompressed(var2, var3);
                File var4 = new File(var0, "level.dat_old");
                File var5 = new File(var0, "level.dat");
                Util.safeReplaceFile(var5, var3, var4);
            } catch (Exception var10) {
                LevelStorageSource.LOGGER.error("Failed to save level {}", var0, var10);
            }

        }

        public Optional<Path> getIconFile() {
            return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelPath.resolve("icon.png"));
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path var0 = this.levelPath.resolve("session.lock");
            LevelStorageSource.LOGGER.info("Deleting level {}", this.levelId);

            for(int var1 = 1; var1 <= 5; ++var1) {
                LevelStorageSource.LOGGER.info("Attempt {}...", var1);

                try {
                    Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() {
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
                                if (param0.equals(LevelStorageAccess.this.levelPath)) {
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

                    LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelPath, var6);

                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException var5) {
                    }
                }
            }

        }

        public void renameLevel(String param0) throws IOException {
            this.checkLock();
            File var0 = new File(LevelStorageSource.this.baseDir.toFile(), this.levelId);
            if (var0.exists()) {
                File var1 = new File(var0, "level.dat");
                if (var1.exists()) {
                    CompoundTag var2 = NbtIo.readCompressed(var1);
                    CompoundTag var3 = var2.getCompound("Data");
                    var3.putString("LevelName", param0);
                    NbtIo.writeCompressed(var2, var1);
                }

            }
        }

        public long makeWorldBackup() throws IOException {
            this.checkLock();
            String var0 = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
            Path var1 = LevelStorageSource.this.getBackupPath();

            try {
                Files.createDirectories(Files.exists(var1) ? var1.toRealPath() : var1);
            } catch (IOException var9) {
                throw new RuntimeException(var9);
            }

            Path var3 = var1.resolve(FileUtil.findAvailableName(var1, var0, ".zip"));

            try (final ZipOutputStream var4 = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(var3)))) {
                final Path var5 = Paths.get(this.levelId);
                Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path param0, BasicFileAttributes param1) throws IOException {
                        if (param0.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            String var0 = var5.resolve(LevelStorageAccess.this.levelPath.relativize(param0)).toString().replace('\\', '/');
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
