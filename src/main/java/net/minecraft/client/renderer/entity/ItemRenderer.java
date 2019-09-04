package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemRenderer implements ResourceManagerReloadListener {
    public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
    public float blitOffset;
    private final ItemModelShaper itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;

    public ItemRenderer(TextureManager param0, ModelManager param1, ItemColors param2) {
        this.textureManager = param0;
        this.itemModelShaper = new ItemModelShaper(param1);

        for(Item var0 : Registry.ITEM) {
            if (!IGNORED.contains(var0)) {
                this.itemModelShaper.register(var0, new ModelResourceLocation(Registry.ITEM.getKey(var0), "inventory"));
            }
        }

        this.itemColors = param2;
    }

    public ItemModelShaper getItemModelShaper() {
        return this.itemModelShaper;
    }

    private void renderModelLists(BakedModel param0, ItemStack param1) {
        this.renderModelLists(param0, -1, param1);
    }

    private void renderModelLists(BakedModel param0, int param1) {
        this.renderModelLists(param0, param1, ItemStack.EMPTY);
    }

    private void renderModelLists(BakedModel param0, int param1, ItemStack param2) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(7, DefaultVertexFormat.BLOCK_NORMALS);
        Random var2 = new Random();
        long var3 = 42L;

        for(Direction var4 : Direction.values()) {
            var2.setSeed(42L);
            this.renderQuadList(var1, param0.getQuads(null, var4, var2), param1, param2);
        }

        var2.setSeed(42L);
        this.renderQuadList(var1, param0.getQuads(null, null, var2), param1, param2);
        var0.end();
    }

    public void render(ItemStack param0, BakedModel param1) {
        if (!param0.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
            if (param1.isCustomRenderer()) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableRescaleNormal();
                EntityBlockRenderer.instance.renderByItem(param0);
            } else {
                this.renderModelLists(param1, param0);
                if (param0.hasFoil()) {
                    renderFoilLayer(this.textureManager, () -> this.renderModelLists(param1, -8372020), 8);
                }
            }

            RenderSystem.popMatrix();
        }
    }

    public static void renderFoilLayer(TextureManager param0, Runnable param1, int param2) {
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(514);
        RenderSystem.disableLighting();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        param0.bind(ENCHANT_GLINT_LOCATION);
        RenderSystem.matrixMode(5890);
        RenderSystem.pushMatrix();
        RenderSystem.scalef((float)param2, (float)param2, (float)param2);
        float var0 = (float)(Util.getMillis() % 3000L) / 3000.0F / (float)param2;
        RenderSystem.translatef(var0, 0.0F, 0.0F);
        RenderSystem.rotatef(-50.0F, 0.0F, 0.0F, 1.0F);
        param1.run();
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        RenderSystem.scalef((float)param2, (float)param2, (float)param2);
        float var1 = (float)(Util.getMillis() % 4873L) / 4873.0F / (float)param2;
        RenderSystem.translatef(-var1, 0.0F, 0.0F);
        RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
        param1.run();
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableLighting();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(true);
        param0.bind(TextureAtlas.LOCATION_BLOCKS);
    }

    private void applyNormal(BufferBuilder param0, BakedQuad param1) {
        Vec3i var0 = param1.getDirection().getNormal();
        param0.postNormal((float)var0.getX(), (float)var0.getY(), (float)var0.getZ());
    }

    private void putQuadData(BufferBuilder param0, BakedQuad param1, int param2) {
        param0.putBulkData(param1.getVertices());
        param0.fixupQuadColor(param2);
        this.applyNormal(param0, param1);
    }

    private void renderQuadList(BufferBuilder param0, List<BakedQuad> param1, int param2, ItemStack param3) {
        boolean var0 = param2 == -1 && !param3.isEmpty();
        int var1 = 0;

        for(int var2 = param1.size(); var1 < var2; ++var1) {
            BakedQuad var3 = param1.get(var1);
            int var4 = param2;
            if (var0 && var3.isTinted()) {
                var4 = this.itemColors.getColor(param3, var3.getTintIndex());
                var4 |= -16777216;
            }

            this.putQuadData(param0, var3, var4);
        }

    }

    public boolean isGui3d(ItemStack param0) {
        BakedModel var0 = this.itemModelShaper.getItemModel(param0);
        return var0 == null ? false : var0.isGui3d();
    }

    public void renderStatic(ItemStack param0, ItemTransforms.TransformType param1) {
        if (!param0.isEmpty()) {
            BakedModel var0 = this.getModel(param0);
            this.renderStatic(param0, var0, param1, false);
        }
    }

    public BakedModel getModel(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2) {
        BakedModel var0 = this.itemModelShaper.getItemModel(param0);
        Item var1 = param0.getItem();
        return !var1.hasProperties() ? var0 : this.resolveOverrides(var0, param0, param1, param2);
    }

    public BakedModel getInHandModel(ItemStack param0, Level param1, LivingEntity param2) {
        Item var0 = param0.getItem();
        BakedModel var1;
        if (var0 == Items.TRIDENT) {
            var1 = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        } else {
            var1 = this.itemModelShaper.getItemModel(param0);
        }

        return !var0.hasProperties() ? var1 : this.resolveOverrides(var1, param0, param1, param2);
    }

    public BakedModel getModel(ItemStack param0) {
        return this.getModel(param0, null, null);
    }

    private BakedModel resolveOverrides(BakedModel param0, ItemStack param1, @Nullable Level param2, @Nullable LivingEntity param3) {
        BakedModel var0 = param0.getOverrides().resolve(param0, param1, param2, param3);
        return var0 == null ? this.itemModelShaper.getModelManager().getMissingModel() : var0;
    }

    public void renderWithMobState(ItemStack param0, LivingEntity param1, ItemTransforms.TransformType param2, boolean param3) {
        if (!param0.isEmpty() && param1 != null) {
            BakedModel var0 = this.getInHandModel(param0, param1.level, param1);
            this.renderStatic(param0, var0, param2, param3);
        }
    }

    protected void renderStatic(ItemStack param0, BakedModel param1, ItemTransforms.TransformType param2, boolean param3) {
        if (!param0.isEmpty()) {
            this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
            this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableRescaleNormal();
            RenderSystem.alphaFunc(516, 0.1F);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
            RenderSystem.pushMatrix();
            ItemTransforms var0 = param1.getTransforms();
            ItemTransforms.apply(var0.getTransform(param2), param3);
            if (this.needsFlip(var0.getTransform(param2))) {
                RenderSystem.cullFace(GlStateManager.CullFace.FRONT);
            }

            this.render(param0, param1);
            RenderSystem.cullFace(GlStateManager.CullFace.BACK);
            RenderSystem.popMatrix();
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableBlend();
            this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
            this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
        }
    }

    private boolean needsFlip(ItemTransform param0) {
        return param0.scale.x() < 0.0F ^ param0.scale.y() < 0.0F ^ param0.scale.z() < 0.0F;
    }

    public void renderGuiItem(ItemStack param0, int param1, int param2) {
        this.renderGuiItem(param0, param1, param2, this.getModel(param0));
    }

    protected void renderGuiItem(ItemStack param0, int param1, int param2, BakedModel param3) {
        RenderSystem.pushMatrix();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).pushFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(516, 0.1F);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.setupGuiItem(param1, param2, param3.isGui3d());
        param3.getTransforms().apply(ItemTransforms.TransformType.GUI);
        this.render(param0, param3);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableLighting();
        RenderSystem.popMatrix();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).popFilter();
    }

    private void setupGuiItem(int param0, int param1, boolean param2) {
        RenderSystem.translatef((float)param0, (float)param1, 100.0F + this.blitOffset);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        if (param2) {
            RenderSystem.enableLighting();
        } else {
            RenderSystem.disableLighting();
        }

    }

    public void renderAndDecorateItem(ItemStack param0, int param1, int param2) {
        this.renderAndDecorateItem(Minecraft.getInstance().player, param0, param1, param2);
    }

    public void renderAndDecorateItem(@Nullable LivingEntity param0, ItemStack param1, int param2, int param3) {
        if (!param1.isEmpty()) {
            this.blitOffset += 50.0F;

            try {
                this.renderGuiItem(param1, param2, param3, this.getModel(param1, null, param0));
            } catch (Throwable var8) {
                CrashReport var1 = CrashReport.forThrowable(var8, "Rendering item");
                CrashReportCategory var2 = var1.addCategory("Item being rendered");
                var2.setDetail("Item Type", () -> String.valueOf(param1.getItem()));
                var2.setDetail("Item Damage", () -> String.valueOf(param1.getDamageValue()));
                var2.setDetail("Item NBT", () -> String.valueOf(param1.getTag()));
                var2.setDetail("Item Foil", () -> String.valueOf(param1.hasFoil()));
                throw new ReportedException(var1);
            }

            this.blitOffset -= 50.0F;
        }
    }

    public void renderGuiItemDecorations(Font param0, ItemStack param1, int param2, int param3) {
        this.renderGuiItemDecorations(param0, param1, param2, param3, null);
    }

    public void renderGuiItemDecorations(Font param0, ItemStack param1, int param2, int param3, @Nullable String param4) {
        if (!param1.isEmpty()) {
            if (param1.getCount() != 1 || param4 != null) {
                String var0 = param4 == null ? String.valueOf(param1.getCount()) : param4;
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                param0.drawShadow(var0, (float)(param2 + 19 - 2 - param0.width(var0)), (float)(param3 + 6 + 3), 16777215);
                RenderSystem.enableBlend();
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }

            if (param1.isDamaged()) {
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tesselator var1 = Tesselator.getInstance();
                BufferBuilder var2 = var1.getBuilder();
                float var3 = (float)param1.getDamageValue();
                float var4 = (float)param1.getMaxDamage();
                float var5 = Math.max(0.0F, (var4 - var3) / var4);
                int var6 = Math.round(13.0F - var3 * 13.0F / var4);
                int var7 = Mth.hsvToRgb(var5 / 3.0F, 1.0F, 1.0F);
                this.fillRect(var2, param2 + 2, param3 + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(var2, param2 + 2, param3 + 13, var6, 1, var7 >> 16 & 0xFF, var7 >> 8 & 0xFF, var7 & 0xFF, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer var8 = Minecraft.getInstance().player;
            float var9 = var8 == null ? 0.0F : var8.getCooldowns().getCooldownPercent(param1.getItem(), Minecraft.getInstance().getFrameTime());
            if (var9 > 0.0F) {
                RenderSystem.disableLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                Tesselator var10 = Tesselator.getInstance();
                BufferBuilder var11 = var10.getBuilder();
                this.fillRect(var11, param2, param3 + Mth.floor(16.0F * (1.0F - var9)), 16, Mth.ceil(16.0F * var9), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableLighting();
                RenderSystem.enableDepthTest();
            }

        }
    }

    private void fillRect(BufferBuilder param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        param0.begin(7, DefaultVertexFormat.POSITION_COLOR);
        param0.vertex((double)(param1 + 0), (double)(param2 + 0), 0.0).color(param5, param6, param7, param8).endVertex();
        param0.vertex((double)(param1 + 0), (double)(param2 + param4), 0.0).color(param5, param6, param7, param8).endVertex();
        param0.vertex((double)(param1 + param3), (double)(param2 + param4), 0.0).color(param5, param6, param7, param8).endVertex();
        param0.vertex((double)(param1 + param3), (double)(param2 + 0), 0.0).color(param5, param6, param7, param8).endVertex();
        Tesselator.getInstance().end();
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.itemModelShaper.rebuildCache();
    }
}
