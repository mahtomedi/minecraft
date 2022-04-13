package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> param0, ItemInHandRenderer param1) {
        super(param0);
        this.itemInHandRenderer = param1;
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, Fox param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        boolean var0 = param3.isSleeping();
        boolean var1 = param3.isBaby();
        param0.pushPose();
        if (var1) {
            float var2 = 0.75F;
            param0.scale(0.75F, 0.75F, 0.75F);
            param0.translate(0.0, 0.5, 0.209375F);
        }

        param0.translate(
            (double)(this.getParentModel().head.x / 16.0F), (double)(this.getParentModel().head.y / 16.0F), (double)(this.getParentModel().head.z / 16.0F)
        );
        float var3 = param3.getHeadRollAngle(param6);
        param0.mulPose(Vector3f.ZP.rotation(var3));
        param0.mulPose(Vector3f.YP.rotationDegrees(param8));
        param0.mulPose(Vector3f.XP.rotationDegrees(param9));
        if (param3.isBaby()) {
            if (var0) {
                param0.translate(0.4F, 0.26F, 0.15F);
            } else {
                param0.translate(0.06F, 0.26F, -0.5);
            }
        } else if (var0) {
            param0.translate(0.46F, 0.26F, 0.22F);
        } else {
            param0.translate(0.06F, 0.27F, -0.5);
        }

        param0.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        if (var0) {
            param0.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        }

        ItemStack var4 = param3.getItemBySlot(EquipmentSlot.MAINHAND);
        this.itemInHandRenderer.renderItem(param3, var4, ItemTransforms.TransformType.GROUND, false, param0, param1, param2);
        param0.popPose();
    }
}
