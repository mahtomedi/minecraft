package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.BellModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellRenderer extends BlockEntityRenderer<BellBlockEntity> {
    private static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("textures/entity/bell/bell_body.png");
    private final BellModel bellModel = new BellModel();

    public void render(BellBlockEntity param0, double param1, double param2, double param3, float param4, int param5) {
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        this.bindTexture(BELL_RESOURCE_LOCATION);
        GlStateManager.translatef((float)param1, (float)param2, (float)param3);
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

        this.bellModel.render(var1, var2, 0.0625F);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
