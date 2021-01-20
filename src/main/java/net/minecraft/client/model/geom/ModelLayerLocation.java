package net.minecraft.client.model.geom;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ModelLayerLocation {
    private final ResourceLocation model;
    private final String layer;

    public ModelLayerLocation(ResourceLocation param0, String param1) {
        this.model = param0;
        this.layer = param1;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof ModelLayerLocation)) {
            return false;
        } else {
            ModelLayerLocation var0 = (ModelLayerLocation)param0;
            return this.model.equals(var0.model) && this.layer.equals(var0.layer);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.model.hashCode();
        return 31 * var0 + this.layer.hashCode();
    }

    @Override
    public String toString() {
        return this.model + "#" + this.layer;
    }
}
