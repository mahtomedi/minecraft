package net.minecraft.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuardianModel extends EntityModel<Guardian> {
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
        this.head.texOffs(0, 0).addBox(-6.0F, 10.0F, -8.0F, 12, 12, 16);
        this.head.texOffs(0, 28).addBox(-8.0F, 10.0F, -6.0F, 2, 12, 12);
        this.head.texOffs(0, 28).addBox(6.0F, 10.0F, -6.0F, 2, 12, 12, true);
        this.head.texOffs(16, 40).addBox(-6.0F, 8.0F, -6.0F, 12, 2, 12);
        this.head.texOffs(16, 40).addBox(-6.0F, 22.0F, -6.0F, 12, 2, 12);

        for(int var0 = 0; var0 < this.spikeParts.length; ++var0) {
            this.spikeParts[var0] = new ModelPart(this, 0, 0);
            this.spikeParts[var0].addBox(-1.0F, -4.5F, -1.0F, 2, 9, 2);
            this.head.addChild(this.spikeParts[var0]);
        }

        this.eye = new ModelPart(this, 8, 0);
        this.eye.addBox(-1.0F, 15.0F, 0.0F, 2, 2, 1);
        this.head.addChild(this.eye);
        this.tailParts = new ModelPart[3];
        this.tailParts[0] = new ModelPart(this, 40, 0);
        this.tailParts[0].addBox(-2.0F, 14.0F, 7.0F, 4, 4, 8);
        this.tailParts[1] = new ModelPart(this, 0, 54);
        this.tailParts[1].addBox(0.0F, 14.0F, 0.0F, 3, 3, 7);
        this.tailParts[2] = new ModelPart(this);
        this.tailParts[2].texOffs(41, 32).addBox(0.0F, 14.0F, 0.0F, 2, 2, 6);
        this.tailParts[2].texOffs(25, 19).addBox(1.0F, 10.5F, 3.0F, 1, 9, 9);
        this.head.addChild(this.tailParts[0]);
        this.tailParts[0].addChild(this.tailParts[1]);
        this.tailParts[1].addChild(this.tailParts[2]);
    }

    public void render(Guardian param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        this.setupAnim(param0, param1, param2, param3, param4, param5, param6);
        this.head.render(param6);
    }

    public void setupAnim(Guardian param0, float param1, float param2, float param3, float param4, float param5, float param6) {
        float var0 = param3 - (float)param0.tickCount;
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        float var1 = (1.0F - param0.getSpikesAnimation(var0)) * 0.55F;

        for(int var2 = 0; var2 < 12; ++var2) {
            this.spikeParts[var2].xRot = (float) Math.PI * SPIKE_X_ROT[var2];
            this.spikeParts[var2].yRot = (float) Math.PI * SPIKE_Y_ROT[var2];
            this.spikeParts[var2].zRot = (float) Math.PI * SPIKE_Z_ROT[var2];
            this.spikeParts[var2].x = SPIKE_X[var2] * (1.0F + Mth.cos(param3 * 1.5F + (float)var2) * 0.01F - var1);
            this.spikeParts[var2].y = 16.0F + SPIKE_Y[var2] * (1.0F + Mth.cos(param3 * 1.5F + (float)var2) * 0.01F - var1);
            this.spikeParts[var2].z = SPIKE_Z[var2] * (1.0F + Mth.cos(param3 * 1.5F + (float)var2) * 0.01F - var1);
        }

        this.eye.z = -8.25F;
        Entity var3 = Minecraft.getInstance().getCameraEntity();
        if (param0.hasActiveAttackTarget()) {
            var3 = param0.getActiveAttackTarget();
        }

        if (var3 != null) {
            Vec3 var4 = var3.getEyePosition(0.0F);
            Vec3 var5 = param0.getEyePosition(0.0F);
            double var6 = var4.y - var5.y;
            if (var6 > 0.0) {
                this.eye.y = 0.0F;
            } else {
                this.eye.y = 1.0F;
            }

            Vec3 var7 = param0.getViewVector(0.0F);
            var7 = new Vec3(var7.x, 0.0, var7.z);
            Vec3 var8 = new Vec3(var5.x - var4.x, 0.0, var5.z - var4.z).normalize().yRot((float) (Math.PI / 2));
            double var9 = var7.dot(var8);
            this.eye.x = Mth.sqrt((float)Math.abs(var9)) * 2.0F * (float)Math.signum(var9);
        }

        this.eye.visible = true;
        float var10 = param0.getTailAnimation(var0);
        this.tailParts[0].yRot = Mth.sin(var10) * (float) Math.PI * 0.05F;
        this.tailParts[1].yRot = Mth.sin(var10) * (float) Math.PI * 0.1F;
        this.tailParts[1].x = -1.5F;
        this.tailParts[1].y = 0.5F;
        this.tailParts[1].z = 14.0F;
        this.tailParts[2].yRot = Mth.sin(var10) * (float) Math.PI * 0.15F;
        this.tailParts[2].x = 0.5F;
        this.tailParts[2].y = 0.5F;
        this.tailParts[2].z = 6.0F;
    }
}
