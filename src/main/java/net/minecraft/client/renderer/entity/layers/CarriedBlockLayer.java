package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarriedBlockLayer<T extends LivingEntity> extends RenderLayer<T, HumanoidModel<T>> {
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;

    public CarriedBlockLayer(RenderLayerParent<T, HumanoidModel<T>> param0, float param1, float param2, float param3) {
        super(param0);
        this.offsetX = param1;
        this.offsetY = param2;
        this.offsetZ = param3;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (param3.getCarried() == LivingEntity.Carried.BLOCK) {
            BlockState var0 = param3.getCarriedBlock();
            param0.pushPose();
            param0.mulPose(Vector3f.YP.rotationDegrees(param8));
            param0.mulPose(Vector3f.XP.rotationDegrees(param9));
            Item var1 = GenericItemBlock.itemFromGenericBlock(var0);
            if (var1 != null) {
                param0.translate(0.0, 0.25, -1.0);
                param0.translate((double)this.offsetX, (double)this.offsetY, (double)this.offsetZ);
                float var2 = 1.5F;
                param0.scale(-1.5F, -1.5F, 1.5F);
                ItemStack var3 = var1.getDefaultInstance();
                Minecraft.getInstance()
                    .getItemInHandRenderer()
                    .renderItem(param3, var3, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, param0, param1, param2);
            } else {
                param0.translate(0.0, 0.375, -0.75);
                param0.mulPose(Vector3f.XP.rotationDegrees(20.0F));
                param0.mulPose(Vector3f.YP.rotationDegrees(45.0F));
                param0.translate((double)this.offsetX, (double)this.offsetY, (double)this.offsetZ);
                float var4 = 0.5F;
                param0.scale(-0.5F, -0.5F, 0.5F);
                param0.mulPose(Vector3f.YP.rotationDegrees(90.0F));
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(var0, param0, param1, param2, OverlayTexture.NO_OVERLAY);
            }

            param0.popPose();
        }
    }
}
