package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.PackEntry> {
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("pack.incompatible");
    private static final Component INCOMPATIBLE_CONFIRM_TITLE = new TranslatableComponent("pack.incompatible.confirm.title");
    private final Component title;

    public TransferableSelectionList(Minecraft param0, int param1, int param2, Component param3) {
        super(param0, param1, param2, 32, param2 - 55 + 4, 36);
        this.title = param3;
        this.centerListVertically = false;
        this.setRenderHeader(true, (int)(9.0F * 1.5F));
    }

    @Override
    protected void renderHeader(PoseStack param0, int param1, int param2, Tesselator param3) {
        Component var0 = new TextComponent("").append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft
            .font
            .draw(param0, var0, (float)(param1 + this.width / 2 - this.minecraft.font.width(var0) / 2), (float)Math.min(this.y0 + 3, param2), 16777215);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @OnlyIn(Dist.CLIENT)
    public static class PackEntry extends ObjectSelectionList.Entry<TransferableSelectionList.PackEntry> {
        private TransferableSelectionList parent;
        protected final Minecraft minecraft;
        protected final Screen screen;
        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;

        public PackEntry(Minecraft param0, TransferableSelectionList param1, Screen param2, PackSelectionModel.Entry param3) {
            this.minecraft = param0;
            this.screen = param2;
            this.pack = param3;
            this.parent = param1;
            Component var0;
            Component var1;
            if (param3.getCompatibility().isCompatible()) {
                var0 = param3.getTitle();
                var1 = param3.getExtendedDescription();
            } else {
                var0 = TransferableSelectionList.INCOMPATIBLE_TITLE;
                var1 = param3.getCompatibility().getDescription();
            }

            int var4 = param0.font.width(var0);
            if (var4 > 157) {
                FormattedText var5 = FormattedText.composite(param0.font.substrByWidth(var0, 157 - param0.font.width("...")), FormattedText.of("..."));
                this.nameDisplayCache = Language.getInstance().getVisualOrder(var5);
            } else {
                this.nameDisplayCache = var0.getVisualOrderText();
            }

            this.descriptionDisplayCache = MultiLineLabel.create(param0.font, var1, 157, 2);
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            PackCompatibility var0 = this.pack.getCompatibility();
            if (!var0.isCompatible()) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.fill(param0, param3 - 1, param2 - 1, param3 + param4 - 9, param2 + param5 + 1, -8978432);
            }

            this.minecraft.getTextureManager().bind(this.pack.getIconTexture());
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param0, param3, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || param8)) {
                this.minecraft.getTextureManager().bind(TransferableSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param0, param3, param2, param3 + 32, param2 + 32, -1601138544);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int var1 = param6 - param3;
                int var2 = param7 - param2;
                if (this.pack.canSelect()) {
                    if (var1 < 32) {
                        GuiComponent.blit(param0, param3, param2, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param0, param3, param2, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                } else {
                    if (this.pack.canUnselect()) {
                        if (var1 < 16) {
                            GuiComponent.blit(param0, param3, param2, 32.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(param0, param3, param2, 32.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveUp()) {
                        if (var1 < 32 && var1 > 16 && var2 < 16) {
                            GuiComponent.blit(param0, param3, param2, 96.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(param0, param3, param2, 96.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveDown()) {
                        if (var1 < 32 && var1 > 16 && var2 > 16) {
                            GuiComponent.blit(param0, param3, param2, 64.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(param0, param3, param2, 64.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
                }
            }

            this.minecraft.font.drawShadow(param0, this.nameDisplayCache, (float)(param3 + 32 + 2), (float)(param2 + 1), 16777215);
            this.descriptionDisplayCache.renderLeftAligned(param0, param3 + 32 + 2, param2 + 12, 10, 8421504);
        }

        private boolean showHoverOverlay() {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            double var0 = param0 - (double)this.parent.getRowLeft();
            double var1 = param1 - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && var0 <= 32.0) {
                if (this.pack.canSelect()) {
                    PackCompatibility var2 = this.pack.getCompatibility();
                    if (var2.isCompatible()) {
                        this.pack.select();
                    } else {
                        Component var3 = var2.getConfirmation();
                        this.minecraft.setScreen(new ConfirmScreen(param0x -> {
                            this.minecraft.setScreen(this.screen);
                            if (param0x) {
                                this.pack.select();
                            }

                        }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, var3));
                    }

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
