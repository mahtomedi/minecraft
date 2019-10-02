package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix4f;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
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

    private void renderModelLists(BakedModel param0, ItemStack param1, int param2, PoseStack param3, VertexConsumer param4) {
        Random var0 = new Random();
        long var1 = 42L;

        for(Direction var2 : Direction.values()) {
            var0.setSeed(42L);
            this.renderQuadList(param3, param4, param0.getQuads(null, var2, var0), param1, param2);
        }

        var0.setSeed(42L);
        this.renderQuadList(param3, param4, param0.getQuads(null, null, var0), param1, param2);
    }

    public void render(
        ItemStack param0, ItemTransforms.TransformType param1, boolean param2, PoseStack param3, MultiBufferSource param4, int param5, BakedModel param6
    ) {
        if (!param0.isEmpty()) {
            param3.pushPose();
            if (param0.getItem() == Items.TRIDENT && param1 == ItemTransforms.TransformType.GUI) {
                param6 = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
            }

            param6.getTransforms().getTransform(param1).apply(param2, param3);
            param3.translate(-0.5, -0.5, -0.5);
            if (!param6.isCustomRenderer() && (param0.getItem() != Items.TRIDENT || param1 == ItemTransforms.TransformType.GUI)) {
                VertexConsumer var0 = getFoilBuffer(param4, TextureAtlas.LOCATION_BLOCKS, true, param0.hasFoil(), param6.isGui3d());
                OverlayTexture.setDefault(var0);
                this.renderModelLists(param6, param0, param5, param3, var0);
                var0.unsetDefaultOverlayCoords();
            } else {
                EntityBlockRenderer.instance.renderByItem(param0, param3, param4, param5);
            }

            param3.popPose();
        }
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource param0, ResourceLocation param1, boolean param2, boolean param3, boolean param4) {
        return (VertexConsumer)(param3
            ? new VertexMultiConsumer(
                ImmutableList.of(param0.getBuffer(param2 ? RenderType.GLINT : RenderType.ENTITY_GLINT), param0.getBuffer(RenderType.NEW_ENTITY(param1)))
            )
            : param0.getBuffer(RenderType.NEW_ENTITY(param1)));
    }

    private void renderQuadList(PoseStack param0, VertexConsumer param1, List<BakedQuad> param2, ItemStack param3, int param4) {
        boolean var0 = !param3.isEmpty();
        Matrix4f var1 = param0.getPose();

        for(BakedQuad var2 : param2) {
            int var3 = -1;
            if (var0 && var2.isTinted()) {
                var3 = this.itemColors.getColor(param3, var2.getTintIndex());
            }

            float var4 = (float)(var3 >> 16 & 0xFF) / 255.0F;
            float var5 = (float)(var3 >> 8 & 0xFF) / 255.0F;
            float var6 = (float)(var3 & 0xFF) / 255.0F;
            param1.putBulkData(var1, var2, var4, var5, var6, param4);
        }

    }

    public BakedModel getModel(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2) {
        Item var0 = param0.getItem();
        BakedModel var1;
        if (var0 == Items.TRIDENT) {
            var1 = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        } else {
            var1 = this.itemModelShaper.getItemModel(param0);
        }

        return !var0.hasProperties() ? var1 : this.resolveOverrides(var1, param0, param1, param2);
    }

    private BakedModel resolveOverrides(BakedModel param0, ItemStack param1, @Nullable Level param2, @Nullable LivingEntity param3) {
        BakedModel var0 = param0.getOverrides().resolve(param0, param1, param2, param3);
        return var0 == null ? this.itemModelShaper.getModelManager().getMissingModel() : var0;
    }

    public void renderStatic(ItemStack param0, ItemTransforms.TransformType param1, int param2, PoseStack param3, MultiBufferSource param4) {
        this.renderStatic(null, param0, param1, false, param3, param4, null, param2);
    }

    public void renderStatic(
        @Nullable LivingEntity param0,
        ItemStack param1,
        ItemTransforms.TransformType param2,
        boolean param3,
        PoseStack param4,
        MultiBufferSource param5,
        @Nullable Level param6,
        int param7
    ) {
        if (!param1.isEmpty()) {
            BakedModel var0 = this.getModel(param1, param6, param0);
            this.render(param1, param2, param3, param4, param5, param7, var0);
        }
    }

    public void renderGuiItem(ItemStack param0, int param1, int param2) {
        this.renderGuiItem(param0, param1, param2, this.getModel(param0, null, null));
    }

    protected void renderGuiItem(ItemStack param0, int param1, int param2, BakedModel param3) {
        RenderSystem.pushMatrix();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef((float)param1, (float)param2, 100.0F + this.blitOffset);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        PoseStack var0 = new PoseStack();
        MultiBufferSource.BufferSource var1 = Minecraft.getInstance().renderBuffers().bufferSource();
        this.render(param0, ItemTransforms.TransformType.GUI, false, var0, var1, 15728880, param3);
        var1.endBatch();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
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
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                param0.drawShadow(var0, (float)(param2 + 19 - 2 - param0.width(var0)), (float)(param3 + 6 + 3), 16777215);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
            }

            if (param1.isDamaged()) {
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
                RenderSystem.enableDepthTest();
            }

            LocalPlayer var8 = Minecraft.getInstance().player;
            float var9 = var8 == null ? 0.0F : var8.getCooldowns().getCooldownPercent(param1.getItem(), Minecraft.getInstance().getFrameTime());
            if (var9 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                Tesselator var10 = Tesselator.getInstance();
                BufferBuilder var11 = var10.getBuilder();
                this.fillRect(var11, param2, param3 + Mth.floor(16.0F * (1.0F - var9)), 16, Mth.ceil(16.0F * var9), 255, 255, 255, 127);
                RenderSystem.enableTexture();
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
