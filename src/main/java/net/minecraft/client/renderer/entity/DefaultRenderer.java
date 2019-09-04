package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultRenderer extends EntityRenderer<Entity> {
    public DefaultRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    @Override
    public void render(Entity param0, double param1, double param2, double param3, float param4, float param5) {
        RenderSystem.pushMatrix();
        render(param0.getBoundingBox(), param1 - param0.xOld, param2 - param0.yOld, param3 - param0.zOld);
        RenderSystem.popMatrix();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    @Override
    protected ResourceLocation getTextureLocation(Entity param0) {
        return null;
    }
}
