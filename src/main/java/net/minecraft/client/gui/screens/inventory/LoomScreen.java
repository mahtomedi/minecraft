package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LoomScreen extends AbstractContainerScreen<LoomMenu> {
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
    private static final int TOTAL_PATTERN_ROWS = (BannerPattern.COUNT - 5 - 1 + 4 - 1) / 4;
    private static final DyeColor PATTERN_BASE_COLOR = DyeColor.GRAY;
    private static final DyeColor PATTERN_OVERLAY_COLOR = DyeColor.WHITE;
    private static final List<DyeColor> PATTERN_COLORS = Lists.newArrayList(PATTERN_BASE_COLOR, PATTERN_OVERLAY_COLOR);
    private ResourceLocation resultBannerTexture;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private final ResourceLocation[] patternTextures = new ResourceLocation[BannerPattern.COUNT];
    private boolean displayPatterns;
    private boolean displaySpecialPattern;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex = 1;
    private int loadNextTextureIndex = 1;

    public LoomScreen(LoomMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        param0.registerUpdateListener(this::containerChanged);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.loadNextTextureIndex < BannerPattern.COUNT) {
            BannerPattern var0 = BannerPattern.values()[this.loadNextTextureIndex];
            String var1 = "b" + PATTERN_BASE_COLOR.getId();
            String var2 = var0.getHashname() + PATTERN_OVERLAY_COLOR.getId();
            this.patternTextures[this.loadNextTextureIndex] = BannerTextures.BANNER_CACHE
                .getTextureLocation(var1 + var2, Lists.newArrayList(BannerPattern.BASE, var0), PATTERN_COLORS);
            ++this.loadNextTextureIndex;
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        super.render(param0, param1, param2);
        this.renderTooltip(param0, param1);
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        this.font.draw(this.title.getColoredString(), 8.0F, 4.0F, 4210752);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        this.renderBackground();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BG_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        Slot var2 = this.menu.getBannerSlot();
        Slot var3 = this.menu.getDyeSlot();
        Slot var4 = this.menu.getPatternSlot();
        Slot var5 = this.menu.getResultSlot();
        if (!var2.hasItem()) {
            this.blit(var0 + var2.x, var1 + var2.y, this.imageWidth, 0, 16, 16);
        }

        if (!var3.hasItem()) {
            this.blit(var0 + var3.x, var1 + var3.y, this.imageWidth + 16, 0, 16, 16);
        }

        if (!var4.hasItem()) {
            this.blit(var0 + var4.x, var1 + var4.y, this.imageWidth + 32, 0, 16, 16);
        }

        int var6 = (int)(41.0F * this.scrollOffs);
        this.blit(var0 + 119, var1 + 13 + var6, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
        if (this.resultBannerTexture != null && !this.hasMaxPatterns) {
            this.minecraft.getTextureManager().bind(this.resultBannerTexture);
            blit(var0 + 141, var1 + 8, 20, 40, 1.0F, 1.0F, 20, 40, 64, 64);
        } else if (this.hasMaxPatterns) {
            this.blit(var0 + var5.x - 2, var1 + var5.y - 2, this.imageWidth, 17, 17, 16);
        }

        if (this.displayPatterns) {
            int var7 = var0 + 60;
            int var8 = var1 + 13;
            int var9 = this.startIndex + 16;

            for(int var10 = this.startIndex; var10 < var9 && var10 < this.patternTextures.length - 5; ++var10) {
                int var11 = var10 - this.startIndex;
                int var12 = var7 + var11 % 4 * 14;
                int var13 = var8 + var11 / 4 * 14;
                this.minecraft.getTextureManager().bind(BG_LOCATION);
                int var14 = this.imageHeight;
                if (var10 == this.menu.getSelectedBannerPatternIndex()) {
                    var14 += 14;
                } else if (param1 >= var12 && param2 >= var13 && param1 < var12 + 14 && param2 < var13 + 14) {
                    var14 += 28;
                }

                this.blit(var12, var13, 0, var14, 14, 14);
                if (this.patternTextures[var10] != null) {
                    this.minecraft.getTextureManager().bind(this.patternTextures[var10]);
                    blit(var12 + 4, var13 + 2, 5, 10, 1.0F, 1.0F, 20, 40, 64, 64);
                }
            }
        } else if (this.displaySpecialPattern) {
            int var15 = var0 + 60;
            int var16 = var1 + 13;
            this.minecraft.getTextureManager().bind(BG_LOCATION);
            this.blit(var15, var16, 0, this.imageHeight, 14, 14);
            int var17 = this.menu.getSelectedBannerPatternIndex();
            if (this.patternTextures[var17] != null) {
                this.minecraft.getTextureManager().bind(this.patternTextures[var17]);
                blit(var15 + 4, var16 + 2, 5, 10, 1.0F, 1.0F, 20, 40, 64, 64);
            }
        }

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
            this.resultBannerTexture = null;
        } else {
            BannerBlockEntity var1 = new BannerBlockEntity();
            var1.fromItem(var0, ((BannerItem)var0.getItem()).getColor());
            this.resultBannerTexture = BannerTextures.BANNER_CACHE.getTextureLocation(var1.getTextureHashName(), var1.getPatterns(), var1.getColors());
        }

        ItemStack var2 = this.menu.getBannerSlot().getItem();
        ItemStack var3 = this.menu.getDyeSlot().getItem();
        ItemStack var4 = this.menu.getPatternSlot().getItem();
        CompoundTag var5 = var2.getOrCreateTagElement("BlockEntityTag");
        this.hasMaxPatterns = var5.contains("Patterns", 9) && !var2.isEmpty() && var5.getList("Patterns", 10).size() >= 6;
        if (this.hasMaxPatterns) {
            this.resultBannerTexture = null;
        }

        if (!ItemStack.matches(var2, this.bannerStack) || !ItemStack.matches(var3, this.dyeStack) || !ItemStack.matches(var4, this.patternStack)) {
            this.displayPatterns = !var2.isEmpty() && !var3.isEmpty() && var4.isEmpty() && !this.hasMaxPatterns;
            this.displaySpecialPattern = !this.hasMaxPatterns && !var4.isEmpty() && !var2.isEmpty() && !var3.isEmpty();
        }

        this.bannerStack = var2.copy();
        this.dyeStack = var3.copy();
        this.patternStack = var4.copy();
    }
}
