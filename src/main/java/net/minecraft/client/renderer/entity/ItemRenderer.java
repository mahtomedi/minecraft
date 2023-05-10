package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
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
    public static final ResourceLocation ENCHANTED_GLINT_ENTITY = new ResourceLocation("textures/misc/enchanted_glint_entity.png");
    public static final ResourceLocation ENCHANTED_GLINT_ITEM = new ResourceLocation("textures/misc/enchanted_glint_item.png");
    private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
    public static final int GUI_SLOT_CENTER_X = 8;
    public static final int GUI_SLOT_CENTER_Y = 8;
    public static final int ITEM_COUNT_BLIT_OFFSET = 200;
    public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
    public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
    public static final float COMPASS_FOIL_TEXTURE_SCALE = 0.0078125F;
    private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    public static final ModelResourceLocation TRIDENT_IN_HAND_MODEL = ModelResourceLocation.vanilla("trident_in_hand", "inventory");
    private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");
    public static final ModelResourceLocation SPYGLASS_IN_HAND_MODEL = ModelResourceLocation.vanilla("spyglass_in_hand", "inventory");
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
                if (hasAnimatedTexture(param0) && param0.hasFoil()) {
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

    private static boolean hasAnimatedTexture(ItemStack param0) {
        return param0.is(ItemTags.COMPASSES) || param0.is(Items.CLOCK);
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

    @Override
    public void onResourceManagerReload(ResourceManager param0) {
        this.itemModelShaper.rebuildCache();
    }
}
