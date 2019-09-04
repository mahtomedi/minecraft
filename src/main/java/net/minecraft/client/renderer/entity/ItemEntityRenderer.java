package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
    private final ItemRenderer itemRenderer;
    private final Random random = new Random();

    public ItemEntityRenderer(EntityRenderDispatcher param0, ItemRenderer param1) {
        super(param0);
        this.itemRenderer = param1;
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    private int setupBobbingItem(ItemEntity param0, double param1, double param2, double param3, float param4, BakedModel param5) {
        ItemStack var0 = param0.getItem();
        Item var1 = var0.getItem();
        if (var1 == null) {
            return 0;
        } else {
            boolean var2 = param5.isGui3d();
            int var3 = this.getRenderAmount(var0);
            float var4 = 0.25F;
            float var5 = Mth.sin(((float)param0.getAge() + param4) / 10.0F + param0.bobOffs) * 0.1F + 0.1F;
            float var6 = param5.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
            RenderSystem.translatef((float)param1, (float)param2 + var5 + 0.25F * var6, (float)param3);
            if (var2 || this.entityRenderDispatcher.options != null) {
                float var7 = (((float)param0.getAge() + param4) / 20.0F + param0.bobOffs) * (180.0F / (float)Math.PI);
                RenderSystem.rotatef(var7, 0.0F, 1.0F, 0.0F);
            }

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            return var3;
        }
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

    public void render(ItemEntity param0, double param1, double param2, double param3, float param4, float param5) {
        ItemStack var0 = param0.getItem();
        int var1 = var0.isEmpty() ? 187 : Item.getId(var0.getItem()) + var0.getDamageValue();
        this.random.setSeed((long)var1);
        boolean var2 = false;
        if (this.bindTexture(param0)) {
            this.entityRenderDispatcher.textureManager.getTexture(this.getTextureLocation(param0)).pushFilter(false, false);
            var2 = true;
        }

        RenderSystem.enableRescaleNormal();
        RenderSystem.alphaFunc(516, 0.1F);
        RenderSystem.enableBlend();
        Lighting.turnOn();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.pushMatrix();
        BakedModel var3 = this.itemRenderer.getModel(var0, param0.level, null);
        int var4 = this.setupBobbingItem(param0, param1, param2, param3, param5, var3);
        float var5 = var3.getTransforms().ground.scale.x();
        float var6 = var3.getTransforms().ground.scale.y();
        float var7 = var3.getTransforms().ground.scale.z();
        boolean var8 = var3.isGui3d();
        if (!var8) {
            float var9 = -0.0F * (float)(var4 - 1) * 0.5F * var5;
            float var10 = -0.0F * (float)(var4 - 1) * 0.5F * var6;
            float var11 = -0.09375F * (float)(var4 - 1) * 0.5F * var7;
            RenderSystem.translatef(var9, var10, var11);
        }

        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        for(int var12 = 0; var12 < var4; ++var12) {
            if (var8) {
                RenderSystem.pushMatrix();
                if (var12 > 0) {
                    float var13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float var14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float var15 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    RenderSystem.translatef(var13, var14, var15);
                }

                var3.getTransforms().apply(ItemTransforms.TransformType.GROUND);
                this.itemRenderer.render(var0, var3);
                RenderSystem.popMatrix();
            } else {
                RenderSystem.pushMatrix();
                if (var12 > 0) {
                    float var16 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float var17 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    RenderSystem.translatef(var16, var17, 0.0F);
                }

                var3.getTransforms().apply(ItemTransforms.TransformType.GROUND);
                this.itemRenderer.render(var0, var3);
                RenderSystem.popMatrix();
                RenderSystem.translatef(0.0F * var5, 0.0F * var6, 0.09375F * var7);
            }
        }

        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }

        RenderSystem.popMatrix();
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableBlend();
        this.bindTexture(param0);
        if (var2) {
            this.entityRenderDispatcher.textureManager.getTexture(this.getTextureLocation(param0)).popFilter();
        }

        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected ResourceLocation getTextureLocation(ItemEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
