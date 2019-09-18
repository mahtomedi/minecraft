package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxRenderer extends BatchedBlockEntityRenderer<ShulkerBoxBlockEntity> {
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(ShulkerModel<?> param0) {
        this.model = param0;
    }

    protected void renderToBuffer(
        ShulkerBoxBlockEntity param0,
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
        Direction var0 = Direction.UP;
        if (param0.hasLevel()) {
            BlockState var1 = this.getLevel().getBlockState(param0.getBlockPos());
            if (var1.getBlock() instanceof ShulkerBoxBlock) {
                var0 = var1.getValue(ShulkerBoxBlock.FACING);
            }
        }

        ResourceLocation var2;
        if (param5 >= 0) {
            var2 = ModelBakery.DESTROY_STAGES.get(param5);
        } else {
            DyeColor var3 = param0.getColor();
            if (var3 == null) {
                var2 = ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION;
            } else {
                var2 = ModelBakery.SHULKER_TEXTURE_LOCATION.get(var3.getId());
            }
        }

        TextureAtlasSprite var6 = this.getSprite(var2);
        param7.pushPose();
        param7.translate(0.5, 1.5, 0.5);
        param7.scale(1.0F, -1.0F, -1.0F);
        param7.translate(0.0, 1.0, 0.0);
        float var7 = 0.9995F;
        param7.scale(0.9995F, 0.9995F, 0.9995F);
        param7.translate(0.0, -1.0, 0.0);
        switch(var0) {
            case DOWN:
                param7.translate(0.0, 2.0, 0.0);
                param7.multiplyPose(new Quaternion(Vector3f.XP, 180.0F, true));
            case UP:
            default:
                break;
            case NORTH:
                param7.translate(0.0, 1.0, 1.0);
                param7.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
                param7.multiplyPose(new Quaternion(Vector3f.ZP, 180.0F, true));
                break;
            case SOUTH:
                param7.translate(0.0, 1.0, -1.0);
                param7.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
                break;
            case WEST:
                param7.translate(-1.0, 1.0, 0.0);
                param7.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
                param7.multiplyPose(new Quaternion(Vector3f.ZP, -90.0F, true));
                break;
            case EAST:
                param7.translate(1.0, 1.0, 0.0);
                param7.multiplyPose(new Quaternion(Vector3f.XP, 90.0F, true));
                param7.multiplyPose(new Quaternion(Vector3f.ZP, 90.0F, true));
        }

        this.model.getBase().render(param7, 0.0625F, param8, param9, var6);
        param7.translate(0.0, (double)(-param0.getProgress(param4) * 0.5F), 0.0);
        param7.multiplyPose(new Quaternion(Vector3f.YP, 270.0F * param0.getProgress(param4), true));
        this.model.getLid().render(param7, 0.0625F, param8, param9, var6);
        param7.popPose();
    }
}
