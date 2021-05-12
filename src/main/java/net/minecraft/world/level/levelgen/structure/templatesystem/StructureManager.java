package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String STRUCTURE_DIRECTORY_NAME = "structures";
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
    private final Map<ResourceLocation, StructureTemplate> structureRepository = Maps.newHashMap();
    private final DataFixer fixerUpper;
    private ResourceManager resourceManager;
    private final Path generatedDir;

    public StructureManager(ResourceManager param0, LevelStorageSource.LevelStorageAccess param1, DataFixer param2) {
        this.resourceManager = param0;
        this.fixerUpper = param2;
        this.generatedDir = param1.getLevelPath(LevelResource.GENERATED_DIR).normalize();
    }

    public StructureTemplate getOrCreate(ResourceLocation param0) {
        StructureTemplate var0 = this.get(param0);
        if (var0 == null) {
            var0 = new StructureTemplate();
            this.structureRepository.put(param0, var0);
        }

        return var0;
    }

    @Nullable
    public StructureTemplate get(ResourceLocation param0) {
        return this.structureRepository.computeIfAbsent(param0, param0x -> {
            StructureTemplate var0 = this.loadFromGenerated(param0x);
            return var0 != null ? var0 : this.loadFromResource(param0x);
        });
    }

    public void onResourceManagerReload(ResourceManager param0) {
        this.resourceManager = param0;
        this.structureRepository.clear();
    }

    @Nullable
    private StructureTemplate loadFromResource(ResourceLocation param0) {
        ResourceLocation var0 = new ResourceLocation(param0.getNamespace(), "structures/" + param0.getPath() + ".nbt");

        try {
            StructureTemplate var4;
            try (Resource var1 = this.resourceManager.getResource(var0)) {
                var4 = this.readStructure(var1.getInputStream());
            }

            return var4;
        } catch (FileNotFoundException var8) {
            return null;
        } catch (Throwable var9) {
            LOGGER.error("Couldn't load structure {}: {}", param0, var9.toString());
            return null;
        }
    }

    @Nullable
    private StructureTemplate loadFromGenerated(ResourceLocation param0) {
        if (!this.generatedDir.toFile().isDirectory()) {
            return null;
        } else {
            Path var0 = this.createAndValidatePathToStructure(param0, ".nbt");

            try {
                StructureTemplate var4;
                try (InputStream var1 = new FileInputStream(var0.toFile())) {
                    var4 = this.readStructure(var1);
                }

                return var4;
            } catch (FileNotFoundException var8) {
                return null;
            } catch (IOException var9) {
                LOGGER.error("Couldn't load structure from {}", var0, var9);
                return null;
            }
        }
    }

    private StructureTemplate readStructure(InputStream param0) throws IOException {
        CompoundTag var0 = NbtIo.readCompressed(param0);
        return this.readStructure(var0);
    }

    public StructureTemplate readStructure(CompoundTag param0) {
        if (!param0.contains("DataVersion", 99)) {
            param0.putInt("DataVersion", 500);
        }

        StructureTemplate var0 = new StructureTemplate();
        var0.load(NbtUtils.update(this.fixerUpper, DataFixTypes.STRUCTURE, param0, param0.getInt("DataVersion")));
        return var0;
    }

    public boolean save(ResourceLocation param0) {
        StructureTemplate var0 = this.structureRepository.get(param0);
        if (var0 == null) {
            return false;
        } else {
            Path var1 = this.createAndValidatePathToStructure(param0, ".nbt");
            Path var2 = var1.getParent();
            if (var2 == null) {
                return false;
            } else {
                try {
                    Files.createDirectories(Files.exists(var2) ? var2.toRealPath() : var2);
                } catch (IOException var12) {
                    LOGGER.error("Failed to create parent directory: {}", var2);
                    return false;
                }

                CompoundTag var4 = var0.save(new CompoundTag());

                try {
                    try (OutputStream var5 = new FileOutputStream(var1.toFile())) {
                        NbtIo.writeCompressed(var4, var5);
                    }

                    return true;
                } catch (Throwable var11) {
                    return false;
                }
            }
        }
    }

    public Path createPathToStructure(ResourceLocation param0, String param1) {
        try {
            Path var0 = this.generatedDir.resolve(param0.getNamespace());
            Path var1 = var0.resolve("structures");
            return FileUtil.createPathToResource(var1, param0.getPath(), param1);
        } catch (InvalidPathException var5) {
            throw new ResourceLocationException("Invalid resource path: " + param0, var5);
        }
    }

    private Path createAndValidatePathToStructure(ResourceLocation param0, String param1) {
        if (param0.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + param0);
        } else {
            Path var0 = this.createPathToStructure(param0, param1);
            if (var0.startsWith(this.generatedDir) && FileUtil.isPathNormalized(var0) && FileUtil.isPathPortable(var0)) {
                return var0;
            } else {
                throw new ResourceLocationException("Invalid resource path: " + var0);
            }
        }
    }

    public void remove(ResourceLocation param0) {
        this.structureRepository.remove(param0);
    }
}
