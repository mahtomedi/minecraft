package net.minecraft.client.resources.model;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelManager extends SimplePreparableReloadListener<ModelBakery> implements AutoCloseable {
    private Map<ResourceLocation, BakedModel> bakedRegistry;
    private AtlasSet atlases;
    private final BlockModelShaper blockModelShaper;
    private final TextureManager textureManager;
    private final BlockColors blockColors;
    private int maxMipmapLevels;
    private BakedModel missingModel;
    private Object2IntMap<BlockState> modelGroups;

    public ModelManager(TextureManager param0, BlockColors param1, int param2) {
        this.textureManager = param0;
        this.blockColors = param1;
        this.maxMipmapLevels = param2;
        this.blockModelShaper = new BlockModelShaper(this);
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

    protected ModelBakery prepare(ResourceManager param0, ProfilerFiller param1) {
        param1.startTick();
        ModelBakery var0 = new ModelBakery(param0, this.blockColors, param1, this.maxMipmapLevels);
        param1.endTick();
        return var0;
    }

    protected void apply(ModelBakery param0, ResourceManager param1, ProfilerFiller param2) {
        param2.startTick();
        param2.push("upload");
        this.atlases = param0.uploadTextures(this.textureManager, param2);
        this.bakedRegistry = param0.getBakedTopLevelModels();
        this.modelGroups = param0.getModelGroups();
        this.missingModel = this.bakedRegistry.get(ModelBakery.MISSING_MODEL_LOCATION);
        param2.popPush("cache");
        this.blockModelShaper.rebuildCache();
        param2.pop();
        param2.endTick();
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
}
