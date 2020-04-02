package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelStorageSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
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
    private final Path baseDir;
    private final Path backupDir;
    private final DataFixer fixerUpper;

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

    @OnlyIn(Dist.CLIENT)
    public List<LevelSummary> getLevelList() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir)) {
            throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
        } else {
            List<LevelSummary> var0 = Lists.newArrayList();
            File[] var1 = this.baseDir.toFile().listFiles();

            for(File var2 : var1) {
                if (var2.isDirectory()) {
                    String var3 = var2.getName();

                    boolean var4;
                    try {
                        var4 = DirectoryLock.isLocked(var2.toPath());
                    } catch (Exception var15) {
                        LOGGER.warn("Failed to read {} lock", var2, var15);
                        continue;
                    }

                    LevelData var7 = this.getLevelData(var2);
                    if (var7 != null && (var7.getVersion() == 19132 || var7.getVersion() == 19133)) {
                        boolean var8 = var7.getVersion() != this.getStorageVersion();
                        String var9 = var7.getLevelName();
                        if (StringUtils.isEmpty(var9)) {
                            var9 = var3;
                        }

                        long var10 = 0L;
                        File var11 = new File(var2, "icon.png");
                        var0.add(new LevelSummary(var7, var3, var9, 0L, var8, var4, var11));
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
    private LevelData getLevelData(File param0) {
        if (!param0.exists()) {
            return null;
        } else {
            File var0 = new File(param0, "level.dat");
            if (var0.exists()) {
                LevelData var1 = getLevelData(var0, this.fixerUpper);
                if (var1 != null) {
                    return var1;
                }
            }

            var0 = new File(param0, "level.dat_old");
            return var0.exists() ? getLevelData(var0, this.fixerUpper) : null;
        }
    }

    @Nullable
    public static LevelData getLevelData(File param0, DataFixer param1) {
        try {
            CompoundTag var0 = NbtIo.readCompressed(new FileInputStream(param0));
            CompoundTag var1 = var0.getCompound("Data");
            CompoundTag var2 = var1.contains("Player", 10) ? var1.getCompound("Player") : null;
            var1.remove("Player");
            int var3 = var1.contains("DataVersion", 99) ? var1.getInt("DataVersion") : -1;
            return new LevelData(NbtUtils.update(param1, DataFixTypes.LEVEL, var1, var3), param1, var3, var2);
        } catch (Exception var6) {
            LOGGER.error("Exception reading {}", param0, var6);
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
    public boolean levelExists(String param0) {
        return Files.isDirectory(this.baseDir.resolve(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public Path getBaseDir() {
        return this.baseDir;
    }

    @OnlyIn(Dist.CLIENT)
    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageSource.LevelStorageAccess createAccess(String param0) throws IOException {
        return new LevelStorageSource.LevelStorageAccess(param0);
    }

    public class LevelStorageAccess implements AutoCloseable {
        private final DirectoryLock lock;
        private final Path levelPath;
        private final String levelId;

        public LevelStorageAccess(String param1) throws IOException {
            this.levelId = param1;
            this.levelPath = LevelStorageSource.this.baseDir.resolve(param1);
            this.lock = DirectoryLock.create(this.levelPath);
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath() {
            return this.levelPath;
        }

        private void checkLock() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public LevelStorage selectLevel(@Nullable MinecraftServer param0) {
            this.checkLock();
            return new LevelStorage(LevelStorageSource.this.baseDir.toFile(), this.levelId, param0, LevelStorageSource.this.fixerUpper);
        }

        public boolean requiresConversion() {
            LevelData var0 = this.getDataTag();
            return var0 != null && var0.getVersion() != LevelStorageSource.this.getStorageVersion();
        }

        public boolean convertLevel(ProgressListener param0) {
            this.checkLock();
            return McRegionUpgrader.convertLevel(this, param0);
        }

        @Nullable
        public LevelData getDataTag() {
            this.checkLock();
            return LevelStorageSource.this.getLevelData(this.levelPath.toFile());
        }

        public File getIconFile() {
            this.checkLock();
            return this.levelPath.resolve("icon.png").toFile();
        }

        @OnlyIn(Dist.CLIENT)
        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path var0 = this.levelPath.resolve("session.lock");

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

        @OnlyIn(Dist.CLIENT)
        public void renameLevel(String param0) throws IOException {
            this.checkLock();
            File var0 = new File(LevelStorageSource.this.baseDir.toFile(), this.levelId);
            if (var0.exists()) {
                File var1 = new File(var0, "level.dat");
                if (var1.exists()) {
                    CompoundTag var2 = NbtIo.readCompressed(new FileInputStream(var1));
                    CompoundTag var3 = var2.getCompound("Data");
                    var3.putString("LevelName", param0);
                    NbtIo.writeCompressed(var2, new FileOutputStream(var1));
                }

            }
        }

        @OnlyIn(Dist.CLIENT)
        public long makeWorldBackup() throws IOException {
            this.checkLock();
            final Path var0 = this.getLevelPath();
            String var1 = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
            Path var2 = LevelStorageSource.this.getBackupPath();

            try {
                Files.createDirectories(Files.exists(var2) ? var2.toRealPath() : var2);
            } catch (IOException var17) {
                throw new RuntimeException(var17);
            }

            Path var4 = var2.resolve(FileUtil.findAvailableName(var2, var1, ".zip"));

            try (final ZipOutputStream var5 = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(var4)))) {
                final Path var6 = Paths.get(this.levelId);
                Files.walkFileTree(var0, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path param0, BasicFileAttributes param1) throws IOException {
                        if (param0.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            String var0 = var6.resolve(var0.relativize(param0)).toString().replace('\\', '/');
                            ZipEntry var1 = new ZipEntry(var0);
                            var5.putNextEntry(var1);
                            com.google.common.io.Files.asByteSource(param0.toFile()).copyTo(var5);
                            var5.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
            }

            return Files.size(var4);
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }
    }
}
