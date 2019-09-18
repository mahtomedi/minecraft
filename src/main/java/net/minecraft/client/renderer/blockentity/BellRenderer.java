package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellRenderer extends BatchedBlockEntityRenderer<BellBlockEntity> {
    public static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("entity/bell/bell_body");
    private final ModelPart bellBody = new ModelPart(32, 32, 0, 0);

    public BellRenderer() {
        this.bellBody.addBox(-3.0F, -6.0F, -3.0F, 6.0F, 7.0F, 6.0F);
        this.bellBody.setPos(8.0F, 12.0F, 8.0F);
        ModelPart var0 = new ModelPart(32, 32, 0, 13);
        var0.addBox(4.0F, 4.0F, 4.0F, 8.0F, 2.0F, 8.0F);
        var0.setPos(-8.0F, -12.0F, -8.0F);
        this.bellBody.addChild(var0);
    }

    protected void renderToBuffer(
        BellBlockEntity param0,
        double param1,
        double param2,
        double param3,
        float param4,
        int param5,
        RenderType param6,
        BufferBuilder param7,
        int param8,
        int param9
    ) {
        float var0 = (float)param0.ticks + param4;
        float var1 = 0.0F;
        float var2 = 0.0F;
        if (param0.shaking) {
            float var3 = Mth.sin(var0 / (float) Math.PI) / (4.0F + var0 / 3.0F);
            if (param0.clickDirection == Direction.NORTH) {
                var1 = -var3;
            } else if (param0.clickDirection == Direction.SOUTH) {
                var1 = var3;
            } else if (param0.clickDirection == Direction.EAST) {
                var2 = -var3;
            } else if (param0.clickDirection == Direction.WEST) {
                var2 = var3;
            }
        }

        this.bellBody.xRot = var1;
        this.bellBody.zRot = var2;
        param7.pushPose();
        param7.getPose().setIdentity();
        this.bellBody.render(param7, 0.0625F, param8, param9, this.getSprite(BELL_RESOURCE_LOCATION));
        param7.popPose();
    }
}
