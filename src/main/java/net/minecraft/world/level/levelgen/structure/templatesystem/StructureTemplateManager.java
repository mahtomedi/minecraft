package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class StructureTemplateManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String STRUCTURE_DIRECTORY_NAME = "structures";
    private static final String TEST_STRUCTURES_DIR = "gameteststructures";
    private static final String STRUCTURE_FILE_EXTENSION = ".nbt";
    private static final String STRUCTURE_TEXT_FILE_EXTENSION = ".snbt";
    private final Map<ResourceLocation, Optional<StructureTemplate>> structureRepository = Maps.newConcurrentMap();
    private final DataFixer fixerUpper;
    private ResourceManager resourceManager;
    private final Path generatedDir;
    private final List<StructureTemplateManager.Source> sources;
    private final HolderGetter<Block> blockLookup;
    private static final FileToIdConverter LISTER = new FileToIdConverter("structures", ".nbt");

    public StructureTemplateManager(ResourceManager param0, LevelStorageSource.LevelStorageAccess param1, DataFixer param2, HolderGetter<Block> param3) {
        this.resourceManager = param0;
        this.fixerUpper = param2;
        this.generatedDir = param1.getLevelPath(LevelResource.GENERATED_DIR).normalize();
        this.blockLookup = param3;
        Builder<StructureTemplateManager.Source> var0 = ImmutableList.builder();
        var0.add(new StructureTemplateManager.Source(this::loadFromGenerated, this::listGenerated));
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            var0.add(new StructureTemplateManager.Source(this::loadFromTestStructures, this::listTestStructures));
        }

        var0.add(new StructureTemplateManager.Source(this::loadFromResource, this::listResources));
        this.sources = var0.build();
    }

    public StructureTemplate getOrCreate(ResourceLocation param0) {
        Optional<StructureTemplate> var0 = this.get(param0);
        if (var0.isPresent()) {
            return var0.get();
        } else {
            StructureTemplate var1 = new StructureTemplate();
            this.structureRepository.put(param0, Optional.of(var1));
            return var1;
        }
    }

    public Optional<StructureTemplate> get(ResourceLocation param0) {
        return this.structureRepository.computeIfAbsent(param0, this::tryLoad);
    }

    public Stream<ResourceLocation> listTemplates() {
        return this.sources.stream().flatMap(param0 -> param0.lister().get()).distinct();
    }

    private Optional<StructureTemplate> tryLoad(ResourceLocation param0x) {
        for(StructureTemplateManager.Source var0 : this.sources) {
            try {
                Optional<StructureTemplate> var1 = var0.loader().apply(param0x);
                if (var1.isPresent()) {
                    return var1;
                }
            } catch (Exception var5) {
            }
        }

        return Optional.empty();
    }

    public void onResourceManagerReload(ResourceManager param0) {
        this.resourceManager = param0;
        this.structureRepository.clear();
    }

    private Optional<StructureTemplate> loadFromResource(ResourceLocation param0x) {
        ResourceLocation var0x = LISTER.idToFile(param0x);
        return this.load(() -> this.resourceManager.open(var0x), param1x -> LOGGER.error("Couldn't load structure {}", param0x, param1x));
    }

    private Stream<ResourceLocation> listResources() {
        return LISTER.listMatchingResources(this.resourceManager).keySet().stream().map(LISTER::fileToId);
    }

    private Optional<StructureTemplate> loadFromTestStructures(ResourceLocation param0x) {
        return this.loadFromSnbt(param0x, Paths.get("gameteststructures"));
    }

    private Stream<ResourceLocation> listTestStructures() {
        return this.listFolderContents(Paths.get("gameteststructures"), "minecraft", ".snbt");
    }

    private Optional<StructureTemplate> loadFromGenerated(ResourceLocation param0x) {
        if (!Files.isDirectory(this.generatedDir)) {
            return Optional.empty();
        } else {
            Path var0x = createAndValidatePathToStructure(this.generatedDir, param0x, ".nbt");
            return this.load(() -> new FileInputStream(var0x.toFile()), param1x -> LOGGER.error("Couldn't load structure from {}", var0x, param1x));
        }
    }

    private Stream<ResourceLocation> listGenerated() {
        if (!Files.isDirectory(this.generatedDir)) {
            return Stream.empty();
        } else {
            try {
                return Files.list(this.generatedDir).filter(param0x -> Files.isDirectory(param0x)).flatMap(param0x -> this.listGeneratedInNamespace(param0x));
            } catch (IOException var2) {
                return Stream.empty();
            }
        }
    }

    private Stream<ResourceLocation> listGeneratedInNamespace(Path param0) {
        Path var0 = param0.resolve("structures");
        return this.listFolderContents(var0, param0.getFileName().toString(), ".nbt");
    }

    private Stream<ResourceLocation> listFolderContents(Path param0, String param1, String param2) {
        if (!Files.isDirectory(param0)) {
            return Stream.empty();
        } else {
            int var0 = param2.length();
            Function<String, String> var1 = param1x -> param1x.substring(0, param1x.length() - var0);

            try {
                return Files.walk(param0).filter(param1x -> param1x.toString().endsWith(param2)).mapMulti((param3, param4) -> {
                    try {
                        param4.accept(new ResourceLocation(param1, var1.apply(this.relativize(param0, param3))));
                    } catch (ResourceLocationException var7x) {
                        LOGGER.error("Invalid location while listing pack contents", (Throwable)var7x);
                    }

                });
            } catch (IOException var7) {
                LOGGER.error("Failed to list folder contents", (Throwable)var7);
                return Stream.empty();
            }
        }
    }

    private String relativize(Path param0, Path param1) {
        return param0.relativize(param1).toString().replace(File.separator, "/");
    }

    private Optional<StructureTemplate> loadFromSnbt(ResourceLocation param0, Path param1) {
        if (!Files.isDirectory(param1)) {
            return Optional.empty();
        } else {
            Path var0 = FileUtil.createPathToResource(param1, param0.getPath(), ".snbt");

            try {
                Optional var6;
                try (BufferedReader var1 = Files.newBufferedReader(var0)) {
                    String var2 = IOUtils.toString((Reader)var1);
                    var6 = Optional.of(this.readStructure(NbtUtils.snbtToStructure(var2)));
                }

                return var6;
            } catch (NoSuchFileException var9) {
                return Optional.empty();
            } catch (CommandSyntaxException | IOException var10) {
                LOGGER.error("Couldn't load structure from {}", var0, var10);
                return Optional.empty();
            }
        }
    }

    private Optional<StructureTemplate> load(StructureTemplateManager.InputStreamOpener param0, Consumer<Throwable> param1) {
        try {
            Optional var4;
            try (InputStream var0 = param0.open()) {
                var4 = Optional.of(this.readStructure(var0));
            }

            return var4;
        } catch (FileNotFoundException var8) {
            return Optional.empty();
        } catch (Throwable var9) {
            param1.accept(var9);
            return Optional.empty();
        }
    }

    private StructureTemplate readStructure(InputStream param0) throws IOException {
        CompoundTag var0 = NbtIo.readCompressed(param0);
        return this.readStructure(var0);
    }

    public StructureTemplate readStructure(CompoundTag param0) {
        StructureTemplate var0 = new StructureTemplate();
        int var1 = NbtUtils.getDataVersion(param0, 500);
        var0.load(this.blockLookup, DataFixTypes.STRUCTURE.updateToCurrentVersion(this.fixerUpper, param0, var1));
        return var0;
    }

    public boolean save(ResourceLocation param0) {
        Optional<StructureTemplate> var0 = this.structureRepository.get(param0);
        if (var0.isEmpty()) {
            return false;
        } else {
            StructureTemplate var1 = var0.get();
            Path var2 = createAndValidatePathToStructure(this.generatedDir, param0, ".nbt");
            Path var3 = var2.getParent();
            if (var3 == null) {
                return false;
            } else {
                try {
                    Files.createDirectories(Files.exists(var3) ? var3.toRealPath() : var3);
                } catch (IOException var13) {
                    LOGGER.error("Failed to create parent directory: {}", var3);
                    return false;
                }

                CompoundTag var5 = var1.save(new CompoundTag());

                try {
                    try (OutputStream var6 = new FileOutputStream(var2.toFile())) {
                        NbtIo.writeCompressed(var5, var6);
                    }

                    return true;
                } catch (Throwable var12) {
                    return false;
                }
            }
        }
    }

    public Path getPathToGeneratedStructure(ResourceLocation param0, String param1) {
        return createPathToStructure(this.generatedDir, param0, param1);
    }

    public static Path createPathToStructure(Path param0, ResourceLocation param1, String param2) {
        try {
            Path var0 = param0.resolve(param1.getNamespace());
            Path var1 = var0.resolve("structures");
            return FileUtil.createPathToResource(var1, param1.getPath(), param2);
        } catch (InvalidPathException var5) {
            throw new ResourceLocationException("Invalid resource path: " + param1, var5);
        }
    }

    private static Path createAndValidatePathToStructure(Path param0, ResourceLocation param1, String param2) {
        if (param1.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + param1);
        } else {
            Path var0 = createPathToStructure(param0, param1, param2);
            if (var0.startsWith(param0) && FileUtil.isPathNormalized(var0) && FileUtil.isPathPortable(var0)) {
                return var0;
            } else {
                throw new ResourceLocationException("Invalid resource path: " + var0);
            }
        }
    }

    public void remove(ResourceLocation param0) {
        this.structureRepository.remove(param0);
    }

    @FunctionalInterface
    interface InputStreamOpener {
        InputStream open() throws IOException;
    }

    static record Source(Function<ResourceLocation, Optional<StructureTemplate>> loader, Supplier<Stream<ResourceLocation>> lister) {
    }
}
