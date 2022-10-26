package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    private int totalRowCount() {
        return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), 4);
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        this.renderBackground(param0);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
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
            param0.translate((float)(var0 + 139), (float)(var1 + 52), 0.0F);
            param0.scale(24.0F, -24.0F, 1.0F);
            param0.translate(0.5F, 0.5F, 0.5F);
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
            List<Holder<BannerPattern>> var11 = this.menu.getSelectablePatterns();

            label63:
            for(int var12 = 0; var12 < 4; ++var12) {
                for(int var13 = 0; var13 < 4; ++var13) {
                    int var14 = var12 + this.startRow;
                    int var15 = var14 * 4 + var13;
                    if (var15 >= var11.size()) {
                        break label63;
                    }

                    RenderSystem.setShaderTexture(0, BG_LOCATION);
                    int var16 = var9 + var13 * 14;
                    int var17 = var10 + var12 * 14;
                    boolean var18 = param2 >= var16 && param3 >= var17 && param2 < var16 + 14 && param3 < var17 + 14;
                    int var19;
                    if (var15 == this.menu.getSelectedBannerPatternIndex()) {
                        var19 = this.imageHeight + 14;
                    } else if (var18) {
                        var19 = this.imageHeight + 28;
                    } else {
                        var19 = this.imageHeight;
                    }

                    this.blit(param0, var16, var17, 0, var19, 14, 14);
                    this.renderPattern(var11.get(var15), var16, var17);
                }
            }
        }

        Lighting.setupFor3DItems();
    }

    private void renderPattern(Holder<BannerPattern> param0, int param1, int param2) {
        CompoundTag var0 = new CompoundTag();
        ListTag var1 = new BannerPattern.Builder().addPattern(BannerPatterns.BASE, DyeColor.GRAY).addPattern(param0, DyeColor.WHITE).toListTag();
        var0.put("Patterns", var1);
        ItemStack var2 = new ItemStack(Items.GRAY_BANNER);
        BlockItem.setBlockEntityData(var2, BlockEntityType.BANNER, var0);
        PoseStack var3 = new PoseStack();
        var3.pushPose();
        var3.translate((float)param1 + 0.5F, (float)(param2 + 16), 0.0F);
        var3.scale(6.0F, -6.0F, 1.0F);
        var3.translate(0.5F, 0.5F, 0.0F);
        var3.translate(0.5F, 0.5F, 0.5F);
        float var4 = 0.6666667F;
        var3.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource var5 = this.minecraft.renderBuffers().bufferSource();
        this.flag.xRot = 0.0F;
        this.flag.y = -32.0F;
        List<Pair<Holder<BannerPattern>, DyeColor>> var6 = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(var2));
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
    public boolean mouseScrolled(double param0, double param1, double param2) {
        int var0 = this.totalRowCount() - 4;
        if (this.displayPatterns && var0 > 0) {
            float var1 = (float)param2 / (float)var0;
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
