package net.minecraft.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuardianModel extends HierarchicalModel<Guardian> {
    private static final float[] SPIKE_X_ROT = new float[]{1.75F, 0.25F, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.25F, 0.75F, 0.0F, 0.0F};
    private static final float[] SPIKE_Y_ROT = new float[]{0.0F, 0.0F, 0.0F, 0.0F, 0.25F, 1.75F, 1.25F, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F};
    private static final float[] SPIKE_Z_ROT = new float[]{0.0F, 0.0F, 0.25F, 1.75F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.75F, 1.25F};
    private static final float[] SPIKE_X = new float[]{0.0F, 0.0F, 8.0F, -8.0F, -8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F, 8.0F, -8.0F};
    private static final float[] SPIKE_Y = new float[]{-8.0F, -8.0F, -8.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, 8.0F};
    private static final float[] SPIKE_Z = new float[]{8.0F, -8.0F, 0.0F, 0.0F, -8.0F, -8.0F, 8.0F, 8.0F, 8.0F, -8.0F, 0.0F, 0.0F};
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart eye;
    private final ModelPart[] spikeParts;
    private final ModelPart[] tailParts;

    public GuardianModel(ModelPart param0) {
        this.root = param0;
        this.spikeParts = new ModelPart[12];
        this.head = param0.getChild("head");

        for(int var0 = 0; var0 < this.spikeParts.length; ++var0) {
            this.spikeParts[var0] = this.head.getChild(createSpikeName(var0));
        }

        this.eye = this.head.getChild("eye");
        this.tailParts = new ModelPart[3];
        this.tailParts[0] = this.head.getChild("tail0");
        this.tailParts[1] = this.tailParts[0].getChild("tail1");
        this.tailParts[2] = this.tailParts[1].getChild("tail2");
    }

    private static String createSpikeName(int param0) {
        return "spike" + param0;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition var0 = new MeshDefinition();
        PartDefinition var1 = var0.getRoot();
        PartDefinition var2 = var1.addOrReplaceChild(
            "head",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, 10.0F, -8.0F, 12.0F, 12.0F, 16.0F)
                .texOffs(0, 28)
                .addBox(-8.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F)
                .texOffs(0, 28)
                .addBox(6.0F, 10.0F, -6.0F, 2.0F, 12.0F, 12.0F, true)
                .texOffs(16, 40)
                .addBox(-6.0F, 8.0F, -6.0F, 12.0F, 2.0F, 12.0F)
                .texOffs(16, 40)
                .addBox(-6.0F, 22.0F, -6.0F, 12.0F, 2.0F, 12.0F),
            PartPose.ZERO
        );
        CubeListBuilder var3 = CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F);

        for(int var4 = 0; var4 < 12; ++var4) {
            float var5 = getSpikeX(var4, 0.0F, 0.0F);
            float var6 = getSpikeY(var4, 0.0F, 0.0F);
            float var7 = getSpikeZ(var4, 0.0F, 0.0F);
            float var8 = (float) Math.PI * SPIKE_X_ROT[var4];
            float var9 = (float) Math.PI * SPIKE_Y_ROT[var4];
            float var10 = (float) Math.PI * SPIKE_Z_ROT[var4];
            var2.addOrReplaceChild(createSpikeName(var4), var3, PartPose.offsetAndRotation(var5, var6, var7, var8, var9, var10));
        }

        var2.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, 15.0F, 0.0F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, -8.25F));
        PartDefinition var11 = var2.addOrReplaceChild(
            "tail0", CubeListBuilder.create().texOffs(40, 0).addBox(-2.0F, 14.0F, 7.0F, 4.0F, 4.0F, 8.0F), PartPose.ZERO
        );
        PartDefinition var12 = var11.addOrReplaceChild(
            "tail1", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 14.0F, 0.0F, 3.0F, 3.0F, 7.0F), PartPose.offset(-1.5F, 0.5F, 14.0F)
        );
        var12.addOrReplaceChild(
            "tail2",
            CubeListBuilder.create().texOffs(41, 32).addBox(0.0F, 14.0F, 0.0F, 2.0F, 2.0F, 6.0F).texOffs(25, 19).addBox(1.0F, 10.5F, 3.0F, 1.0F, 9.0F, 9.0F),
            PartPose.offset(0.5F, 0.5F, 6.0F)
        );
        return LayerDefinition.create(var0, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(Guardian param0, float param1, float param2, float param3, float param4, float param5) {
        float var0 = param3 - (float)param0.tickCount;
        this.head.yRot = param4 * (float) (Math.PI / 180.0);
        this.head.xRot = param5 * (float) (Math.PI / 180.0);
        float var1 = (1.0F - param0.getSpikesAnimation(var0)) * 0.55F;
        this.setupSpikes(param3, var1);
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
        this.tailParts[2].yRot = Mth.sin(var9) * (float) Math.PI * 0.15F;
    }

    private void setupSpikes(float param0, float param1) {
        for(int var0 = 0; var0 < 12; ++var0) {
            this.spikeParts[var0].x = getSpikeX(var0, param0, param1);
            this.spikeParts[var0].y = getSpikeY(var0, param0, param1);
            this.spikeParts[var0].z = getSpikeZ(var0, param0, param1);
        }

    }

    private static float getSpikeOffset(int param0, float param1, float param2) {
        return 1.0F + Mth.cos(param1 * 1.5F + (float)param0) * 0.01F - param2;
    }

    private static float getSpikeX(int param0, float param1, float param2) {
        return SPIKE_X[param0] * getSpikeOffset(param0, param1, param2);
    }

    private static float getSpikeY(int param0, float param1, float param2) {
        return 16.0F + SPIKE_Y[param0] * getSpikeOffset(param0, param1, param2);
    }

    private static float getSpikeZ(int param0, float param1, float param2) {
        return SPIKE_Z[param0] * getSpikeOffset(param0, param1, param2);
    }
}
