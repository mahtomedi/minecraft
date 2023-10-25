package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraft.world.level.validation.PathAllowList;
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
    private static final String TAG_DATA = "Data";
    private static final PathMatcher NO_SYMLINKS_ALLOWED = param0 -> false;
    public static final String ALLOWED_SYMLINKS_CONFIG_NAME = "allowed_symlinks.txt";
    private static final int UNCOMPRESSED_NBT_QUOTA = 104857600;
    private final Path baseDir;
    private final Path backupDir;
    final DataFixer fixerUpper;
    private final DirectoryValidator worldDirValidator;
    boolean crashedWhileSaving;

    public LevelStorageSource(Path param0, Path param1, DirectoryValidator param2, DataFixer param3) {
        this.fixerUpper = param3;

        try {
            FileUtil.createDirectoriesSafe(param0);
        } catch (IOException var6) {
            throw new UncheckedIOException(var6);
        }

        this.baseDir = param0;
        this.backupDir = param1;
        this.worldDirValidator = param2;
    }

    public static DirectoryValidator parseValidator(Path param0) {
        if (Files.exists(param0)) {
            try {
                DirectoryValidator var2;
                try (BufferedReader var0 = Files.newBufferedReader(param0)) {
                    var2 = new DirectoryValidator(PathAllowList.readPlain(var0));
                }

                return var2;
            } catch (Exception var6) {
                LOGGER.error("Failed to parse {}, disallowing all symbolic links", "allowed_symlinks.txt", var6);
            }
        }

        return new DirectoryValidator(NO_SYMLINKS_ALLOWED);
    }

    public static LevelStorageSource createDefault(Path param0) {
        DirectoryValidator var0 = parseValidator(param0.resolve("allowed_symlinks.txt"));
        return new LevelStorageSource(param0, param0.resolve("../backups"), var0, DataFixers.getDataFixer());
    }

    public static WorldDataConfiguration readDataConfig(Dynamic<?> param0) {
        return WorldDataConfiguration.CODEC.parse(param0).resultOrPartial(LOGGER::error).orElse(WorldDataConfiguration.DEFAULT);
    }

    public static WorldLoader.PackConfig getPackConfig(Dynamic<?> param0, PackRepository param1, boolean param2) {
        return new WorldLoader.PackConfig(param1, readDataConfig(param0), param2, false);
    }

    public static LevelDataAndDimensions getLevelDataAndDimensions(
        Dynamic<?> param0, WorldDataConfiguration param1, Registry<LevelStem> param2, RegistryAccess.Frozen param3
    ) {
        Dynamic<?> var0 = wrapWithRegistryOps(param0, param3);
        Dynamic<?> var1 = var0.get("WorldGenSettings").orElseEmptyMap();
        WorldGenSettings var2 = WorldGenSettings.CODEC.parse(var1).getOrThrow(false, Util.prefix("WorldGenSettings: ", LOGGER::error));
        LevelSettings var3 = LevelSettings.parse(var0, param1);
        WorldDimensions.Complete var4 = var2.dimensions().bake(param2);
        Lifecycle var5 = var4.lifecycle().add(param3.allRegistriesLifecycle());
        PrimaryLevelData var6 = PrimaryLevelData.parse(var0, var3, var4.specialWorldProperty(), var2.options(), var5);
        return new LevelDataAndDimensions(var6, var4);
    }

    private static <T> Dynamic<T> wrapWithRegistryOps(Dynamic<T> param0, RegistryAccess.Frozen param1) {
        RegistryOps<T> var0 = RegistryOps.create(param0.getOps(), param1);
        return new Dynamic<>(var0, param0.getValue());
    }

    public String getName() {
        return "Anvil";
    }

    public LevelStorageSource.LevelCandidates findLevelCandidates() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir)) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
        } else {
            try {
                LevelStorageSource.LevelCandidates var3;
                try (Stream<Path> var0 = Files.list(this.baseDir)) {
                    List<LevelStorageSource.LevelDirectory> var1 = var0.filter(param0 -> Files.isDirectory(param0))
                        .map(LevelStorageSource.LevelDirectory::new)
                        .filter(param0 -> Files.isRegularFile(param0.dataFile()) || Files.isRegularFile(param0.oldDataFile()))
                        .toList();
                    var3 = new LevelStorageSource.LevelCandidates(var1);
                }

                return var3;
            } catch (IOException var6) {
                throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
            }
        }
    }

    public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelStorageSource.LevelCandidates param0) {
        List<CompletableFuture<LevelSummary>> var0 = new ArrayList<>(param0.levels.size());

        for(LevelStorageSource.LevelDirectory var1 : param0.levels) {
            var0.add(CompletableFuture.supplyAsync(() -> {
                boolean var0x;
                try {
                    var2x = DirectoryLock.isLocked(var1.path());
                } catch (Exception var13) {
                    LOGGER.warn("Failed to read {} lock", var1.path(), var13);
                    return null;
                }

                try {
                    return this.readLevelSummary(var1, var2x);
                } catch (OutOfMemoryError var12) {
                    MemoryReserve.release();
                    System.gc();
                    String var4x = "Ran out of memory trying to read summary of world folder \"" + var1.directoryName() + "\"";
                    LOGGER.error(LogUtils.FATAL_MARKER, var4x);
                    OutOfMemoryError var5 = new OutOfMemoryError("Ran out of memory reading level data");
                    var5.initCause(var12);
                    CrashReport var6 = CrashReport.forThrowable(var5, var4x);
                    CrashReportCategory var7 = var6.addCategory("World details");
                    var7.setDetail("Folder Name", var1.directoryName());

                    try {
                        long var8 = Files.size(var1.dataFile());
                        var7.setDetail("level.dat size", var8);
                    } catch (IOException var11) {
                        var7.setDetailError("level.dat size", var11);
                    }

                    throw new ReportedException(var6);
                }
            }, Util.backgroundExecutor()));
        }

        return Util.sequenceFailFastAndCancel(var0).thenApply(param0x -> param0x.stream().filter(Objects::nonNull).sorted().toList());
    }

    private int getStorageVersion() {
        return 19133;
    }

    static CompoundTag readLevelDataTagRaw(Path param0) throws IOException {
        return NbtIo.readCompressed(param0.toFile(), NbtAccounter.create(104857600L));
    }

    static Dynamic<?> readLevelDataTagFixed(Path param0, DataFixer param1) throws IOException {
        CompoundTag var0 = readLevelDataTagRaw(param0);
        CompoundTag var1 = var0.getCompound("Data");
        int var2 = NbtUtils.getDataVersion(var1, -1);
        Dynamic<?> var3 = DataFixTypes.LEVEL.updateToCurrentVersion(param1, new Dynamic<>(NbtOps.INSTANCE, var1), var2);
        Dynamic<?> var4 = var3.get("Player").orElseEmptyMap();
        Dynamic<?> var5 = DataFixTypes.PLAYER.updateToCurrentVersion(param1, var4, var2);
        var3 = var3.set("Player", var5);
        Dynamic<?> var6 = var3.get("WorldGenSettings").orElseEmptyMap();
        Dynamic<?> var7 = DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion(param1, var6, var2);
        return var3.set("WorldGenSettings", var7);
    }

    private LevelSummary readLevelSummary(LevelStorageSource.LevelDirectory param0, boolean param1) {
        Path var0 = param0.dataFile();
        if (Files.exists(var0)) {
            try {
                if (Files.isSymbolicLink(var0)) {
                    List<ForbiddenSymlinkInfo> var1 = this.worldDirValidator.validateSymlink(var0);
                    if (!var1.isEmpty()) {
                        LOGGER.warn("{}", ContentValidationException.getMessage(var0, var1));
                        return new LevelSummary.SymlinkLevelSummary(param0.directoryName(), param0.iconFile());
                    }
                }

                Tag var2 = readLightweightData(var0);
                if (var2 instanceof CompoundTag var3) {
                    CompoundTag var4 = var3.getCompound("Data");
                    int var5 = NbtUtils.getDataVersion(var4, -1);
                    Dynamic<?> var6 = DataFixTypes.LEVEL.updateToCurrentVersion(this.fixerUpper, new Dynamic<>(NbtOps.INSTANCE, var4), var5);
                    return this.makeLevelSummary(var6, param0, param1);
                }

                LOGGER.warn("Invalid root tag in {}", var0);
            } catch (Exception var9) {
                LOGGER.error("Exception reading {}", var0, var9);
            }
        }

        return new LevelSummary.CorruptedLevelSummary(param0.directoryName(), param0.iconFile(), getFileModificationTime(param0));
    }

    private static long getFileModificationTime(LevelStorageSource.LevelDirectory param0) {
        Instant var0 = getFileModificationTime(param0.dataFile());
        if (var0 == null) {
            var0 = getFileModificationTime(param0.oldDataFile());
        }

        return var0 == null ? -1L : var0.toEpochMilli();
    }

    @Nullable
    static Instant getFileModificationTime(Path param0) {
        try {
            return Files.getLastModifiedTime(param0).toInstant();
        } catch (IOException var2) {
            return null;
        }
    }

    LevelSummary makeLevelSummary(Dynamic<?> param0, LevelStorageSource.LevelDirectory param1, boolean param2) {
        LevelVersion var0 = LevelVersion.parse(param0);
        int var1 = var0.levelDataVersion();
        if (var1 != 19132 && var1 != 19133) {
            throw new NbtFormatException("Unknown data version: " + Integer.toHexString(var1));
        } else {
            boolean var2 = var1 != this.getStorageVersion();
            Path var3 = param1.iconFile();
            WorldDataConfiguration var4 = readDataConfig(param0);
            LevelSettings var5 = LevelSettings.parse(param0, var4);
            FeatureFlagSet var6 = parseFeatureFlagsFromSummary(param0);
            boolean var7 = FeatureFlags.isExperimental(var6);
            return new LevelSummary(var5, var0, param1.directoryName(), var2, param2, var7, var3);
        }
    }

    private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> param0) {
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
        NbtIo.parseCompressed(param0.toFile(), var0, NbtAccounter.create(104857600L));
        return var0.getResult();
    }

    public boolean isNewLevelIdAcceptable(String param0) {
        try {
            Path var0 = this.getLevelPath(param0);
            Files.createDirectory(var0);
            Files.deleteIfExists(var0);
            return true;
        } catch (IOException var3) {
            return false;
        }
    }

    public boolean levelExists(String param0) {
        try {
            return Files.isDirectory(this.getLevelPath(param0));
        } catch (InvalidPathException var3) {
            return false;
        }
    }

    public Path getLevelPath(String param0) {
        return this.baseDir.resolve(param0);
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String param0) throws IOException, ContentValidationException {
        Path var0 = this.getLevelPath(param0);
        List<ForbiddenSymlinkInfo> var1 = this.worldDirValidator.validateDirectory(var0, true);
        if (!var1.isEmpty()) {
            throw new ContentValidationException(var0, var1);
        } else {
            return new LevelStorageSource.LevelStorageAccess(param0, var0);
        }
    }

    public LevelStorageSource.LevelStorageAccess createAccess(String param0) throws IOException {
        Path var0 = this.getLevelPath(param0);
        return new LevelStorageSource.LevelStorageAccess(param0, var0);
    }

    public DirectoryValidator getWorldDirValidator() {
        return this.worldDirValidator;
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

        public Path rawDataFile(LocalDateTime param0) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_raw_" + param0.format(LevelStorageSource.FORMATTER));
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

        LevelStorageAccess(String param1, Path param2) throws IOException {
            this.levelId = param1;
            this.levelDirectory = new LevelStorageSource.LevelDirectory(param2);
            this.lock = DirectoryLock.create(param2);
        }

        public void safeClose() {
            try {
                this.close();
            } catch (IOException var2) {
                LevelStorageSource.LOGGER.warn("Failed to unlock access to level {}", this.getLevelId(), var2);
            }

        }

        public LevelStorageSource parent() {
            return LevelStorageSource.this;
        }

        public LevelStorageSource.LevelDirectory getLevelDirectory() {
            return this.levelDirectory;
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

        public LevelSummary getSummary(Dynamic<?> param0) {
            this.checkLock();
            return LevelStorageSource.this.makeLevelSummary(param0, this.levelDirectory, false);
        }

        public Dynamic<?> getDataTag() throws IOException {
            return this.getDataTag(false);
        }

        public Dynamic<?> getDataTagFallback() throws IOException {
            return this.getDataTag(true);
        }

        private Dynamic<?> getDataTag(boolean param0) throws IOException {
            this.checkLock();
            return LevelStorageSource.readLevelDataTagFixed(
                param0 ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile(), LevelStorageSource.this.fixerUpper
            );
        }

        public void saveDataTag(RegistryAccess param0, WorldData param1) {
            this.saveDataTag(param0, param1, null);
        }

        public void saveDataTag(RegistryAccess param0, WorldData param1, @Nullable CompoundTag param2) {
            CompoundTag var0 = param1.createTag(param0, param2);
            CompoundTag var1 = new CompoundTag();
            var1.put("Data", var0);
            this.saveLevelData(var1);
        }

        private void saveLevelData(CompoundTag param0) {
            File var0 = this.levelDirectory.path().toFile();
            Exception var1 = null;

            try {
                File var2 = File.createTempFile("level", ".dat", var0);
                NbtIo.writeCompressed(param0, var2);
                File var3 = this.levelDirectory.oldDataFile().toFile();
                File var4 = this.levelDirectory.dataFile().toFile();
                Util.safeReplaceFile(var4, var2, var3);
            } catch (Exception var101) {
                LevelStorageSource.LOGGER.error("Failed to save level {}", var0, var101);
                var1 = var101;
            }

            Path var6 = this.levelDirectory.dataFile();
            if (Files.exists(var6)) {
                File var7 = var6.toFile();

                try {
                    NbtIo.readCompressed(var7, NbtAccounter.create(104857600L));
                } catch (Exception var11) {
                    if (LevelStorageSource.this.crashedWhileSaving) {
                        LevelStorageSource.LOGGER.error("Failed to save level {}. Skipping further handling, reported errors earlier already.", var0, var11);
                    } else {
                        LevelStorageSource.this.crashedWhileSaving = true;
                        CrashReport var9 = new CrashReport("Won the zlib-lottery?", new IllegalStateException("Failed to read back written world data", var1));
                        CrashReportCategory var10 = var9.addCategory("level.dat");
                        var10.setDetail("World folder", this.levelDirectory.directoryName());
                        var10.setDetail("Reading Exception", ((Throwable)(var11 instanceof ReportedException var11 ? var11.getCause() : var11)).toString());
                        var10.setDetail("Uncompressed", () -> Base64.getEncoder().encodeToString(NbtIo.writeToByteArray(param0)));
                        var10.setDetail("Compressed saved", () -> Base64.getEncoder().encodeToString(Files.readAllBytes(var7.toPath())));
                        var10.setDetail("Compressed array", () -> Base64.getEncoder().encodeToString(NbtIo.writeToByteArrayCompressed(param0)));
                        LocalDateTime var12 = LocalDateTime.now();
                        var10.setDetail("Corrupted file", () -> {
                            Path var0x = this.levelDirectory.corruptedDataFile(var12);
                            Files.move(var7.toPath(), var0x);
                            return var0x.getFileName().toString();
                        });
                        var10.setDetail("Raw file", () -> {
                            Path var0x = this.levelDirectory.rawDataFile(var12);
                            Files.write(var0x, NbtIo.writeToByteArray(param0));
                            return var0x.getFileName().toString();
                        });
                        throw new ReportedException(var9);
                    }
                }
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

                        public FileVisitResult postVisitDirectory(Path param0, @Nullable IOException param1) throws IOException {
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
            this.modifyLevelDataWithoutDatafix(param1 -> param1.putString("LevelName", param0.trim()));
        }

        public void renameAndDropPlayer(String param0) throws IOException {
            this.modifyLevelDataWithoutDatafix(param1 -> {
                param1.putString("LevelName", param0.trim());
                param1.remove("Player");
            });
        }

        private void modifyLevelDataWithoutDatafix(Consumer<CompoundTag> param0) throws IOException {
            this.checkLock();
            CompoundTag var0 = LevelStorageSource.readLevelDataTagRaw(this.levelDirectory.dataFile());
            param0.accept(var0.getCompound("Data"));
            this.saveLevelData(var0);
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

        public boolean hasWorldData() {
            return Files.exists(this.levelDirectory.dataFile()) || Files.exists(this.levelDirectory.oldDataFile());
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }

        public boolean restoreLevelDataFromOld() {
            return Util.safeReplaceOrMoveFile(
                this.levelDirectory.dataFile(), this.levelDirectory.oldDataFile(), this.levelDirectory.corruptedDataFile(LocalDateTime.now()), true
            );
        }

        @Nullable
        public Instant getFileModificationTime(boolean param0) {
            return LevelStorageSource.getFileModificationTime(param0 ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile());
        }
    }
}
