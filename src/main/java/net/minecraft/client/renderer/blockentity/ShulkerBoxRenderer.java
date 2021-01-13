package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxRenderer extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(ShulkerModel<?> param0, BlockEntityRenderDispatcher param1) {
        super(param1);
        this.model = param0;
    }

    public void render(ShulkerBoxBlockEntity param0, float param1, PoseStack param2, MultiBufferSource param3, int param4, int param5) {
        Direction var0 = Direction.UP;
        if (param0.hasLevel()) {
            BlockState var1 = param0.getLevel().getBlockState(param0.getBlockPos());
            if (var1.getBlock() instanceof ShulkerBoxBlock) {
                var0 = var1.getValue(ShulkerBoxBlock.FACING);
            }
        }

        DyeColor var2 = param0.getColor();
        Material var3;
        if (var2 == null) {
            var3 = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
        } else {
            var3 = Sheets.SHULKER_TEXTURE_LOCATION.get(var2.getId());
        }

        param2.pushPose();
        param2.translate(0.5, 0.5, 0.5);
        float var5 = 0.9995F;
        param2.scale(0.9995F, 0.9995F, 0.9995F);
        param2.mulPose(var0.getRotation());
        param2.scale(1.0F, -1.0F, -1.0F);
        param2.translate(0.0, -1.0, 0.0);
        VertexConsumer var6 = var3.buffer(param3, RenderType::entityCutoutNoCull);
        this.model.getBase().render(param2, var6, param4, param5);
        param2.translate(0.0, (double)(-param0.getProgress(param1) * 0.5F), 0.0);
        param2.mulPose(Vector3f.YP.rotationDegrees(270.0F * param0.getProgress(param1)));
        this.model.getLid().render(param2, var6, param4, param5);
        param2.popPose();
    }
}
