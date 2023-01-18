package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {
    public static final ResourceLocation INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/inventory.png");
    private static final float SNAPBACK_SPEED = 100.0F;
    private static final int QUICKDROP_DELAY = 500;
    public static final int SLOT_ITEM_BLIT_OFFSET = 100;
    private static final int HOVER_ITEM_BLIT_OFFSET = 200;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int titleLabelX;
    protected int titleLabelY;
    protected int inventoryLabelX;
    protected int inventoryLabelY;
    protected final T menu;
    protected final Component playerInventoryTitle;
    @Nullable
    protected Slot hoveredSlot;
    @Nullable
    private Slot clickedSlot;
    @Nullable
    private Slot snapbackEnd;
    @Nullable
    private Slot quickdropSlot;
    @Nullable
    private Slot lastClickSlot;
    protected int leftPos;
    protected int topPos;
    private boolean isSplittingStack;
    private ItemStack draggingItem = ItemStack.EMPTY;
    private int snapbackStartX;
    private int snapbackStartY;
    private long snapbackTime;
    private ItemStack snapbackItem = ItemStack.EMPTY;
    private long quickdropTime;
    protected final Set<Slot> quickCraftSlots = Sets.newHashSet();
    protected boolean isQuickCrafting;
    private int quickCraftingType;
    private int quickCraftingButton;
    private boolean skipNextRelease;
    private int quickCraftingRemainder;
    private long lastClickTime;
    private int lastClickButton;
    private boolean doubleclick;
    private ItemStack lastQuickMoved = ItemStack.EMPTY;

    public AbstractContainerScreen(T param0, Inventory param1, Component param2) {
        super(param2);
        this.menu = param0;
        this.playerInventoryTitle = param1.getDisplayName();
        this.skipNextRelease = true;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.renderBg(param0, param3, param1, param2);
        RenderSystem.disableDepthTest();
        super.render(param0, param1, param2, param3);
        PoseStack var2 = RenderSystem.getModelViewStack();
        var2.pushPose();
        var2.translate((float)var0, (float)var1, 0.0F);
        RenderSystem.applyModelViewMatrix();
        this.hoveredSlot = null;

        for(int var3 = 0; var3 < this.menu.slots.size(); ++var3) {
            Slot var4 = this.menu.slots.get(var3);
            if (var4.isActive()) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderSlot(param0, var4);
            }

            if (this.isHovering(var4, (double)param1, (double)param2) && var4.isActive()) {
                this.hoveredSlot = var4;
                int var5 = var4.x;
                int var6 = var4.y;
                renderSlotHighlight(param0, var5, var6, this.getBlitOffset());
            }
        }

        this.renderLabels(param0, param1, param2);
        ItemStack var7 = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (!var7.isEmpty()) {
            int var8 = 8;
            int var9 = this.draggingItem.isEmpty() ? 8 : 16;
            String var10 = null;
            if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
                var7 = var7.copy();
                var7.setCount(Mth.ceil((float)var7.getCount() / 2.0F));
            } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
                var7 = var7.copy();
                var7.setCount(this.quickCraftingRemainder);
                if (var7.isEmpty()) {
                    var10 = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(var7, param1 - var0 - 8, param2 - var1 - var9, var10);
        }

        if (!this.snapbackItem.isEmpty()) {
            float var11 = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
            if (var11 >= 1.0F) {
                var11 = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int var12 = this.snapbackEnd.x - this.snapbackStartX;
            int var13 = this.snapbackEnd.y - this.snapbackStartY;
            int var14 = this.snapbackStartX + (int)((float)var12 * var11);
            int var15 = this.snapbackStartY + (int)((float)var13 * var11);
            this.renderFloatingItem(this.snapbackItem, var14, var15, null);
        }

        var2.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    public static void renderSlotHighlight(PoseStack param0, int param1, int param2, int param3) {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(param0, param1, param2, param1 + 16, param2 + 16, -2130706433, -2130706433, param3);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    protected void renderTooltip(PoseStack param0, int param1, int param2) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            this.renderTooltip(param0, this.hoveredSlot.getItem(), param1, param2);
        }

    }

    private void renderFloatingItem(ItemStack param0, int param1, int param2, String param3) {
        PoseStack var0 = RenderSystem.getModelViewStack();
        var0.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.applyModelViewMatrix();
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;
        this.itemRenderer.renderAndDecorateItem(param0, param1, param2);
        this.itemRenderer.renderGuiItemDecorations(this.font, param0, param1, param2 - (this.draggingItem.isEmpty() ? 0 : 8), param3);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
    }

    protected void renderLabels(PoseStack param0, int param1, int param2) {
        this.font.draw(param0, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(param0, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    protected abstract void renderBg(PoseStack var1, float var2, int var3, int var4);

    private void renderSlot(PoseStack param0, Slot param1) {
        int var0 = param1.x;
        int var1 = param1.y;
        ItemStack var2 = param1.getItem();
        boolean var3 = false;
        boolean var4 = param1 == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack var5 = this.menu.getCarried();
        String var6 = null;
        if (param1 == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !var2.isEmpty()) {
            var2 = var2.copy();
            var2.setCount(var2.getCount() / 2);
        } else if (this.isQuickCrafting && this.quickCraftSlots.contains(param1) && !var5.isEmpty()) {
            if (this.quickCraftSlots.size() == 1) {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(param1, var5, true) && this.menu.canDragTo(param1)) {
                var2 = var5.copy();
                var3 = true;
                AbstractContainerMenu.getQuickCraftSlotCount(
                    this.quickCraftSlots, this.quickCraftingType, var2, param1.getItem().isEmpty() ? 0 : param1.getItem().getCount()
                );
                int var7 = Math.min(var2.getMaxStackSize(), param1.getMaxStackSize(var2));
                if (var2.getCount() > var7) {
                    var6 = ChatFormatting.YELLOW.toString() + var7;
                    var2.setCount(var7);
                }
            } else {
                this.quickCraftSlots.remove(param1);
                this.recalculateQuickCraftRemaining();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;
        if (var2.isEmpty() && param1.isActive()) {
            Pair<ResourceLocation, ResourceLocation> var8 = param1.getNoItemIcon();
            if (var8 != null) {
                TextureAtlasSprite var9 = this.minecraft.getTextureAtlas(var8.getFirst()).apply(var8.getSecond());
                RenderSystem.setShaderTexture(0, var9.atlasLocation());
                blit(param0, var0, var1, this.getBlitOffset(), 16, 16, var9);
                var4 = true;
            }
        }

        if (!var4) {
            if (var3) {
                fill(param0, var0, var1, var0 + 16, var1 + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, var2, var0, var1, param1.x + param1.y * this.imageWidth);
            this.itemRenderer.renderGuiItemDecorations(this.font, var2, var0, var1, var6);
        }

        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    private void recalculateQuickCraftRemaining() {
        ItemStack var0 = this.menu.getCarried();
        if (!var0.isEmpty() && this.isQuickCrafting) {
            if (this.quickCraftingType == 2) {
                this.quickCraftingRemainder = var0.getMaxStackSize();
            } else {
                this.quickCraftingRemainder = var0.getCount();

                for(Slot var1 : this.quickCraftSlots) {
                    ItemStack var2 = var0.copy();
                    ItemStack var3 = var1.getItem();
                    int var4 = var3.isEmpty() ? 0 : var3.getCount();
                    AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, var2, var4);
                    int var5 = Math.min(var2.getMaxStackSize(), var1.getMaxStackSize(var2));
                    if (var2.getCount() > var5) {
                        var2.setCount(var5);
                    }

                    this.quickCraftingRemainder -= var2.getCount() - var4;
                }

            }
        }
    }

    @Nullable
    private Slot findSlot(double param0, double param1) {
        for(int var0 = 0; var0 < this.menu.slots.size(); ++var0) {
            Slot var1 = this.menu.slots.get(var0);
            if (this.isHovering(var1, param0, param1) && var1.isActive()) {
                return var1;
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (super.mouseClicked(param0, param1, param2)) {
            return true;
        } else {
            boolean var0 = this.minecraft.options.keyPickItem.matchesMouse(param2) && this.minecraft.gameMode.hasInfiniteItems();
            Slot var1 = this.findSlot(param0, param1);
            long var2 = Util.getMillis();
            this.doubleclick = this.lastClickSlot == var1 && var2 - this.lastClickTime < 250L && this.lastClickButton == param2;
            this.skipNextRelease = false;
            if (param2 != 0 && param2 != 1 && !var0) {
                this.checkHotbarMouseClicked(param2);
            } else {
                int var3 = this.leftPos;
                int var4 = this.topPos;
                boolean var5 = this.hasClickedOutside(param0, param1, var3, var4, param2);
                int var6 = -1;
                if (var1 != null) {
                    var6 = var1.index;
                }

                if (var5) {
                    var6 = -999;
                }

                if (this.minecraft.options.touchscreen().get() && var5 && this.menu.getCarried().isEmpty()) {
                    this.onClose();
                    return true;
                }

                if (var6 != -1) {
                    if (this.minecraft.options.touchscreen().get()) {
                        if (var1 != null && var1.hasItem()) {
                            this.clickedSlot = var1;
                            this.draggingItem = ItemStack.EMPTY;
                            this.isSplittingStack = param2 == 1;
                        } else {
                            this.clickedSlot = null;
                        }
                    } else if (!this.isQuickCrafting) {
                        if (this.menu.getCarried().isEmpty()) {
                            if (var0) {
                                this.slotClicked(var1, var6, param2, ClickType.CLONE);
                            } else {
                                boolean var7 = var6 != -999
                                    && (
                                        InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
                                            || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344)
                                    );
                                ClickType var8 = ClickType.PICKUP;
                                if (var7) {
                                    this.lastQuickMoved = var1 != null && var1.hasItem() ? var1.getItem().copy() : ItemStack.EMPTY;
                                    var8 = ClickType.QUICK_MOVE;
                                } else if (var6 == -999) {
                                    var8 = ClickType.THROW;
                                }

                                this.slotClicked(var1, var6, param2, var8);
                            }

                            this.skipNextRelease = true;
                        } else {
                            this.isQuickCrafting = true;
                            this.quickCraftingButton = param2;
                            this.quickCraftSlots.clear();
                            if (param2 == 0) {
                                this.quickCraftingType = 0;
                            } else if (param2 == 1) {
                                this.quickCraftingType = 1;
                            } else if (var0) {
                                this.quickCraftingType = 2;
                            }
                        }
                    }
                }
            }

            this.lastClickSlot = var1;
            this.lastClickTime = var2;
            this.lastClickButton = param2;
            return true;
        }
    }

    private void checkHotbarMouseClicked(int param0) {
        if (this.hoveredSlot != null && this.menu.getCarried().isEmpty()) {
            if (this.minecraft.options.keySwapOffhand.matchesMouse(param0)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return;
            }

            for(int var0 = 0; var0 < 9; ++var0) {
                if (this.minecraft.options.keyHotbarSlots[var0].matchesMouse(param0)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, var0, ClickType.SWAP);
                }
            }
        }

    }

    protected boolean hasClickedOutside(double param0, double param1, int param2, int param3, int param4) {
        return param0 < (double)param2
            || param1 < (double)param3
            || param0 >= (double)(param2 + this.imageWidth)
            || param1 >= (double)(param3 + this.imageHeight);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        Slot var0 = this.findSlot(param0, param1);
        ItemStack var1 = this.menu.getCarried();
        if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
            if (param2 == 0 || param2 == 1) {
                if (this.draggingItem.isEmpty()) {
                    if (var0 != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                } else if (this.draggingItem.getCount() > 1 && var0 != null && AbstractContainerMenu.canItemQuickReplace(var0, this.draggingItem, false)) {
                    long var2 = Util.getMillis();
                    if (this.quickdropSlot == var0) {
                        if (var2 - this.quickdropTime > 500L) {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.slotClicked(var0, var0.index, 1, ClickType.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.quickdropTime = var2 + 750L;
                            this.draggingItem.shrink(1);
                        }
                    } else {
                        this.quickdropSlot = var0;
                        this.quickdropTime = var2;
                    }
                }
            }
        } else if (this.isQuickCrafting
            && var0 != null
            && !var1.isEmpty()
            && (var1.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2)
            && AbstractContainerMenu.canItemQuickReplace(var0, var1, true)
            && var0.mayPlace(var1)
            && this.menu.canDragTo(var0)) {
            this.quickCraftSlots.add(var0);
            this.recalculateQuickCraftRemaining();
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        Slot var0 = this.findSlot(param0, param1);
        int var1 = this.leftPos;
        int var2 = this.topPos;
        boolean var3 = this.hasClickedOutside(param0, param1, var1, var2, param2);
        int var4 = -1;
        if (var0 != null) {
            var4 = var0.index;
        }

        if (var3) {
            var4 = -999;
        }

        if (this.doubleclick && var0 != null && param2 == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, var0)) {
            if (hasShiftDown()) {
                if (!this.lastQuickMoved.isEmpty()) {
                    for(Slot var5 : this.menu.slots) {
                        if (var5 != null
                            && var5.mayPickup(this.minecraft.player)
                            && var5.hasItem()
                            && var5.container == var0.container
                            && AbstractContainerMenu.canItemQuickReplace(var5, this.lastQuickMoved, true)) {
                            this.slotClicked(var5, var5.index, param2, ClickType.QUICK_MOVE);
                        }
                    }
                }
            } else {
                this.slotClicked(var0, var4, param2, ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
            this.lastClickTime = 0L;
        } else {
            if (this.isQuickCrafting && this.quickCraftingButton != param2) {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease) {
                this.skipNextRelease = false;
                return true;
            }

            if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
                if (param2 == 0 || param2 == 1) {
                    if (this.draggingItem.isEmpty() && var0 != this.clickedSlot) {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    boolean var6 = AbstractContainerMenu.canItemQuickReplace(var0, this.draggingItem, false);
                    if (var4 != -1 && !this.draggingItem.isEmpty() && var6) {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, param2, ClickType.PICKUP);
                        this.slotClicked(var0, var4, 0, ClickType.PICKUP);
                        if (this.menu.getCarried().isEmpty()) {
                            this.snapbackItem = ItemStack.EMPTY;
                        } else {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, param2, ClickType.PICKUP);
                            this.snapbackStartX = Mth.floor(param0 - (double)var1);
                            this.snapbackStartY = Mth.floor(param1 - (double)var2);
                            this.snapbackEnd = this.clickedSlot;
                            this.snapbackItem = this.draggingItem;
                            this.snapbackTime = Util.getMillis();
                        }
                    } else if (!this.draggingItem.isEmpty()) {
                        this.snapbackStartX = Mth.floor(param0 - (double)var1);
                        this.snapbackStartY = Mth.floor(param1 - (double)var2);
                        this.snapbackEnd = this.clickedSlot;
                        this.snapbackItem = this.draggingItem;
                        this.snapbackTime = Util.getMillis();
                    }

                    this.clearDraggingState();
                }
            } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

                for(Slot var7 : this.quickCraftSlots) {
                    this.slotClicked(var7, var7.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            } else if (!this.menu.getCarried().isEmpty()) {
                if (this.minecraft.options.keyPickItem.matchesMouse(param2)) {
                    this.slotClicked(var0, var4, param2, ClickType.CLONE);
                } else {
                    boolean var8 = var4 != -999
                        && (
                            InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
                                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344)
                        );
                    if (var8) {
                        this.lastQuickMoved = var0 != null && var0.hasItem() ? var0.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked(var0, var4, param2, var8 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }

        if (this.menu.getCarried().isEmpty()) {
            this.lastClickTime = 0L;
        }

        this.isQuickCrafting = false;
        return true;
    }

    public void clearDraggingState() {
        this.draggingItem = ItemStack.EMPTY;
        this.clickedSlot = null;
    }

    private boolean isHovering(Slot param0, double param1, double param2) {
        return this.isHovering(param0.x, param0.y, 16, 16, param1, param2);
    }

    protected boolean isHovering(int param0, int param1, int param2, int param3, double param4, double param5) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        param4 -= (double)var0;
        param5 -= (double)var1;
        return param4 >= (double)(param0 - 1)
            && param4 < (double)(param0 + param2 + 1)
            && param5 >= (double)(param1 - 1)
            && param5 < (double)(param1 + param3 + 1);
    }

    protected void slotClicked(Slot param0, int param1, int param2, ClickType param3) {
        if (param0 != null) {
            param1 = param0.index;
        }

        this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, param1, param2, param3, this.minecraft.player);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(param0, param1)) {
            this.onClose();
            return true;
        } else {
            this.checkHotbarKeyPressed(param0, param1);
            if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
                if (this.minecraft.options.keyPickItem.matches(param0, param1)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
                } else if (this.minecraft.options.keyDrop.matches(param0, param1)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
                }
            }

            return true;
        }
    }

    protected boolean checkHotbarKeyPressed(int param0, int param1) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
            if (this.minecraft.options.keySwapOffhand.matches(param0, param1)) {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return true;
            }

            for(int var0 = 0; var0 < 9; ++var0) {
                if (this.minecraft.options.keyHotbarSlots[var0].matches(param0, param1)) {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, var0, ClickType.SWAP);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void removed() {
        if (this.minecraft.player != null) {
            this.menu.removed(this.minecraft.player);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public final void tick() {
        super.tick();
        if (this.minecraft.player.isAlive() && !this.minecraft.player.isRemoved()) {
            this.containerTick();
        } else {
            this.minecraft.player.closeContainer();
        }

    }

    protected void containerTick() {
    }

    @Override
    public T getMenu() {
        return this.menu;
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }
}
