package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityModelSet implements ResourceManagerReloadListener {
    private Map<ModelLayerLocation, ModelPart> roots = ImmutableMap.of();

    public ModelPart getLayer(ModelLayerLocation param0) {
        ModelPart var0 = this.roots.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("No model for layer " + param0);
        } else {
            return var0;
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.roots = ImmutableMap.copyOf(LayerDefinitions.createRoots());
    }
}
