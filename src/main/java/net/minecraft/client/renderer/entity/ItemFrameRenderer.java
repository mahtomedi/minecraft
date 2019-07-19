package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemFrameRenderer extends EntityRenderer<ItemFrame> {
    private static final ResourceLocation MAP_BACKGROUND_LOCATION = new ResourceLocation("textures/map/map_background.png");
    private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;

    public ItemFrameRenderer(EntityRenderDispatcher param0, ItemRenderer param1) {
        super(param0);
        this.itemRenderer = param1;
    }

    public void render(ItemFrame param0, double param1, double param2, double param3, float param4, float param5) {
        GlStateManager.pushMatrix();
        BlockPos var0 = param0.getPos();
        double var1 = (double)var0.getX() - param0.x + param1;
        double var2 = (double)var0.getY() - param0.y + param2;
        double var3 = (double)var0.getZ() - param0.z + param3;
        GlStateManager.translated(var1 + 0.5, var2 + 0.5, var3 + 0.5);
        GlStateManager.rotatef(param0.xRot, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotatef(180.0F - param0.yRot, 0.0F, 1.0F, 0.0F);
        this.entityRenderDispatcher.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        BlockRenderDispatcher var4 = this.minecraft.getBlockRenderer();
        ModelManager var5 = var4.getBlockModelShaper().getModelManager();
        ModelResourceLocation var6 = param0.getItem().getItem() == Items.FILLED_MAP ? MAP_FRAME_LOCATION : FRAME_LOCATION;
        GlStateManager.pushMatrix();
        GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(param0));
        }

        var4.getModelRenderer().renderModel(var5.getModel(var6), 1.0F, 1.0F, 1.0F, 1.0F);
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        if (param0.getItem().getItem() == Items.FILLED_MAP) {
            GlStateManager.pushLightingAttributes();
            Lighting.turnOn();
        }

        GlStateManager.translatef(0.0F, 0.0F, 0.4375F);
        this.drawItem(param0);
        if (param0.getItem().getItem() == Items.FILLED_MAP) {
            Lighting.turnOff();
            GlStateManager.popAttributes();
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        this.renderName(
            param0,
            param1 + (double)((float)param0.getDirection().getStepX() * 0.3F),
            param2 - 0.25,
            param3 + (double)((float)param0.getDirection().getStepZ() * 0.3F)
        );
    }

    @Nullable
    protected ResourceLocation getTextureLocation(ItemFrame param0) {
        return null;
    }

    private void drawItem(ItemFrame param0) {
        ItemStack var0 = param0.getItem();
        if (!var0.isEmpty()) {
            GlStateManager.pushMatrix();
            boolean var1 = var0.getItem() == Items.FILLED_MAP;
            int var2 = var1 ? param0.getRotation() % 4 * 2 : param0.getRotation();
            GlStateManager.rotatef((float)var2 * 360.0F / 8.0F, 0.0F, 0.0F, 1.0F);
            if (var1) {
                GlStateManager.disableLighting();
                this.entityRenderDispatcher.textureManager.bind(MAP_BACKGROUND_LOCATION);
                GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
                float var3 = 0.0078125F;
                GlStateManager.scalef(0.0078125F, 0.0078125F, 0.0078125F);
                GlStateManager.translatef(-64.0F, -64.0F, 0.0F);
                MapItemSavedData var4 = MapItem.getOrCreateSavedData(var0, param0.level);
                GlStateManager.translatef(0.0F, 0.0F, -1.0F);
                if (var4 != null) {
                    this.minecraft.gameRenderer.getMapRenderer().render(var4, true);
                }
            } else {
                GlStateManager.scalef(0.5F, 0.5F, 0.5F);
                this.itemRenderer.renderStatic(var0, ItemTransforms.TransformType.FIXED);
            }

            GlStateManager.popMatrix();
        }
    }

    protected void renderName(ItemFrame param0, double param1, double param2, double param3) {
        if (Minecraft.renderNames()
            && !param0.getItem().isEmpty()
            && param0.getItem().hasCustomHoverName()
            && this.entityRenderDispatcher.crosshairPickEntity == param0) {
            double var0 = param0.distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
            float var1 = param0.isVisuallySneaking() ? 32.0F : 64.0F;
            if (!(var0 >= (double)(var1 * var1))) {
                String var2 = param0.getItem().getHoverName().getColoredString();
                this.renderNameTag(param0, var2, param1, param2, param3, 64);
            }
        }
    }
}
