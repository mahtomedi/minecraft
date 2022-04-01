package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DoubleSpyGlassLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel> extends RenderLayer<T, M> {
    public DoubleSpyGlassLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0, MultiBufferSource param1, int param2, T param3, float param4, float param5, float param6, float param7, float param8, float param9
    ) {
        if (param3 instanceof Skeleton var0) {
            int var1 = var0.getSpyglassesInSockets();
            if (var1 == 0) {
                return;
            }

            ItemStack var2 = new ItemStack(Items.SPYGLASS);
            double var3 = 0.15625;
            double var4 = -0.28125;
            double var5 = 0.5;
            float var6 = 0.75F;
            param0.pushPose();
            this.getParentModel().getHead().translateAndRotate(param0);
            param0.scale(0.75F, 0.75F, 0.75F);
            if (var1 >= 1) {
                param0.pushPose();
                param0.translate(-0.15625, -0.28125, 0.5);
                Minecraft.getInstance().getItemInHandRenderer().renderItem(param3, var2, ItemTransforms.TransformType.HEAD, false, param0, param1, param2);
                param0.popPose();
            }

            if (var1 >= 2) {
                param0.pushPose();
                param0.translate(0.15625, -0.28125, 0.5);
                Minecraft.getInstance().getItemInHandRenderer().renderItem(param3, var2, ItemTransforms.TransformType.HEAD, false, param0, param1, param2);
                param0.popPose();
            }

            param0.popPose();
        }

    }
}
