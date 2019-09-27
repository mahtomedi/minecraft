package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityModel<T extends Entity> extends Model {
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    public void renderToBuffer(PoseStack param0, VertexConsumer param1, int param2) {
        this.renderToBuffer(param0, param1, param2, 1.0F, 1.0F, 1.0F);
    }

    public abstract void renderToBuffer(PoseStack var1, VertexConsumer var2, int var3, float var4, float var5, float var6);

    public abstract void setupAnim(T var1, float var2, float var3, float var4, float var5, float var6, float var7);

    public void prepareMobModel(T param0, float param1, float param2, float param3) {
    }

    public void copyPropertiesTo(EntityModel<T> param0) {
        param0.attackTime = this.attackTime;
        param0.riding = this.riding;
        param0.young = this.young;
    }
}
