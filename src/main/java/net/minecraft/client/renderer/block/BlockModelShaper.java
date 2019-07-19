package net.minecraft.client.renderer.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockModelShaper {
    private final Map<BlockState, BakedModel> modelByStateCache = Maps.newIdentityHashMap();
    private final ModelManager modelManager;

    public BlockModelShaper(ModelManager param0) {
        this.modelManager = param0;
    }

    public TextureAtlasSprite getParticleIcon(BlockState param0) {
        return this.getBlockModel(param0).getParticleIcon();
    }

    public BakedModel getBlockModel(BlockState param0) {
        BakedModel var0 = this.modelByStateCache.get(param0);
        if (var0 == null) {
            var0 = this.modelManager.getMissingModel();
        }

        return var0;
    }

    public ModelManager getModelManager() {
        return this.modelManager;
    }

    public void rebuildCache() {
        this.modelByStateCache.clear();

        for(Block var0 : Registry.BLOCK) {
            var0.getStateDefinition().getPossibleStates().forEach(param0 -> {
            });
        }

    }

    public static ModelResourceLocation stateToModelLocation(BlockState param0) {
        return stateToModelLocation(Registry.BLOCK.getKey(param0.getBlock()), param0);
    }

    public static ModelResourceLocation stateToModelLocation(ResourceLocation param0, BlockState param1) {
        return new ModelResourceLocation(param0, statePropertiesToString(param1.getValues()));
    }

    public static String statePropertiesToString(Map<Property<?>, Comparable<?>> param0) {
        StringBuilder var0 = new StringBuilder();

        for(Entry<Property<?>, Comparable<?>> var1 : param0.entrySet()) {
            if (var0.length() != 0) {
                var0.append(',');
            }

            Property<?> var2 = var1.getKey();
            var0.append(var2.getName());
            var0.append('=');
            var0.append(getValue(var2, var1.getValue()));
        }

        return var0.toString();
    }

    private static <T extends Comparable<T>> String getValue(Property<T> param0, Comparable<?> param1) {
        return param0.getName((T)param1);
    }
}
