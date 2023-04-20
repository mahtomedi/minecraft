package net.minecraft.client.gui.screens.packs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.PackEntry> {
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
    static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
    private final Component title;
    final PackSelectionScreen screen;

    public TransferableSelectionList(Minecraft param0, PackSelectionScreen param1, int param2, int param3, Component param4) {
        super(param0, param2, param3, 32, param3 - 55 + 4, 36);
        this.screen = param1;
        this.title = param4;
        this.centerListVertically = false;
        this.setRenderHeader(true, (int)(9.0F * 1.5F));
    }

    @Override
    protected void renderHeader(GuiGraphics param0, int param1, int param2) {
        Component var0 = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        param0.drawString(
            this.minecraft.font, var0, param1 + this.width / 2 - this.minecraft.font.width(var0) / 2, Math.min(this.y0 + 3, param2), 16777215, false
        );
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.getSelected() != null) {
            switch(param0) {
                case 32:
                case 257:
                    this.getSelected().keyboardSelection();
                    return true;
                default:
                    if (Screen.hasShiftDown()) {
                        switch(param0) {
                            case 264:
                                this.getSelected().keyboardMoveDown();
                                return true;
                            case 265:
                                this.getSelected().keyboardMoveUp();
                                return true;
                        }
                    }
            }
        }

        return super.keyPressed(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public static class PackEntry extends ObjectSelectionList.Entry<TransferableSelectionList.PackEntry> {
        private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
        private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
        private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
        private static final int ICON_OVERLAY_X_MOVE_UP = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
        private static final int MAX_NAME_WIDTH_PIXELS = 157;
        private static final String TOO_LONG_NAME_SUFFIX = "...";
        private final TransferableSelectionList parent;
        protected final Minecraft minecraft;
        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private final FormattedCharSequence incompatibleNameDisplayCache;
        private final MultiLineLabel incompatibleDescriptionDisplayCache;

        public PackEntry(Minecraft param0, TransferableSelectionList param1, PackSelectionModel.Entry param2) {
            this.minecraft = param0;
            this.pack = param2;
            this.parent = param1;
            this.nameDisplayCache = cacheName(param0, param2.getTitle());
            this.descriptionDisplayCache = cacheDescription(param0, param2.getExtendedDescription());
            this.incompatibleNameDisplayCache = cacheName(param0, TransferableSelectionList.INCOMPATIBLE_TITLE);
            this.incompatibleDescriptionDisplayCache = cacheDescription(param0, param2.getCompatibility().getDescription());
        }

        private static FormattedCharSequence cacheName(Minecraft param0, Component param1) {
            int var0 = param0.font.width(param1);
            if (var0 > 157) {
                FormattedText var1 = FormattedText.composite(param0.font.substrByWidth(param1, 157 - param0.font.width("...")), FormattedText.of("..."));
                return Language.getInstance().getVisualOrder(var1);
            } else {
                return param1.getVisualOrderText();
            }
        }

        private static MultiLineLabel cacheDescription(Minecraft param0, Component param1) {
            return MultiLineLabel.create(param0.font, param1, 157, 2);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.pack.getTitle());
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            PackCompatibility var0 = this.pack.getCompatibility();
            if (!var0.isCompatible()) {
                param0.fill(param3 - 1, param2 - 1, param3 + param4 - 9, param2 + param5 + 1, -8978432);
            }

            param0.blit(this.pack.getIconTexture(), param3, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            FormattedCharSequence var1 = this.nameDisplayCache;
            MultiLineLabel var2 = this.descriptionDisplayCache;
            if (this.showHoverOverlay()
                && (this.minecraft.options.touchscreen().get() || param8 || this.parent.getSelected() == this && this.parent.isFocused())) {
                param0.fill(param3, param2, param3 + 32, param2 + 32, -1601138544);
                int var3 = param6 - param3;
                int var4 = param7 - param2;
                if (!this.pack.getCompatibility().isCompatible()) {
                    var1 = this.incompatibleNameDisplayCache;
                    var2 = this.incompatibleDescriptionDisplayCache;
                }

                if (this.pack.canSelect()) {
                    if (var3 < 32) {
                        param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                } else {
                    if (this.pack.canUnselect()) {
                        if (var3 < 16) {
                            param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 32.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 32.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveUp()) {
                        if (var3 < 32 && var3 > 16 && var4 < 16) {
                            param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveDown()) {
                        if (var3 < 32 && var3 > 16 && var4 > 16) {
                            param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 64.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            param0.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 64.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
                }
            }

            param0.drawString(this.minecraft.font, var1, param3 + 32 + 2, param2 + 1, 16777215);
            var2.renderLeftAligned(param0, param3 + 32 + 2, param2 + 12, 10, 8421504);
        }

        public String getPackId() {
            return this.pack.getId();
        }

        private boolean showHoverOverlay() {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        public void keyboardSelection() {
            if (this.pack.canSelect() && this.handlePackSelection()) {
                this.parent.screen.updateFocus(this.parent);
            } else if (this.pack.canUnselect()) {
                this.pack.unselect();
                this.parent.screen.updateFocus(this.parent);
            }

        }

        void keyboardMoveUp() {
            if (this.pack.canMoveUp()) {
                this.pack.moveUp();
            }

        }

        void keyboardMoveDown() {
            if (this.pack.canMoveDown()) {
                this.pack.moveDown();
            }

        }

        private boolean handlePackSelection() {
            if (this.pack.getCompatibility().isCompatible()) {
                this.pack.select();
                return true;
            } else {
                Component var0 = this.pack.getCompatibility().getConfirmation();
                this.minecraft.setScreen(new ConfirmScreen(param0 -> {
                    this.minecraft.setScreen(this.parent.screen);
                    if (param0) {
                        this.pack.select();
                    }

                }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, var0));
                return false;
            }
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 != 0) {
                return false;
            } else {
                double var0 = param0 - (double)this.parent.getRowLeft();
                double var1 = param1 - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
                if (this.showHoverOverlay() && var0 <= 32.0) {
                    this.parent.screen.clearSelected();
                    if (this.pack.canSelect()) {
                        this.handlePackSelection();
                        return true;
                    }

                    if (var0 < 16.0 && this.pack.canUnselect()) {
                        this.pack.unselect();
                        return true;
                    }

                    if (var0 > 16.0 && var1 < 16.0 && this.pack.canMoveUp()) {
                        this.pack.moveUp();
                        return true;
                    }

                    if (var0 > 16.0 && var1 > 16.0 && this.pack.canMoveDown()) {
                        this.pack.moveDown();
                        return true;
                    }
                }

                return false;
            }
        }
    }
}
