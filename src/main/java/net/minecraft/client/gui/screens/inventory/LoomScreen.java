package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoomScreen extends AbstractContainerScreen<LoomMenu> {
    private static final ResourceLocation BANNER_SLOT_SPRITE = new ResourceLocation("container/loom/banner_slot");
    private static final ResourceLocation DYE_SLOT_SPRITE = new ResourceLocation("container/loom/dye_slot");
    private static final ResourceLocation PATTERN_SLOT_SPRITE = new ResourceLocation("container/loom/pattern_slot");
    private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/loom/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation("container/loom/scroller_disabled");
    private static final ResourceLocation PATTERN_SELECTED_SPRITE = new ResourceLocation("container/loom/pattern_selected");
    private static final ResourceLocation PATTERN_HIGHLIGHTED_SPRITE = new ResourceLocation("container/loom/pattern_highlighted");
    private static final ResourceLocation PATTERN_SPRITE = new ResourceLocation("container/loom/pattern");
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
    private static final int PATTERN_COLUMNS = 4;
    private static final int PATTERN_ROWS = 4;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int PATTERN_IMAGE_SIZE = 14;
    private static final int SCROLLER_FULL_HEIGHT = 56;
    private static final int PATTERNS_X = 60;
    private static final int PATTERNS_Y = 13;
    private ModelPart flag;
    @Nullable
    private List<Pair<Holder<BannerPattern>, DyeColor>> resultBannerPatterns;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startRow;

    public LoomScreen(LoomMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        param0.registerUpdateListener(this::containerChanged);
        this.titleLabelY -= 2;
    }

    @Override
    protected void init() {
        super.init();
        this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    private int totalRowCount() {
        return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), 4);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        param0.blit(BG_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        Slot var2 = this.menu.getBannerSlot();
        Slot var3 = this.menu.getDyeSlot();
        Slot var4 = this.menu.getPatternSlot();
        if (!var2.hasItem()) {
            param0.blitSprite(BANNER_SLOT_SPRITE, var0 + var2.x, var1 + var2.y, 16, 16);
        }

        if (!var3.hasItem()) {
            param0.blitSprite(DYE_SLOT_SPRITE, var0 + var3.x, var1 + var3.y, 16, 16);
        }

        if (!var4.hasItem()) {
            param0.blitSprite(PATTERN_SLOT_SPRITE, var0 + var4.x, var1 + var4.y, 16, 16);
        }

        int var5 = (int)(41.0F * this.scrollOffs);
        ResourceLocation var6 = this.displayPatterns ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        param0.blitSprite(var6, var0 + 119, var1 + 13 + var5, 12, 15);
        Lighting.setupForFlatItems();
        if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
            param0.pose().pushPose();
            param0.pose().translate((float)(var0 + 139), (float)(var1 + 52), 0.0F);
            param0.pose().scale(24.0F, -24.0F, 1.0F);
            param0.pose().translate(0.5F, 0.5F, 0.5F);
            float var7 = 0.6666667F;
            param0.pose().scale(0.6666667F, -0.6666667F, -0.6666667F);
            this.flag.xRot = 0.0F;
            this.flag.y = -32.0F;
            BannerRenderer.renderPatterns(
                param0.pose(), param0.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns
            );
            param0.pose().popPose();
            param0.flush();
        }

        if (this.displayPatterns) {
            int var8 = var0 + 60;
            int var9 = var1 + 13;
            List<Holder<BannerPattern>> var10 = this.menu.getSelectablePatterns();

            label63:
            for(int var11 = 0; var11 < 4; ++var11) {
                for(int var12 = 0; var12 < 4; ++var12) {
                    int var13 = var11 + this.startRow;
                    int var14 = var13 * 4 + var12;
                    if (var14 >= var10.size()) {
                        break label63;
                    }

                    int var15 = var8 + var12 * 14;
                    int var16 = var9 + var11 * 14;
                    boolean var17 = param2 >= var15 && param3 >= var16 && param2 < var15 + 14 && param3 < var16 + 14;
                    ResourceLocation var18;
                    if (var14 == this.menu.getSelectedBannerPatternIndex()) {
                        var18 = PATTERN_SELECTED_SPRITE;
                    } else if (var17) {
                        var18 = PATTERN_HIGHLIGHTED_SPRITE;
                    } else {
                        var18 = PATTERN_SPRITE;
                    }

                    param0.blitSprite(var18, var15, var16, 14, 14);
                    this.renderPattern(param0, var10.get(var14), var15, var16);
                }
            }
        }

        Lighting.setupFor3DItems();
    }

    private void renderPattern(GuiGraphics param0, Holder<BannerPattern> param1, int param2, int param3) {
        CompoundTag var0 = new CompoundTag();
        ListTag var1 = new BannerPattern.Builder().addPattern(BannerPatterns.BASE, DyeColor.GRAY).addPattern(param1, DyeColor.WHITE).toListTag();
        var0.put("Patterns", var1);
        ItemStack var2 = new ItemStack(Items.GRAY_BANNER);
        BlockItem.setBlockEntityData(var2, BlockEntityType.BANNER, var0);
        PoseStack var3 = new PoseStack();
        var3.pushPose();
        var3.translate((float)param2 + 0.5F, (float)(param3 + 16), 0.0F);
        var3.scale(6.0F, -6.0F, 1.0F);
        var3.translate(0.5F, 0.5F, 0.0F);
        var3.translate(0.5F, 0.5F, 0.5F);
        float var4 = 0.6666667F;
        var3.scale(0.6666667F, -0.6666667F, -0.6666667F);
        this.flag.xRot = 0.0F;
        this.flag.y = -32.0F;
        List<Pair<Holder<BannerPattern>, DyeColor>> var5 = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(var2));
        BannerRenderer.renderPatterns(var3, param0.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, var5);
        var3.popPose();
        param0.flush();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.scrolling = false;
        if (this.displayPatterns) {
            int var0 = this.leftPos + 60;
            int var1 = this.topPos + 13;

            for(int var2 = 0; var2 < 4; ++var2) {
                for(int var3 = 0; var3 < 4; ++var3) {
                    double var4 = param0 - (double)(var0 + var3 * 14);
                    double var5 = param1 - (double)(var1 + var2 * 14);
                    int var6 = var2 + this.startRow;
                    int var7 = var6 * 4 + var3;
                    if (var4 >= 0.0 && var5 >= 0.0 && var4 < 14.0 && var5 < 14.0 && this.menu.clickMenuButton(this.minecraft.player, var7)) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var7);
                        return true;
                    }
                }
            }

            var0 = this.leftPos + 119;
            var1 = this.topPos + 9;
            if (param0 >= (double)var0 && param0 < (double)(var0 + 12) && param1 >= (double)var1 && param1 < (double)(var1 + 56)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        int var0 = this.totalRowCount() - 4;
        if (this.scrolling && this.displayPatterns && var0 > 0) {
            int var1 = this.topPos + 13;
            int var2 = var1 + 56;
            this.scrollOffs = ((float)param1 - (float)var1 - 7.5F) / ((float)(var2 - var1) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startRow = Math.max((int)((double)(this.scrollOffs * (float)var0) + 0.5), 0);
            return true;
        } else {
            return super.mouseDragged(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        int var0 = this.totalRowCount() - 4;
        if (this.displayPatterns && var0 > 0) {
            float var1 = (float)param3 / (float)var0;
            this.scrollOffs = Mth.clamp(this.scrollOffs - var1, 0.0F, 1.0F);
            this.startRow = Math.max((int)(this.scrollOffs * (float)var0 + 0.5F), 0);
        }

        return true;
    }

    @Override
    protected boolean hasClickedOutside(double param0, double param1, int param2, int param3, int param4) {
        return param0 < (double)param2
            || param1 < (double)param3
            || param0 >= (double)(param2 + this.imageWidth)
            || param1 >= (double)(param3 + this.imageHeight);
    }

    private void containerChanged() {
        ItemStack var0 = this.menu.getResultSlot().getItem();
        if (var0.isEmpty()) {
            this.resultBannerPatterns = null;
        } else {
            this.resultBannerPatterns = BannerBlockEntity.createPatterns(((BannerItem)var0.getItem()).getColor(), BannerBlockEntity.getItemPatterns(var0));
        }

        ItemStack var1 = this.menu.getBannerSlot().getItem();
        ItemStack var2 = this.menu.getDyeSlot().getItem();
        ItemStack var3 = this.menu.getPatternSlot().getItem();
        CompoundTag var4 = BlockItem.getBlockEntityData(var1);
        this.hasMaxPatterns = var4 != null && var4.contains("Patterns", 9) && !var1.isEmpty() && var4.getList("Patterns", 10).size() >= 6;
        if (this.hasMaxPatterns) {
            this.resultBannerPatterns = null;
        }

        if (!ItemStack.matches(var1, this.bannerStack) || !ItemStack.matches(var2, this.dyeStack) || !ItemStack.matches(var3, this.patternStack)) {
            this.displayPatterns = !var1.isEmpty() && !var2.isEmpty() && !this.hasMaxPatterns && !this.menu.getSelectablePatterns().isEmpty();
        }

        if (this.startRow >= this.totalRowCount()) {
            this.startRow = 0;
            this.scrollOffs = 0.0F;
        }

        this.bannerStack = var1.copy();
        this.dyeStack = var2.copy();
        this.patternStack = var3.copy();
    }
}
