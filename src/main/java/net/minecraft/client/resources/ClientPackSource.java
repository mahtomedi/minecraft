package net.minecraft.client.resources;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPackSource extends BuiltInPackSource {
    private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(
        Component.translatable("resourcePack.vanilla.description"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())
    );
    private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(PackMetadataSection.TYPE, VERSION_METADATA_SECTION);
    private static final Component VANILLA_NAME = Component.translatable("resourcePack.vanilla.name");
    private static final Map<String, Component> SPECIAL_PACK_NAMES = Map.of("programmer_art", Component.translatable("resourcePack.programmer_art.name"));
    private static final ResourceLocation PACKS_DIR = new ResourceLocation("minecraft", "resourcepacks");
    @Nullable
    private final Path externalAssetDir;

    public ClientPackSource(Path param0) {
        super(PackType.CLIENT_RESOURCES, createVanillaPackSource(param0), PACKS_DIR);
        this.externalAssetDir = this.findExplodedAssetPacks(param0);
    }

    @Nullable
    private Path findExplodedAssetPacks(Path param0) {
        if (SharedConstants.IS_RUNNING_IN_IDE && param0.getFileSystem() == FileSystems.getDefault()) {
            Path var0 = param0.getParent().resolve("resourcepacks");
            if (Files.isDirectory(var0)) {
                return var0;
            }
        }

        return null;
    }

    private static VanillaPackResources createVanillaPackSource(Path param0) {
        return new VanillaPackResourcesBuilder()
            .setMetadata(BUILT_IN_METADATA)
            .exposeNamespace("minecraft", "realms")
            .applyDevelopmentConfig()
            .pushJarResources()
            .pushAssetPath(PackType.CLIENT_RESOURCES, param0)
            .build();
    }

    @Override
    protected Component getPackTitle(String param0) {
        Component var0 = SPECIAL_PACK_NAMES.get(param0);
        return (Component)(var0 != null ? var0 : Component.literal(param0));
    }

    @Nullable
    @Override
    protected Pack createVanillaPack(PackResources param0) {
        return Pack.readMetaAndCreate("vanilla", VANILLA_NAME, true, param1 -> param0, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
    }

    @Nullable
    @Override
    protected Pack createBuiltinPack(String param0, Pack.ResourcesSupplier param1, Component param2) {
        return Pack.readMetaAndCreate(param0, param2, false, param1, PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
    }

    @Override
    protected void populatePackList(BiConsumer<String, Function<String, Pack>> param0) {
        super.populatePackList(param0);
        if (this.externalAssetDir != null) {
            this.discoverPacksInPath(this.externalAssetDir, param0);
        }

    }
}
