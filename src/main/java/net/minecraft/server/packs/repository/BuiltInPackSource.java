package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public abstract class BuiltInPackSource implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_ID = "vanilla";
    private final PackType packType;
    private final VanillaPackResources vanillaPack;
    private final ResourceLocation packDir;

    public BuiltInPackSource(PackType param0, VanillaPackResources param1, ResourceLocation param2) {
        this.packType = param0;
        this.vanillaPack = param1;
        this.packDir = param2;
    }

    @Override
    public void loadPacks(Consumer<Pack> param0) {
        Pack var0 = this.createVanillaPack(this.vanillaPack);
        if (var0 != null) {
            param0.accept(var0);
        }

        this.listBundledPacks(param0);
    }

    @Nullable
    protected abstract Pack createVanillaPack(PackResources var1);

    protected abstract Component getPackTitle(String var1);

    public VanillaPackResources getVanillaPack() {
        return this.vanillaPack;
    }

    private void listBundledPacks(Consumer<Pack> param0) {
        Map<String, Function<String, Pack>> var0 = new HashMap<>();
        this.populatePackList(var0::put);
        var0.forEach((param1, param2) -> {
            Pack var0x = param2.apply(param1);
            if (var0x != null) {
                param0.accept(var0x);
            }

        });
    }

    protected void populatePackList(BiConsumer<String, Function<String, Pack>> param0) {
        this.vanillaPack.listRawPaths(this.packType, this.packDir, param1 -> this.discoverPacksInPath(param1, param0));
    }

    protected void discoverPacksInPath(@Nullable Path param0, BiConsumer<String, Function<String, Pack>> param1) {
        if (param0 != null && Files.isDirectory(param0)) {
            try {
                FolderRepositorySource.discoverPacks(
                    param0,
                    true,
                    (param1x, param2) -> param1.accept(pathToId(param1x), param1xx -> this.createBuiltinPack(param1xx, param2, this.getPackTitle(param1xx)))
                );
            } catch (IOException var4) {
                LOGGER.warn("Failed to discover packs in {}", param0, var4);
            }
        }

    }

    private static String pathToId(Path param0) {
        return StringUtils.removeEnd(param0.getFileName().toString(), ".zip");
    }

    @Nullable
    protected abstract Pack createBuiltinPack(String var1, Pack.ResourcesSupplier var2, Component var3);
}
