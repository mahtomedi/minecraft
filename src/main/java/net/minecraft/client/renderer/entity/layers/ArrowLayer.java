package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
    private Arrow arrow;

    public ArrowLayer(LivingEntityRenderer<T, M> param0) {
        super(param0);
        this.dispatcher = param0.getDispatcher();
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
        this.arrow = new Arrow(param3.level, param3.getX(), param3.getY(), param3.getZ());
        this.arrow.yRot = (float)(Math.atan2((double)param4, (double)param6) * 180.0F / (float)Math.PI);
        this.arrow.xRot = (float)(Math.atan2((double)param5, (double)var0) * 180.0F / (float)Math.PI);
        this.arrow.yRotO = this.arrow.yRot;
        this.arrow.xRotO = this.arrow.xRot;
        this.dispatcher.render(this.arrow, 0.0, 0.0, 0.0, 0.0F, param7, param0, param1, param2);
    }
}
