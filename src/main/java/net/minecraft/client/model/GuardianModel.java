package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuardianModel extends ListModel<Guardian> {
    private static final float[] SPIKE_X_ROT = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
    private static final float[] SPIKE_Y_ROT = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
    private static final float[] SPIKE_Z_ROT = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
    private static final float[] SPIKE_X = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
    private static final float[] SPIKE_Y = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
    private static final float[] SPIKE_Z = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart[] spikeParts;
    private final ModelPart[] tailParts;

    public GuardianModel() {
        this.texWidth = 64;
        this.texHeight = 64;
        this.spikeParts = new ModelPart[12];
        this.head = new ModelPart(this);
        this.head.texOffs(0, 0).addBox(-6.0F, 10.0F, -8.0F, 12.0F, 12.0F, 16.0F);
        this.head.texOffs(0, 28).addBox(-8.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F);
        this.head.texOffs(0, 28).addBox(6.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F, true);
        this.head.texOffs(16, 40).addBox(-6.0F, 8.0F, -6.0F, 12.0F, 2.0F, 12.0F);
        this.head.texOffs(16, 40).addBox(-6.0F, 22.0F, -6.0F, 12.0F, 2.0F, 12.0F);

        for(int var0 = 0; var0 < this.spikeParts.length; ++var0) {
            this.spikeParts[var0] = new ModelPart(this, 0, 0);
            this.spikeParts[var0].addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);
            this.head.addChild(this.spikeParts[var0]);
        }

        this.eye = new ModelPart(this, 8, 0);
        this.eye.addBox(-1.0F, 15.0F, 0.0F, 2.0F, 2.0F, 1.0F);
        this.head.addChild(this.eye);
        this.tailParts = new ModelPart[3];
        this.tailParts[0] = new ModelPart(this, 40, 0);
        this.tailParts[0].addBox(-2.0F, 14.0F, 7.0F, 4.0F, 4.0F, 8.0F);
        this.tailParts[1] = new ModelPart(this, 0, 54);
        this.tailParts[1].addBox(0.0F, 14.0F, 0.0F, 3.0F, 3.0F, 7.0F);
        this.tailParts[2] = new ModelPart(this);
        this.tailParts[2].texOffs(41, 32).addBox(0.0F, 14.0F, 0.0F, 2.0F, 2.0F, 6.0F);
        this.tailParts[2].texOffs(25, 19).addBox(1.0F, 10.5F, 3.0F, 1.0F, 9.0F, 9.0F);
        this.head.addChild(this.tailParts[0]);
        this.tailParts[0].addChild(this.tailParts[1]);
        this.tailParts[1].addChild(this.tailParts[2]);
        this.setupSpikes(0.0F, 0.0F);
    }

    @Override
    public Iterable<ModelPart> parts() {
        return ImmutableList.of(this.head);
    }

    public void setupAnim(Guardian param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param3 - (float)param0.tickCount;
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        float var1 = (1.0F - param0.getSpikesAnimation(var0)) * 0.55F;
        this.setupSpikes(param3, var1);
        this.eye.z = -8.25F;
        Entity var2 = Minecraft.getInstance().getCameraEntity();
        if (param0.hasActiveAttackTarget()) {
            var2 = param0.getActiveAttackTarget();
        }

        if (var2 != null) {
            Vec3 var3 = var2.getEyePosition(0.0F);
            Vec3 var4 = param0.getEyePosition(0.0F);
            double var5 = var3.y - var4.y;
            if (var5 > 0.0) {
                this.eye.y = 0.0F;
            } else {
                this.eye.y = 1.0F;
            }

            Vec3 var6 = param0.getViewVector(0.0F);
            var6 = new Vec3(var6.x, 0.0, var6.z);
            Vec3 var7 = new Vec3(var4.x - var3.x, 0.0, var4.z - var3.z).normalize().yRot((float) (Math.PI / 2));
            double var8 = var6.dot(var7);
            this.eye.x = Mth.sqrt((float)Math.abs(var8)) * 2.0F * (float)Math.signum(var8);
        }

        this.eye.visible = true;
        float var9 = param0.getTailAnimation(var0);
        this.tailParts[0].yRot = Mth.sin(var9) * (float) Math.PI * 0.05F;
        this.tailParts[1].yRot = Mth.sin(var9) * (float) Math.PI * 0.1F;
        this.tailParts[1].x = -1.5F;
        this.tailParts[1].y = 0.5F;
        this.tailParts[1].z = 14.0F;
        this.tailParts[2].yRot = Mth.sin(var9) * (float) Math.PI * 0.15F;
        this.tailParts[2].x = 0.5F;
        this.tailParts[2].y = 0.5F;
        this.tailParts[2].z = 6.0F;
    }

    private void setupSpikes(float param0, float param1) {
        for(int var0 = 0; var0 < 12; ++var0) {
            this.spikeParts[var0].xRot = (float) Math.PI * SPIKE_X_ROT[var0];
            this.spikeParts[var0].yRot = (float) Math.PI * SPIKE_Y_ROT[var0];
            this.spikeParts[var0].zRot = (float) Math.PI * SPIKE_Z_ROT[var0];
            this.spikeParts[var0].x = SPIKE_X[var0] * (1.0F + Mth.cos(param0 * 1.5F + (float)var0) * 0.01F - param1);
            this.spikeParts[var0].y = 16.0F + SPIKE_Y[var0] * (1.0F + Mth.cos(param0 * 1.5F + (float)var0) * 0.01F - param1);
            this.spikeParts[var0].z = SPIKE_Z[var0] * (1.0F + Mth.cos(param0 * 1.5F + (float)var0) * 0.01F - param1);
        }

    }
}
