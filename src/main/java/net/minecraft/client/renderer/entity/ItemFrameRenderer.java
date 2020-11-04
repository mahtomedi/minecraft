package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemFrameRenderer extends EntityRenderer<ItemFrame> {
    private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;

    public ItemFrameRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.itemRenderer = param0.getItemRenderer();
    }

    public void render(ItemFrame param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        super.render(param0, param1, param2, param3, param4, param5);
        param3.pushPose();
        Direction var0 = param0.getDirection();
        Vec3 var1 = this.getRenderOffset(param0, param2);
        param3.translate(-var1.x(), -var1.y(), -var1.z());
        double var2 = 0.46875;
        param3.translate((double)var0.getStepX() * 0.46875, (double)var0.getStepY() * 0.46875, (double)var0.getStepZ() * 0.46875);
        param3.mulPose(Vector3f.XP.rotationDegrees(param0.xRot));
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F - param0.yRot));
        boolean var3 = param0.isInvisible();
        ItemStack var4 = param0.getItem();
        if (!var3) {
            BlockRenderDispatcher var5 = this.minecraft.getBlockRenderer();
            ModelManager var6 = var5.getBlockModelShaper().getModelManager();
            ModelResourceLocation var7 = var4.is(Items.FILLED_MAP) ? MAP_FRAME_LOCATION : FRAME_LOCATION;
            param3.pushPose();
            param3.translate(-0.5, -0.5, -0.5);
            var5.getModelRenderer()
                .renderModel(
                    param3.last(), param4.getBuffer(Sheets.solidBlockSheet()), null, var6.getModel(var7), 1.0F, 1.0F, 1.0F, param5, OverlayTexture.NO_OVERLAY
                );
            param3.popPose();
        }

        if (!var4.isEmpty()) {
            boolean var8 = var4.is(Items.FILLED_MAP);
            if (var3) {
                param3.translate(0.0, 0.0, 0.5);
            } else {
                param3.translate(0.0, 0.0, 0.4375);
            }

            int var9 = var8 ? param0.getRotation() % 4 * 2 : param0.getRotation();
            param3.mulPose(Vector3f.ZP.rotationDegrees((float)var9 * 360.0F / 8.0F));
            if (var8) {
                param3.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                float var10 = 0.0078125F;
                param3.scale(0.0078125F, 0.0078125F, 0.0078125F);
                param3.translate(-64.0, -64.0, 0.0);
                MapItemSavedData var11 = MapItem.getOrCreateSavedData(var4, param0.level);
                param3.translate(0.0, 0.0, -1.0);
                if (var11 != null) {
                    this.minecraft.gameRenderer.getMapRenderer().render(param3, param4, var11, true, param5);
                }
            } else {
                param3.scale(0.5F, 0.5F, 0.5F);
                this.itemRenderer.renderStatic(var4, ItemTransforms.TransformType.FIXED, param5, OverlayTexture.NO_OVERLAY, param3, param4);
            }
        }

        param3.popPose();
    }

    public Vec3 getRenderOffset(ItemFrame param0, float param1) {
        return new Vec3((double)((float)param0.getDirection().getStepX() * 0.3F), -0.25, (double)((float)param0.getDirection().getStepZ() * 0.3F));
    }

    public ResourceLocation getTextureLocation(ItemFrame param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    protected boolean shouldShowName(ItemFrame param0) {
        if (Minecraft.renderNames()
            && !param0.getItem().isEmpty()
            && param0.getItem().hasCustomHoverName()
            && this.entityRenderDispatcher.crosshairPickEntity == param0) {
            double var0 = this.entityRenderDispatcher.distanceToSqr(param0);
            float var1 = param0.isDiscrete() ? 32.0F : 64.0F;
            return var0 < (double)(var1 * var1);
        } else {
            return false;
        }
    }

    protected void renderNameTag(ItemFrame param0, Component param1, PoseStack param2, MultiBufferSource param3, int param4) {
        super.renderNameTag(param0, param0.getItem().getHoverName(), param2, param3, param4);
    }
}
