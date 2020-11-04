package net.minecraft.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SkullModelBase extends Model {
    public SkullModelBase() {
        super(RenderType::entityTranslucent);
    }

    public abstract void setupAnim(float var1, float var2, float var3);
}
