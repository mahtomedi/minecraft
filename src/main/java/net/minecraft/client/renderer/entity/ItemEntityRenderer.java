package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
    private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
    private static final int ITEM_COUNT_FOR_5_BUNDLE = 48;
    private static final int ITEM_COUNT_FOR_4_BUNDLE = 32;
    private static final int ITEM_COUNT_FOR_3_BUNDLE = 16;
    private static final int ITEM_COUNT_FOR_2_BUNDLE = 1;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public ItemEntityRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.itemRenderer = param0.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    private int getRenderAmount(ItemStack param0) {
        int var0 = 1;
        if (param0.getCount() > 48) {
            var0 = 5;
        } else if (param0.getCount() > 32) {
            var0 = 4;
        } else if (param0.getCount() > 16) {
            var0 = 3;
        } else if (param0.getCount() > 1) {
            var0 = 2;
        }

        return var0;
    }

    public void render(ItemEntity param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        ItemStack var0 = param0.getItem();
        int var1 = var0.isEmpty() ? 187 : Item.getId(var0.getItem()) + var0.getDamageValue();
        this.random.setSeed((long)var1);
        BakedModel var2 = this.itemRenderer.getModel(var0, param0.level, null, param0.getId());
        boolean var3 = var2.isGui3d();
        int var4 = this.getRenderAmount(var0);
        float var5 = 0.25F;
        float var6 = Mth.sin(((float)param0.getAge() + param2) / 10.0F + param0.bobOffs) * 0.1F + 0.1F;
        float var7 = var2.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        param3.translate(0.0, (double)(var6 + 0.25F * var7), 0.0);
        float var8 = param0.getSpin(param2);
        param3.mulPose(Vector3f.YP.rotation(var8));
        float var9 = var2.getTransforms().ground.scale.x();
        float var10 = var2.getTransforms().ground.scale.y();
        float var11 = var2.getTransforms().ground.scale.z();
        if (!var3) {
            float var12 = -0.0F * (float)(var4 - 1) * 0.5F * var9;
            float var13 = -0.0F * (float)(var4 - 1) * 0.5F * var10;
            float var14 = -0.09375F * (float)(var4 - 1) * 0.5F * var11;
            param3.translate((double)var12, (double)var13, (double)var14);
        }

        for(int var15 = 0; var15 < var4; ++var15) {
            param3.pushPose();
            if (var15 > 0) {
                if (var3) {
                    float var16 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float var17 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float var18 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    param3.translate((double)var16, (double)var17, (double)var18);
                } else {
                    float var19 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float var20 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    param3.translate((double)var19, (double)var20, 0.0);
                }
            }

            this.itemRenderer.render(var0, ItemTransforms.TransformType.GROUND, false, param3, param4, param5, OverlayTexture.NO_OVERLAY, var2);
            param3.popPose();
            if (!var3) {
                param3.translate((double)(0.0F * var9), (double)(0.0F * var10), (double)(0.09375F * var11));
            }
        }

        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(ItemEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
