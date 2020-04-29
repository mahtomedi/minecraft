package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemPickupParticle extends Particle {
    private final RenderBuffers renderBuffers;
    private final Entity itemEntity;
    private final Entity target;
    private int life;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public ItemPickupParticle(EntityRenderDispatcher param0, RenderBuffers param1, ClientLevel param2, Entity param3, Entity param4) {
        this(param0, param1, param2, param3, param4, param3.getDeltaMovement());
    }

    private ItemPickupParticle(EntityRenderDispatcher param0, RenderBuffers param1, ClientLevel param2, Entity param3, Entity param4, Vec3 param5) {
        super(param2, param3.getX(), param3.getY(), param3.getZ(), param5.x, param5.y, param5.z);
        this.renderBuffers = param1;
        this.itemEntity = param3;
        this.target = param4;
        this.entityRenderDispatcher = param0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void render(VertexConsumer param0, Camera param1, float param2) {
        float var0 = ((float)this.life + param2) / 3.0F;
        var0 *= var0;
        double var1 = Mth.lerp((double)param2, this.target.xOld, this.target.getX());
        double var2 = Mth.lerp((double)param2, this.target.yOld, this.target.getY()) + 0.5;
        double var3 = Mth.lerp((double)param2, this.target.zOld, this.target.getZ());
        double var4 = Mth.lerp((double)var0, this.itemEntity.getX(), var1);
        double var5 = Mth.lerp((double)var0, this.itemEntity.getY(), var2);
        double var6 = Mth.lerp((double)var0, this.itemEntity.getZ(), var3);
        MultiBufferSource.BufferSource var7 = this.renderBuffers.bufferSource();
        Vec3 var8 = param1.getPosition();
        this.entityRenderDispatcher
            .render(
                this.itemEntity,
                var4 - var8.x(),
                var5 - var8.y(),
                var6 - var8.z(),
                this.itemEntity.yRot,
                param2,
                new PoseStack(),
                var7,
                this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, param2)
            );
        var7.endBatch();
    }

    @Override
    public void tick() {
        ++this.life;
        if (this.life == 3) {
            this.remove();
        }

    }
}
