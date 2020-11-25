package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityModelSet implements ResourceManagerReloadListener {
    private Map<ModelLayerLocation, LayerDefinition> roots = ImmutableMap.of();

    public ModelPart bakeLayer(ModelLayerLocation param0) {
        LayerDefinition var0 = this.roots.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("No model for layer " + param0);
        } else {
            return var0.bakeRoot();
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.roots = ImmutableMap.copyOf(LayerDefinitions.createRoots());
    }
}
