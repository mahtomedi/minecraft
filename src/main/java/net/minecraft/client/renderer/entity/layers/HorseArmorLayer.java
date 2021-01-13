package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {
    private final HorseModel<Horse> model = new HorseModel<>(0.1F);

    public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> param0) {
        super(param0);
    }

    public void render(
        PoseStack param0,
        MultiBufferSource param1,
        int param2,
        Horse param3,
        float param4,
        float param5,
        float param6,
        float param7,
        float param8,
        float param9
    ) {
        ItemStack var0 = param3.getArmor();
        if (var0.getItem() instanceof HorseArmorItem) {
            HorseArmorItem var1 = (HorseArmorItem)var0.getItem();
            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(param3, param4, param5, param6);
            this.model.setupAnim(param3, param4, param5, param7, param8, param9);
            float var3;
            float var4;
            float var5;
            if (var1 instanceof DyeableHorseArmorItem) {
                int var2 = ((DyeableHorseArmorItem)var1).getColor(var0);
                var3 = (float)(var2 >> 16 & 0xFF) / 255.0F;
                var4 = (float)(var2 >> 8 & 0xFF) / 255.0F;
                var5 = (float)(var2 & 0xFF) / 255.0F;
            } else {
                var3 = 1.0F;
                var4 = 1.0F;
                var5 = 1.0F;
            }

            VertexConsumer var9 = param1.getBuffer(RenderType.entityCutoutNoCull(var1.getTexture()));
            this.model.renderToBuffer(param0, var9, param2, OverlayTexture.NO_OVERLAY, var3, var4, var5, 1.0F);
        }
    }
}
