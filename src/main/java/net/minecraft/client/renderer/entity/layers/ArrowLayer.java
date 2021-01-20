package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArrowLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    private final EntityRenderDispatcher dispatcher;

    public ArrowLayer(EntityRendererProvider.Context param0, LivingEntityRenderer<T, M> param1) {
        super(param1);
        this.dispatcher = param0.getEntityRenderDispatcher();
    }

    @Override
    protected int numStuck(T param0) {
        return param0.getArrowCount();
    }

    @Override
    protected void renderStuckItem(
        PoseStack param0, MultiBufferSource param1, int param2, Entity param3, float param4, float param5, float param6, float param7
    ) {
        float var0 = Mth.sqrt(param4 * param4 + param6 * param6);
        Arrow var1 = new Arrow(param3.level, param3.getX(), param3.getY(), param3.getZ());
        var1.yRot = (float)(Math.atan2((double)param4, (double)param6) * 180.0F / (float)Math.PI);
        var1.xRot = (float)(Math.atan2((double)param5, (double)var0) * 180.0F / (float)Math.PI);
        var1.yRotO = var1.yRot;
        var1.xRotO = var1.xRot;
        this.dispatcher.render(var1, 0.0, 0.0, 0.0, 0.0F, param7, param0, param1, param2);
    }
}
