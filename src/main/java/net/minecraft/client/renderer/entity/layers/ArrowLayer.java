package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.model.PlayerModel;
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
    protected void preRenderStuckItem(T param0) {
        Lighting.turnOff();
        this.arrow = new Arrow(param0.level, param0.x, param0.y, param0.z);
    }

    @Override
    protected int numStuck(T param0) {
        return param0.getArrowCount();
    }

    @Override
    protected void renderStuckItem(Entity param0, float param1, float param2, float param3, float param4) {
        float var0 = Mth.sqrt(param1 * param1 + param3 * param3);
        this.arrow.yRot = (float)(Math.atan2((double)param1, (double)param3) * 180.0F / (float)Math.PI);
        this.arrow.xRot = (float)(Math.atan2((double)param2, (double)var0) * 180.0F / (float)Math.PI);
        this.arrow.yRotO = this.arrow.yRot;
        this.arrow.xRotO = this.arrow.xRot;
        this.dispatcher.render(this.arrow, 0.0, 0.0, 0.0, 0.0F, param4, false);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
