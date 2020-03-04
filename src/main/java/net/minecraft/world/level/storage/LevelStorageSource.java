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
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixTypes;
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
                    LevelData var4 = this.getDataTagFor(var3);
                    if (var4 != null && (var4.getVersion() == 19132 || var4.getVersion() == 19133)) {
                        boolean var5 = var4.getVersion() != this.getStorageVersion();
                        String var6 = var4.getLevelName();
                        if (StringUtils.isEmpty(var6)) {
                            var6 = var3;
                        }

                        long var7 = 0L;
                        var0.add(new LevelSummary(var4, var3, var6, 0L, var5));
                    }
                }
            }

            return var0;
        }
    }

    private int getStorageVersion() {
        return 19133;
    }

    public LevelStorage selectLevel(String param0, @Nullable MinecraftServer param1) {
        return selectLevel(this.baseDir, this.fixerUpper, param0, param1);
    }

    protected static LevelStorage selectLevel(Path param0, DataFixer param1, String param2, @Nullable MinecraftServer param3) {
        return new LevelStorage(param0.toFile(), param2, param3, param1);
    }

    public boolean requiresConversion(String param0) {
        LevelData var0 = this.getDataTagFor(param0);
        return var0 != null && var0.getVersion() != this.getStorageVersion();
    }

    public boolean convertLevel(String param0, ProgressListener param1) {
        return McRegionUpgrader.convertLevel(this.baseDir, this.fixerUpper, param0, param1);
    }

    @Nullable
    public LevelData getDataTagFor(String param0) {
        return getDataTagFor(this.baseDir, this.fixerUpper, param0);
    }

    @Nullable
    protected static LevelData getDataTagFor(Path param0, DataFixer param1, String param2) {
        File var0 = new File(param0.toFile(), param2);
        if (!var0.exists()) {
            return null;
        } else {
            File var1 = new File(var0, "level.dat");
            if (var1.exists()) {
                LevelData var2 = getLevelData(var1, param1);
                if (var2 != null) {
                    return var2;
                }
            }

            var1 = new File(var0, "level.dat_old");
            return var1.exists() ? getLevelData(var1, param1) : null;
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
    public void renameLevel(String param0, String param1) {
        File var0 = new File(this.baseDir.toFile(), param0);
        if (var0.exists()) {
            File var1 = new File(var0, "level.dat");
            if (var1.exists()) {
                try {
                    CompoundTag var2 = NbtIo.readCompressed(new FileInputStream(var1));
                    CompoundTag var3 = var2.getCompound("Data");
                    var3.putString("LevelName", param1);
                    NbtIo.writeCompressed(var2, new FileOutputStream(var1));
                } catch (Exception var7) {
                    var7.printStackTrace();
                }
            }

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
    public boolean deleteLevel(String param0) {
        File var0 = new File(this.baseDir.toFile(), param0);
        if (!var0.exists()) {
            return true;
        } else {
            LOGGER.info("Deleting level {}", param0);

            for(int var1 = 1; var1 <= 5; ++var1) {
                LOGGER.info("Attempt {}...", var1);
                if (deleteRecursive(var0.listFiles())) {
                    break;
                }

                LOGGER.warn("Unsuccessful in deleting contents.");
                if (var1 < 5) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException var5) {
                    }
                }
            }

            return var0.delete();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean deleteRecursive(File[] param0) {
        for(File var0 : param0) {
            LOGGER.debug("Deleting {}", var0);
            if (var0.isDirectory() && !deleteRecursive(var0.listFiles())) {
                LOGGER.warn("Couldn't delete directory {}", var0);
                return false;
            }

            if (!var0.delete()) {
                LOGGER.warn("Couldn't delete file {}", var0);
                return false;
            }
        }

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean levelExists(String param0) {
        return Files.isDirectory(this.baseDir.resolve(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public Path getBaseDir() {
        return this.baseDir;
    }

    public File getFile(String param0, String param1) {
        return this.baseDir.resolve(param0).resolve(param1).toFile();
    }

    @OnlyIn(Dist.CLIENT)
    private Path getLevelPath(String param0) {
        return this.baseDir.resolve(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public Path getBackupPath() {
        return this.backupDir;
    }

    @OnlyIn(Dist.CLIENT)
    public long makeWorldBackup(String param0) throws IOException {
        final Path var0 = this.getLevelPath(param0);
        String var1 = LocalDateTime.now().format(FORMATTER) + "_" + param0;
        Path var2 = this.getBackupPath();

        try {
            Files.createDirectories(Files.exists(var2) ? var2.toRealPath() : var2);
        } catch (IOException var18) {
            throw new RuntimeException(var18);
        }

        Path var4 = var2.resolve(FileUtil.findAvailableName(var2, var1, ".zip"));

        try (final ZipOutputStream var5 = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(var4)))) {
            final Path var6 = Paths.get(param0);
            Files.walkFileTree(var0, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path param0, BasicFileAttributes param1) throws IOException {
                    String var0 = var6.resolve(var0.relativize(param0)).toString().replace('\\', '/');
                    ZipEntry var1 = new ZipEntry(var0);
                    var5.putNextEntry(var1);
                    com.google.common.io.Files.asByteSource(param0.toFile()).copyTo(var5);
                    var5.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return Files.size(var4);
    }
}
