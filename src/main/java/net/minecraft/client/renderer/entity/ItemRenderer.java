package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemRenderer implements ResourceManagerReloadListener {
    public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
    private static final int GUI_SLOT_CENTER_X = 8;
    private static final int GUI_SLOT_CENTER_Y = 8;
    public static final int ITEM_COUNT_BLIT_OFFSET = 200;
    public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
    public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
    public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125F;
    private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    public static final ModelResourceLocation TRIDENT_IN_HAND_MODEL = ModelResourceLocation.vanilla("trident_in_hand", "inventory");
    private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");
    public static final ModelResourceLocation SPYGLASS_IN_HAND_MODEL = ModelResourceLocation.vanilla("spyglass_in_hand", "inventory");
    public float blitOffset;
    private final Minecraft minecraft;
    private final ItemModelShaper itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;
    private final BlockEntityWithoutLevelRenderer blockEntityRenderer;

    public ItemRenderer(Minecraft param0, TextureManager param1, ModelManager param2, ItemColors param3, BlockEntityWithoutLevelRenderer param4) {
        this.minecraft = param0;
        this.textureManager = param1;
        this.itemModelShaper = new ItemModelShaper(param2);
        this.blockEntityRenderer = param4;

        for(Item var0 : BuiltInRegistries.ITEM) {
            if (!IGNORED.contains(var0)) {
                this.itemModelShaper.register(var0, new ModelResourceLocation(BuiltInRegistries.ITEM.getKey(var0), "inventory"));
            }
        }

        this.itemColors = param3;
    }

    public ItemModelShaper getItemModelShaper() {
        return this.itemModelShaper;
    }

    private void renderModelLists(BakedModel param0, ItemStack param1, int param2, int param3, PoseStack param4, VertexConsumer param5) {
        RandomSource var0 = RandomSource.create();
        long var1 = 42L;

        for(Direction var2 : Direction.values()) {
            var0.setSeed(42L);
            this.renderQuadList(param4, param5, param0.getQuads(null, var2, var0), param1, param2, param3);
        }

        var0.setSeed(42L);
        this.renderQuadList(param4, param5, param0.getQuads(null, null, var0), param1, param2, param3);
    }

    public void render(
        ItemStack param0, ItemDisplayContext param1, boolean param2, PoseStack param3, MultiBufferSource param4, int param5, int param6, BakedModel param7
    ) {
        if (!param0.isEmpty()) {
            param3.pushPose();
            boolean var0 = param1 == ItemDisplayContext.GUI || param1 == ItemDisplayContext.GROUND || param1 == ItemDisplayContext.FIXED;
            if (var0) {
                if (param0.is(Items.TRIDENT)) {
                    param7 = this.itemModelShaper.getModelManager().getModel(TRIDENT_MODEL);
                } else if (param0.is(Items.SPYGLASS)) {
                    param7 = this.itemModelShaper.getModelManager().getModel(SPYGLASS_MODEL);
                }
            }

            param7.getTransforms().getTransform(param1).apply(param2, param3);
            param3.translate(-0.5F, -0.5F, -0.5F);
            if (!param7.isCustomRenderer() && (!param0.is(Items.TRIDENT) || var0)) {
                boolean var2;
                if (param1 != ItemDisplayContext.GUI && !param1.firstPerson() && param0.getItem() instanceof BlockItem) {
                    Block var1 = ((BlockItem)param0.getItem()).getBlock();
                    var2 = !(var1 instanceof HalfTransparentBlock) && !(var1 instanceof StainedGlassPaneBlock);
                } else {
                    var2 = true;
                }

                RenderType var4 = ItemBlockRenderTypes.getRenderType(param0, var2);
                VertexConsumer var6;
                if (param0.is(ItemTags.COMPASSES) && param0.hasFoil()) {
                    param3.pushPose();
                    PoseStack.Pose var5 = param3.last();
                    if (param1 == ItemDisplayContext.GUI) {
                        MatrixUtil.mulComponentWise(var5.pose(), 0.5F);
                    } else if (param1.firstPerson()) {
                        MatrixUtil.mulComponentWise(var5.pose(), 0.75F);
                    }

                    if (var2) {
                        var6 = getCompassFoilBufferDirect(param4, var4, var5);
                    } else {
                        var6 = getCompassFoilBuffer(param4, var4, var5);
                    }

                    param3.popPose();
                } else if (var2) {
                    var6 = getFoilBufferDirect(param4, var4, true, param0.hasFoil());
                } else {
                    var6 = getFoilBuffer(param4, var4, true, param0.hasFoil());
                }

                this.renderModelLists(param7, param0, param5, param6, param3, var6);
            } else {
                this.blockEntityRenderer.renderByItem(param0, param1, param3, param4, param5, param6);
            }

            param3.popPose();
        }
    }

    public static VertexConsumer getArmorFoilBuffer(MultiBufferSource param0, RenderType param1, boolean param2, boolean param3) {
        return param3
            ? VertexMultiConsumer.create(param0.getBuffer(param2 ? RenderType.armorGlint() : RenderType.armorEntityGlint()), param0.getBuffer(param1))
            : param0.getBuffer(param1);
    }

    public static VertexConsumer getCompassFoilBuffer(MultiBufferSource param0, RenderType param1, PoseStack.Pose param2) {
        return VertexMultiConsumer.create(
            new SheetedDecalTextureGenerator(param0.getBuffer(RenderType.glint()), param2.pose(), param2.normal(), 0.0078125F), param0.getBuffer(param1)
        );
    }

    public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource param0, RenderType param1, PoseStack.Pose param2) {
        return VertexMultiConsumer.create(
            new SheetedDecalTextureGenerator(param0.getBuffer(RenderType.glintDirect()), param2.pose(), param2.normal(), 0.0078125F), param0.getBuffer(param1)
        );
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource param0, RenderType param1, boolean param2, boolean param3) {
        if (param3) {
            return Minecraft.useShaderTransparency() && param1 == Sheets.translucentItemSheet()
                ? VertexMultiConsumer.create(param0.getBuffer(RenderType.glintTranslucent()), param0.getBuffer(param1))
                : VertexMultiConsumer.create(param0.getBuffer(param2 ? RenderType.glint() : RenderType.entityGlint()), param0.getBuffer(param1));
        } else {
            return param0.getBuffer(param1);
        }
    }

    public static VertexConsumer getFoilBufferDirect(MultiBufferSource param0, RenderType param1, boolean param2, boolean param3) {
        return param3
            ? VertexMultiConsumer.create(param0.getBuffer(param2 ? RenderType.glintDirect() : RenderType.entityGlintDirect()), param0.getBuffer(param1))
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

    public BakedModel getModel(ItemStack param0, @Nullable Level param1, @Nullable LivingEntity param2, int param3) {
        BakedModel var0;
        if (param0.is(Items.TRIDENT)) {
            var0 = this.itemModelShaper.getModelManager().getModel(TRIDENT_IN_HAND_MODEL);
        } else if (param0.is(Items.SPYGLASS)) {
            var0 = this.itemModelShaper.getModelManager().getModel(SPYGLASS_IN_HAND_MODEL);
        } else {
            var0 = this.itemModelShaper.getItemModel(param0);
        }

        ClientLevel var3 = param1 instanceof ClientLevel ? (ClientLevel)param1 : null;
        BakedModel var4 = var0.getOverrides().resolve(var0, param0, var3, param2, param3);
        return var4 == null ? this.itemModelShaper.getModelManager().getMissingModel() : var4;
    }

    public void renderStatic(
        ItemStack param0, ItemDisplayContext param1, int param2, int param3, PoseStack param4, MultiBufferSource param5, @Nullable Level param6, int param7
    ) {
        this.renderStatic(null, param0, param1, false, param4, param5, param6, param2, param3, param7);
    }

    public void renderStatic(
        @Nullable LivingEntity param0,
        ItemStack param1,
        ItemDisplayContext param2,
        boolean param3,
        PoseStack param4,
        MultiBufferSource param5,
        @Nullable Level param6,
        int param7,
        int param8,
        int param9
    ) {
        if (!param1.isEmpty()) {
            BakedModel var0 = this.getModel(param1, param6, param0, param9);
            this.render(param1, param2, param3, param4, param5, param7, param8, var0);
        }
    }

    public void renderGuiItem(ItemStack param0, int param1, int param2) {
        this.renderGuiItem(param0, param1, param2, this.getModel(param0, null, null, 0));
    }

    protected void renderGuiItem(ItemStack param0, int param1, int param2, BakedModel param3) {
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        PoseStack var0 = RenderSystem.getModelViewStack();
        var0.pushPose();
        var0.translate((float)param1, (float)param2, 100.0F + this.blitOffset);
        var0.translate(8.0F, 8.0F, 0.0F);
        var0.scale(1.0F, -1.0F, 1.0F);
        var0.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack var1 = new PoseStack();
        MultiBufferSource.BufferSource var2 = this.minecraft.renderBuffers().bufferSource();
        boolean var3 = !param3.usesBlockLight();
        if (var3) {
            Lighting.setupForFlatItems();
        }

        this.render(param0, ItemDisplayContext.GUI, false, var1, var2, 15728880, OverlayTexture.NO_OVERLAY, param3);
        var2.endBatch();
        RenderSystem.enableDepthTest();
        if (var3) {
            Lighting.setupFor3DItems();
        }

        var0.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public void renderAndDecorateItem(ItemStack param0, int param1, int param2) {
        this.tryRenderGuiItem(this.minecraft.player, this.minecraft.level, param0, param1, param2, 0);
    }

    public void renderAndDecorateItem(ItemStack param0, int param1, int param2, int param3) {
        this.tryRenderGuiItem(this.minecraft.player, this.minecraft.level, param0, param1, param2, param3);
    }

    public void renderAndDecorateItem(ItemStack param0, int param1, int param2, int param3, int param4) {
        this.tryRenderGuiItem(this.minecraft.player, this.minecraft.level, param0, param1, param2, param3, param4);
    }

    public void renderAndDecorateFakeItem(ItemStack param0, int param1, int param2) {
        this.tryRenderGuiItem(null, this.minecraft.level, param0, param1, param2, 0);
    }

    public void renderAndDecorateItem(LivingEntity param0, ItemStack param1, int param2, int param3, int param4) {
        this.tryRenderGuiItem(param0, param0.level, param1, param2, param3, param4);
    }

    private void tryRenderGuiItem(@Nullable LivingEntity param0, @Nullable Level param1, ItemStack param2, int param3, int param4, int param5) {
        this.tryRenderGuiItem(param0, param1, param2, param3, param4, param5, 0);
    }

    private void tryRenderGuiItem(@Nullable LivingEntity param0, @Nullable Level param1, ItemStack param2, int param3, int param4, int param5, int param6) {
        if (!param2.isEmpty()) {
            BakedModel var0 = this.getModel(param2, param1, param0, param5);
            this.blitOffset = var0.isGui3d() ? this.blitOffset + 50.0F + (float)param6 : this.blitOffset + 50.0F;

            try {
                this.renderGuiItem(param2, param3, param4, var0);
            } catch (Throwable var12) {
                CrashReport var2 = CrashReport.forThrowable(var12, "Rendering item");
                CrashReportCategory var3 = var2.addCategory("Item being rendered");
                var3.setDetail("Item Type", () -> String.valueOf(param2.getItem()));
                var3.setDetail("Item Damage", () -> String.valueOf(param2.getDamageValue()));
                var3.setDetail("Item NBT", () -> String.valueOf(param2.getTag()));
                var3.setDetail("Item Foil", () -> String.valueOf(param2.hasFoil()));
                throw new ReportedException(var2);
            }

            this.blitOffset = var0.isGui3d() ? this.blitOffset - 50.0F - (float)param6 : this.blitOffset - 50.0F;
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
                var0.translate(0.0F, 0.0F, this.blitOffset + 200.0F);
                MultiBufferSource.BufferSource var2 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                param0.drawInBatch(
                    var1, (float)(param2 + 19 - 2 - param0.width(var1)), (float)(param3 + 6 + 3), 16777215, true, var0.last().pose(), var2, false, 0, 15728880
                );
                var2.endBatch();
            }

            if (param1.isBarVisible()) {
                RenderSystem.disableDepthTest();
                int var3 = param1.getBarWidth();
                int var4 = param1.getBarColor();
                int var5 = param2 + 2;
                int var6 = param3 + 13;
                GuiComponent.fill(var0, var5, var6, var5 + 13, var6 + 2, -16777216);
                GuiComponent.fill(var0, var5, var6, var5 + var3, var6 + 1, var4 | 0xFF000000);
                RenderSystem.enableDepthTest();
            }

            LocalPlayer var7 = this.minecraft.player;
            float var8 = var7 == null ? 0.0F : var7.getCooldowns().getCooldownPercent(param1.getItem(), this.minecraft.getFrameTime());
            if (var8 > 0.0F) {
                RenderSystem.disableDepthTest();
                int var9 = param3 + Mth.floor(16.0F * (1.0F - var8));
                int var10 = var9 + Mth.ceil(16.0F * var8);
                GuiComponent.fill(var0, param2, var9, param2 + 16, var10, Integer.MAX_VALUE);
                RenderSystem.enableDepthTest();
            }

        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.itemModelShaper.rebuildCache();
    }
}
