package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.List;
import java.util.Objects;
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
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
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

    private void renderModelLists(BakedModel param0, ItemStack param1, int param2, int param3, PoseStack param4, VertexConsumer param5) {
        Random var0 = new Random();
        long var1 = 42L;

        for(Direction var2 : Direction.values()) {
            var0.setSeed(42L);
            this.renderQuadList(param4, param5, param0.getQuads(null, var2, var0), param1, param2, param3);
        }

        var0.setSeed(42L);
        this.renderQuadList(param4, param5, param0.getQuads(null, null, var0), param1, param2, param3);
    }

    public void render(
        ItemStack param0,
        ItemTransforms.TransformType param1,
        boolean param2,
        PoseStack param3,
        MultiBufferSource param4,
        int param5,
        int param6,
        BakedModel param7
    ) {
        if (!param0.isEmpty()) {
            param3.pushPose();
            boolean var0 = param1 == ItemTransforms.TransformType.GUI;
            boolean var1 = var0 || param1 == ItemTransforms.TransformType.GROUND || param1 == ItemTransforms.TransformType.FIXED;
            if (param0.getItem() == Items.TRIDENT && var1) {
                param7 = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
            }

            param7.getTransforms().getTransform(param1).apply(param2, param3);
            param3.translate(-0.5, -0.5, -0.5);
            if (!param7.isCustomRenderer() && (param0.getItem() != Items.TRIDENT || var1)) {
                RenderType var2 = ItemBlockRenderTypes.getRenderType(param0);
                RenderType var3;
                if (var0 && Objects.equals(var2, Sheets.translucentBlockSheet())) {
                    var3 = Sheets.translucentCullBlockSheet();
                } else {
                    var3 = var2;
                }

                VertexConsumer var5 = getFoilBuffer(param4, var3, true, param0.hasFoil());
                this.renderModelLists(param7, param0, param5, param6, param3, var5);
            } else {
                BlockEntityWithoutLevelRenderer.instance.renderByItem(param0, param3, param4, param5, param6);
            }

            param3.popPose();
        }
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource param0, RenderType param1, boolean param2, boolean param3) {
        return param3
            ? VertexMultiConsumer.create(param0.getBuffer(param2 ? RenderType.glint() : RenderType.entityGlint()), param0.getBuffer(param1))
            : param0.getBuffer(param1);
    }

    private void renderQuadList(PoseStack param0, VertexConsumer param1, List<BakedQuad> param2, ItemStack param3, int param4, int param5) {
        boolean var0 = !param3.isEmpty();
        PoseStack.Pose var1 = param0.last();

        for(BakedQuad var2 : param2) {
            int var3 = -1;
            if (var0 && var2.isTinted()) {
                var3 = this.itemColors.getColor(param3, var2.getTintIndex());
            }

            float var4 = (float)(var3 >> 16 & 0xFF) / 255.0F;
            float var5 = (float)(var3 >> 8 & 0xFF) / 255.0F;
            float var6 = (float)(var3 & 0xFF) / 255.0F;
            param1.putBulkData(var1, var2, var4, var5, var6, param4, param5);
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

    public void renderStatic(ItemStack param0, ItemTransforms.TransformType param1, int param2, int param3, PoseStack param4, MultiBufferSource param5) {
        this.renderStatic(null, param0, param1, false, param4, param5, null, param2, param3);
    }

    public void renderStatic(
        @Nullable LivingEntity param0,
        ItemStack param1,
        ItemTransforms.TransformType param2,
        boolean param3,
        PoseStack param4,
        MultiBufferSource param5,
        @Nullable Level param6,
        int param7,
        int param8
    ) {
        if (!param1.isEmpty()) {
            BakedModel var0 = this.getModel(param1, param6, param0);
            this.render(param1, param2, param3, param4, param5, param7, param8, var0);
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
        this.render(param0, ItemTransforms.TransformType.GUI, false, var0, var1, 15728880, OverlayTexture.NO_OVERLAY, param3);
        var1.endBatch();
        RenderSystem.enableDepthTest();
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
            PoseStack var0 = new PoseStack();
            if (param1.getCount() != 1 || param4 != null) {
                String var1 = param4 == null ? String.valueOf(param1.getCount()) : param4;
                var0.translate(0.0, 0.0, (double)(this.blitOffset + 200.0F));
                MultiBufferSource.BufferSource var2 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                param0.drawInBatch(
                    var1, (float)(param2 + 19 - 2 - param0.width(var1)), (float)(param3 + 6 + 3), 16777215, true, var0.last().pose(), var2, false, 0, 15728880
                );
                var2.endBatch();
            }

            if (param1.isDamaged()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tesselator var3 = Tesselator.getInstance();
                BufferBuilder var4 = var3.getBuilder();
                float var5 = (float)param1.getDamageValue();
                float var6 = (float)param1.getMaxDamage();
                float var7 = Math.max(0.0F, (var6 - var5) / var6);
                int var8 = Math.round(13.0F - var5 * 13.0F / var6);
                int var9 = Mth.hsvToRgb(var7 / 3.0F, 1.0F, 1.0F);
                this.fillRect(var4, param2 + 2, param3 + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(var4, param2 + 2, param3 + 13, var8, 1, var9 >> 16 & 0xFF, var9 >> 8 & 0xFF, var9 & 0xFF, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer var10 = Minecraft.getInstance().player;
            float var11 = var10 == null ? 0.0F : var10.getCooldowns().getCooldownPercent(param1.getItem(), Minecraft.getInstance().getFrameTime());
            if (var11 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator var12 = Tesselator.getInstance();
                BufferBuilder var13 = var12.getBuilder();
                this.fillRect(var13, param2, param3 + Mth.floor(16.0F * (1.0F - var11)), 16, Mth.ceil(16.0F * var11), 255, 255, 255, 127);
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
