package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoomScreen extends AbstractContainerScreen<LoomMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
    private static final int TOTAL_PATTERN_ROWS = (BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT - 1 + 4 - 1) / 4;
    private final ModelPart flag;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> resultBannerPatterns;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private boolean displaySpecialPattern;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex = 1;

    public LoomScreen(LoomMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        this.flag = BannerRenderer.makeFlag();
        param0.registerUpdateListener(this::containerChanged);
        this.titleLabelY -= 2;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        this.renderBackground(param0);
        this.minecraft.getTextureManager().bind(BG_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        Slot var2 = this.menu.getBannerSlot();
        Slot var3 = this.menu.getDyeSlot();
        Slot var4 = this.menu.getPatternSlot();
        Slot var5 = this.menu.getResultSlot();
        if (!var2.hasItem()) {
            this.blit(param0, var0 + var2.x, var1 + var2.y, this.imageWidth, 0, 16, 16);
        }

        if (!var3.hasItem()) {
            this.blit(param0, var0 + var3.x, var1 + var3.y, this.imageWidth + 16, 0, 16, 16);
        }

        if (!var4.hasItem()) {
            this.blit(param0, var0 + var4.x, var1 + var4.y, this.imageWidth + 32, 0, 16, 16);
        }

        int var6 = (int)(41.0F * this.scrollOffs);
        this.blit(param0, var0 + 119, var1 + 13 + var6, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
        Lighting.setupForFlatItems();
        if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
            MultiBufferSource.BufferSource var7 = this.minecraft.renderBuffers().bufferSource();
            param0.pushPose();
            param0.translate((double)(var0 + 139), (double)(var1 + 52), 0.0);
            param0.scale(24.0F, -24.0F, 1.0F);
            param0.translate(0.5, 0.5, 0.5);
            float var8 = 0.6666667F;
            param0.scale(0.6666667F, -0.6666667F, -0.6666667F);
            this.flag.xRot = 0.0F;
            this.flag.y = -32.0F;
            BannerRenderer.renderPatterns(
                param0, var7, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns
            );
            param0.popPose();
            var7.endBatch();
        } else if (this.hasMaxPatterns) {
            this.blit(param0, var0 + var5.x - 2, var1 + var5.y - 2, this.imageWidth, 17, 17, 16);
        }

        if (this.displayPatterns) {
            int var9 = var0 + 60;
            int var10 = var1 + 13;
            int var11 = this.startIndex + 16;

            for(int var12 = this.startIndex; var12 < var11 && var12 < BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT; ++var12) {
                int var13 = var12 - this.startIndex;
                int var14 = var9 + var13 % 4 * 14;
                int var15 = var10 + var13 / 4 * 14;
                this.minecraft.getTextureManager().bind(BG_LOCATION);
                int var16 = this.imageHeight;
                if (var12 == this.menu.getSelectedBannerPatternIndex()) {
                    var16 += 14;
                } else if (param2 >= var14 && param3 >= var15 && param2 < var14 + 14 && param3 < var15 + 14) {
                    var16 += 28;
                }

                this.blit(param0, var14, var15, 0, var16, 14, 14);
                this.renderPattern(var12, var14, var15);
            }
        } else if (this.displaySpecialPattern) {
            int var17 = var0 + 60;
            int var18 = var1 + 13;
            this.minecraft.getTextureManager().bind(BG_LOCATION);
            this.blit(param0, var17, var18, 0, this.imageHeight, 14, 14);
            int var19 = this.menu.getSelectedBannerPatternIndex();
            this.renderPattern(var19, var17, var18);
        }

        Lighting.setupFor3DItems();
    }

    private void renderPattern(int param0, int param1, int param2) {
        ItemStack var0 = new ItemStack(Items.GRAY_BANNER);
        CompoundTag var1 = var0.getOrCreateTagElement("BlockEntityTag");
        ListTag var2 = new BannerPattern.Builder()
            .addPattern(BannerPattern.BASE, DyeColor.GRAY)
            .addPattern(BannerPattern.values()[param0], DyeColor.WHITE)
            .toListTag();
        var1.put("Patterns", var2);
        PoseStack var3 = new PoseStack();
        var3.pushPose();
        var3.translate((double)((float)param1 + 0.5F), (double)(param2 + 16), 0.0);
        var3.scale(6.0F, -6.0F, 1.0F);
        var3.translate(0.5, 0.5, 0.0);
        var3.translate(0.5, 0.5, 0.5);
        float var4 = 0.6666667F;
        var3.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource var5 = this.minecraft.renderBuffers().bufferSource();
        this.flag.xRot = 0.0F;
        this.flag.y = -32.0F;
        List<Pair<BannerPattern, DyeColor>> var6 = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(var0));
        BannerRenderer.renderPatterns(var3, var5, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, var6);
        var3.popPose();
        var5.endBatch();
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.scrolling = false;
        if (this.displayPatterns) {
            int var0 = this.leftPos + 60;
            int var1 = this.topPos + 13;
            int var2 = this.startIndex + 16;

            for(int var3 = this.startIndex; var3 < var2; ++var3) {
                int var4 = var3 - this.startIndex;
                double var5 = param0 - (double)(var0 + var4 % 4 * 14);
                double var6 = param1 - (double)(var1 + var4 / 4 * 14);
                if (var5 >= 0.0 && var6 >= 0.0 && var5 < 14.0 && var6 < 14.0 && this.menu.clickMenuButton(this.minecraft.player, var3)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var3);
                    return true;
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
        if (this.scrolling && this.displayPatterns) {
            int var0 = this.topPos + 13;
            int var1 = var0 + 56;
            this.scrollOffs = ((float)param1 - (float)var0 - 7.5F) / ((float)(var1 - var0) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            int var2 = TOTAL_PATTERN_ROWS - 4;
            int var3 = (int)((double)(this.scrollOffs * (float)var2) + 0.5);
            if (var3 < 0) {
                var3 = 0;
            }

            this.startIndex = 1 + var3 * 4;
            return true;
        } else {
            return super.mouseDragged(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2) {
        if (this.displayPatterns) {
            int var0 = TOTAL_PATTERN_ROWS - 4;
            this.scrollOffs = (float)((double)this.scrollOffs - param2 / (double)var0);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = 1 + (int)((double)(this.scrollOffs * (float)var0) + 0.5) * 4;
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
        CompoundTag var4 = var1.getOrCreateTagElement("BlockEntityTag");
        this.hasMaxPatterns = var4.contains("Patterns", 9) && !var1.isEmpty() && var4.getList("Patterns", 10).size() >= 6;
        if (this.hasMaxPatterns) {
            this.resultBannerPatterns = null;
        }

        if (!ItemStack.matches(var1, this.bannerStack) || !ItemStack.matches(var2, this.dyeStack) || !ItemStack.matches(var3, this.patternStack)) {
            this.displayPatterns = !var1.isEmpty() && !var2.isEmpty() && var3.isEmpty() && !this.hasMaxPatterns;
            this.displaySpecialPattern = !this.hasMaxPatterns && !var3.isEmpty() && !var1.isEmpty() && !var2.isEmpty();
        }

        this.bannerStack = var1.copy();
        this.dyeStack = var2.copy();
        this.patternStack = var3.copy();
    }
}
