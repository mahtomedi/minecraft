package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ModelManager implements PreparableReloadListener, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, ResourceLocation> VANILLA_ATLASES = Map.of(
        Sheets.BANNER_SHEET,
        new ResourceLocation("banner_patterns"),
        Sheets.BED_SHEET,
        new ResourceLocation("beds"),
        Sheets.CHEST_SHEET,
        new ResourceLocation("chests"),
        Sheets.SHIELD_SHEET,
        new ResourceLocation("shield_patterns"),
        Sheets.SIGN_SHEET,
        new ResourceLocation("signs"),
        Sheets.SHULKER_SHEET,
        new ResourceLocation("shulker_boxes"),
        TextureAtlas.LOCATION_BLOCKS,
        new ResourceLocation("blocks")
    );
    private Map<ResourceLocation, BakedModel> bakedRegistry;
    private final AtlasSet atlases;
    private final BlockModelShaper blockModelShaper;
    private final BlockColors blockColors;
    private int maxMipmapLevels;
    private BakedModel missingModel;
    private Object2IntMap<BlockState> modelGroups;

    public ModelManager(TextureManager param0, BlockColors param1, int param2) {
        this.blockColors = param1;
        this.maxMipmapLevels = param2;
        this.blockModelShaper = new BlockModelShaper(this);
        this.atlases = new AtlasSet(VANILLA_ATLASES, param0);
    }

    public BakedModel getModel(ModelResourceLocation param0) {
        return this.bakedRegistry.getOrDefault(param0, this.missingModel);
    }

    public BakedModel getMissingModel() {
        return this.missingModel;
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    @Override
    public final CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier param0,
        ResourceManager param1,
        ProfilerFiller param2,
        ProfilerFiller param3,
        Executor param4,
        Executor param5
    ) {
        param2.startTick();
        CompletableFuture<Map<ResourceLocation, BlockModel>> var0 = loadBlockModels(param1, param4);
        CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> var1 = loadBlockStates(param1, param4);
        CompletableFuture<ModelBakery> var2 = var0.thenCombineAsync(
            var1, (param1x, param2x) -> new ModelBakery(this.blockColors, param2, param1x, param2x), param4
        );
        Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> var3 = this.atlases.scheduleLoad(param1, this.maxMipmapLevels, param4);
        return CompletableFuture.allOf(Stream.concat(var3.values().stream(), Stream.of(var2)).toArray(param0x -> new CompletableFuture[param0x]))
            .thenApplyAsync(
                param3x -> this.loadModels(
                        param2, var3.entrySet().stream().collect(Collectors.toMap(Entry::getKey, param0x -> param0x.getValue().join())), var2.join()
                    ),
                param4
            )
            .thenCompose(param0x -> param0x.readyForUpload.thenApply(param1x -> param0x))
            .thenCompose(param0::wait)
            .thenAcceptAsync(param1x -> this.apply(param1x, param3), param5);
    }

    private static CompletableFuture<Map<ResourceLocation, BlockModel>> loadBlockModels(ResourceManager param0, Executor param1) {
        return CompletableFuture.<Map<ResourceLocation, Resource>>supplyAsync(() -> ModelBakery.MODEL_LISTER.listMatchingResources(param0), param1)
            .thenCompose(
                param1x -> {
                    List<CompletableFuture<Pair<ResourceLocation, BlockModel>>> var0x = new ArrayList(param1x.size());
        
                    for(Entry<ResourceLocation, Resource> var1x : param1x.entrySet()) {
                        var0x.add(CompletableFuture.supplyAsync(() -> {
                            try {
                                Pair var2x;
                                try (Reader var1xx = ((Resource)var1x.getValue()).openAsReader()) {
                                    var2x = Pair.of((ResourceLocation)var1x.getKey(), BlockModel.fromStream(var1xx));
                                }
        
                                return var2x;
                            } catch (Exception var6) {
                                LOGGER.error("Failed to load model {}", var1x.getKey(), var6);
                                return null;
                            }
                        }, param1));
                    }
        
                    return Util.sequence(var0x)
                        .thenApply(param0x -> param0x.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
                }
            );
    }

    private static CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> loadBlockStates(ResourceManager param0, Executor param1) {
        return CompletableFuture.<Map<ResourceLocation, List<Resource>>>supplyAsync(
                () -> ModelBakery.BLOCKSTATE_LISTER.listMatchingResourceStacks(param0), param1
            )
            .thenCompose(
                param1x -> {
                    List<CompletableFuture<Pair<ResourceLocation, List<ModelBakery.LoadedJson>>>> var0x = new ArrayList(param1x.size());
        
                    for(Entry<ResourceLocation, List<Resource>> var1x : param1x.entrySet()) {
                        var0x.add(CompletableFuture.supplyAsync(() -> {
                            List<Resource> var0xx = (List)var1x.getValue();
                            List<ModelBakery.LoadedJson> var1xx = new ArrayList(var0xx.size());
        
                            for(Resource var2x : var0xx) {
                                try (Reader var3 = var2x.openAsReader()) {
                                    JsonObject var4x = GsonHelper.parse(var3);
                                    var1xx.add(new ModelBakery.LoadedJson(var2x.sourcePackId(), var4x));
                                } catch (Exception var10) {
                                    LOGGER.error("Failed to load blockstate {} from pack {}", var1x.getKey(), var2x.sourcePackId(), var10);
                                }
                            }
        
                            return Pair.of((ResourceLocation)var1x.getKey(), var1xx);
                        }, param1));
                    }
        
                    return Util.sequence(var0x)
                        .thenApply(param0x -> param0x.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
                }
            );
    }

    private ModelManager.ReloadState loadModels(ProfilerFiller param0, Map<ResourceLocation, AtlasSet.StitchResult> param1, ModelBakery param2) {
        param0.push("load");
        param0.popPush("baking");
        Multimap<ResourceLocation, Material> var0 = HashMultimap.create();
        param2.bakeModels((param2x, param3) -> {
            AtlasSet.StitchResult var0x = param1.get(param3.atlasLocation());
            TextureAtlasSprite var1x = var0x.getSprite(param3.texture());
            if (var1x != null) {
                return var1x;
            } else {
                var0.put(param2x, param3);
                return var0x.missing();
            }
        });
        var0.asMap()
            .forEach(
                (param0x, param1x) -> LOGGER.warn(
                        "Missing textures in model {}:\n{}",
                        param0x,
                        param1x.stream()
                            .sorted(Material.COMPARATOR)
                            .map(param0xx -> "    " + param0xx.atlasLocation() + ":" + param0xx.texture())
                            .collect(Collectors.joining("\n"))
                    )
            );
        param0.popPush("dispatch");
        Map<ResourceLocation, BakedModel> var1 = param2.getBakedTopLevelModels();
        BakedModel var2 = var1.get(ModelBakery.MISSING_MODEL_LOCATION);
        Map<BlockState, BakedModel> var3 = new IdentityHashMap<>();

        for(Block var4 : BuiltInRegistries.BLOCK) {
            var4.getStateDefinition().getPossibleStates().forEach(param3 -> {
                ResourceLocation var0x = param3.getBlock().builtInRegistryHolder().key().location();
                BakedModel var1x = var1.getOrDefault(BlockModelShaper.stateToModelLocation(var0x, param3), var2);
                var3.put(param3, var1x);
            });
        }

        CompletableFuture<Void> var5 = CompletableFuture.allOf(
            param1.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray(param0x -> new CompletableFuture[param0x])
        );
        param0.pop();
        param0.endTick();
        return new ModelManager.ReloadState(param2, var2, var3, param1, var5);
    }

    private void apply(ModelManager.ReloadState param0, ProfilerFiller param1) {
        param1.startTick();
        param1.push("upload");
        param0.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
        ModelBakery var0 = param0.modelBakery;
        this.bakedRegistry = var0.getBakedTopLevelModels();
        this.modelGroups = var0.getModelGroups();
        this.missingModel = param0.missingModel;
        param1.popPush("cache");
        this.blockModelShaper.replaceCache(param0.modelCache);
        param1.pop();
        param1.endTick();
    }

    public boolean requiresRender(BlockState param0, BlockState param1) {
        if (param0 == param1) {
            return false;
        } else {
            int var0 = this.modelGroups.getInt(param0);
            if (var0 != -1) {
                int var1 = this.modelGroups.getInt(param1);
                if (var0 == var1) {
                    FluidState var2 = param0.getFluidState();
                    FluidState var3 = param1.getFluidState();
                    return var2 != var3;
                }
            }

            return true;
        }
    }

    public TextureAtlas getAtlas(ResourceLocation param0) {
        return this.atlases.getAtlas(param0);
    }

    @Override
    public void close() {
        this.atlases.close();
    }

    public void updateMaxMipLevel(int param0) {
        this.maxMipmapLevels = param0;
    }

    @OnlyIn(Dist.CLIENT)
    static record ReloadState(
        ModelBakery modelBakery,
        BakedModel missingModel,
        Map<BlockState, BakedModel> modelCache,
        Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations,
        CompletableFuture<Void> readyForUpload
    ) {
    }
}
