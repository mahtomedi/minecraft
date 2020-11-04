package net.minecraft.client.model.geom.builders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MaterialDefinition {
    final int xTexSize;
    final int yTexSize;

    public MaterialDefinition(int param0, int param1) {
        this.xTexSize = param0;
        this.yTexSize = param1;
    }
}
