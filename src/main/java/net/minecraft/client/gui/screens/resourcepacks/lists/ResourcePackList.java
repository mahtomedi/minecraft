package net.minecraft.client.gui.screens.resourcepacks.lists;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.resourcepacks.ResourcePackSelectScreen;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ResourcePackList extends ObjectSelectionList<ResourcePackList.ResourcePackEntry> {
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    private static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("resourcePack.incompatible");
    private static final Component INCOMPATIBLE_CONFIRM_TITLE = new TranslatableComponent("resourcePack.incompatible.confirm.title");
    protected final Minecraft minecraft;
    private final Component title;

    public ResourcePackList(Minecraft param0, int param1, int param2, Component param3) {
        super(param0, param1, param2, 32, param2 - 55 + 4, 36);
        this.minecraft = param0;
        this.centerListVertically = false;
        this.setRenderHeader(true, (int)(9.0F * 1.5F));
        this.title = param3;
    }

    @Override
    protected void renderHeader(int param0, int param1, Tesselator param2) {
        Component var0 = new TextComponent("").append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft
            .font
            .draw(
                var0.getColoredString(),
                (float)(param0 + this.width / 2 - this.minecraft.font.width(var0.getColoredString()) / 2),
                (float)Math.min(this.y0 + 3, param1),
                16777215
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

    public void addResourcePackEntry(ResourcePackList.ResourcePackEntry param0) {
        this.addEntry(param0);
        param0.parent = this;
    }

    @OnlyIn(Dist.CLIENT)
    public static class ResourcePackEntry extends ObjectSelectionList.Entry<ResourcePackList.ResourcePackEntry> {
        private ResourcePackList parent;
        protected final Minecraft minecraft;
        protected final ResourcePackSelectScreen screen;
        private final UnopenedResourcePack resourcePack;

        public ResourcePackEntry(ResourcePackList param0, ResourcePackSelectScreen param1, UnopenedResourcePack param2) {
            this.screen = param1;
            this.minecraft = Minecraft.getInstance();
            this.resourcePack = param2;
            this.parent = param0;
        }

        public void addToList(SelectedResourcePackList param0) {
            this.getResourcePack().getDefaultPosition().insert(param0.children(), this, ResourcePackList.ResourcePackEntry::getResourcePack, true);
            this.parent = param0;
        }

        protected void bindToIcon() {
            this.resourcePack.bindIcon(this.minecraft.getTextureManager());
        }

        protected PackCompatibility getCompatibility() {
            return this.resourcePack.getCompatibility();
        }

        protected String getDescription() {
            return this.resourcePack.getDescription().getColoredString();
        }

        protected String getName() {
            return this.resourcePack.getTitle().getColoredString();
        }

        public UnopenedResourcePack getResourcePack() {
            return this.resourcePack;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            PackCompatibility var0 = this.getCompatibility();
            if (!var0.isCompatible()) {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.fill(param2 - 1, param1 - 1, param2 + param3 - 9, param1 + param4 + 1, -8978432);
            }

            this.bindToIcon();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(param2, param1, 0.0F, 0.0F, 32, 32, 32, 32);
            String var1 = this.getName();
            String var2 = this.getDescription();
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || param7)) {
                this.minecraft.getTextureManager().bind(ResourcePackList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param2, param1, param2 + 32, param1 + 32, -1601138544);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int var3 = param5 - param2;
                int var4 = param6 - param1;
                if (!var0.isCompatible()) {
                    var1 = ResourcePackList.INCOMPATIBLE_TITLE.getColoredString();
                    var2 = var0.getDescription().getColoredString();
                }

                if (this.canMoveRight()) {
                    if (var3 < 32) {
                        GuiComponent.blit(param2, param1, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(param2, param1, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                } else {
                    if (this.canMoveLeft()) {
                        if (var3 < 16) {
                            GuiComponent.blit(param2, param1, 32.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(param2, param1, 32.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.canMoveUp()) {
                        if (var3 < 32 && var3 > 16 && var4 < 16) {
                            GuiComponent.blit(param2, param1, 96.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(param2, param1, 96.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.canMoveDown()) {
                        if (var3 < 32 && var3 > 16 && var4 > 16) {
                            GuiComponent.blit(param2, param1, 64.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(param2, param1, 64.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
                }
            }

            int var5 = this.minecraft.font.width(var1);
            if (var5 > 157) {
                var1 = this.minecraft.font.substrByWidth(var1, 157 - this.minecraft.font.width("...")) + "...";
            }

            this.minecraft.font.drawShadow(var1, (float)(param2 + 32 + 2), (float)(param1 + 1), 16777215);
            List<String> var6 = this.minecraft.font.split(var2, 157);

            for(int var7 = 0; var7 < 2 && var7 < var6.size(); ++var7) {
                this.minecraft.font.drawShadow(var6.get(var7), (float)(param2 + 32 + 2), (float)(param1 + 12 + 10 * var7), 8421504);
            }

        }

        protected boolean showHoverOverlay() {
            return !this.resourcePack.isFixedPosition() || !this.resourcePack.isRequired();
        }

        protected boolean canMoveRight() {
            return !this.screen.isSelected(this);
        }

        protected boolean canMoveLeft() {
            return this.screen.isSelected(this) && !this.resourcePack.isRequired();
        }

        protected boolean canMoveUp() {
            List<ResourcePackList.ResourcePackEntry> var0 = this.parent.children();
            int var1 = var0.indexOf(this);
            return var1 > 0 && !var0.get(var1 - 1).resourcePack.isFixedPosition();
        }

        protected boolean canMoveDown() {
            List<ResourcePackList.ResourcePackEntry> var0 = this.parent.children();
            int var1 = var0.indexOf(this);
            return var1 >= 0 && var1 < var0.size() - 1 && !var0.get(var1 + 1).resourcePack.isFixedPosition();
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            double var0 = param0 - (double)this.parent.getRowLeft();
            double var1 = param1 - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && var0 <= 32.0) {
                if (this.canMoveRight()) {
                    this.getScreen().setChanged();
                    PackCompatibility var2 = this.getCompatibility();
                    if (var2.isCompatible()) {
                        this.getScreen().select(this);
                    } else {
                        Component var3 = var2.getConfirmation();
                        this.minecraft.setScreen(new ConfirmScreen(param0x -> {
                            this.minecraft.setScreen(this.getScreen());
                            if (param0x) {
                                this.getScreen().select(this);
                            }

                        }, ResourcePackList.INCOMPATIBLE_CONFIRM_TITLE, var3));
                    }

                    return true;
                }

                if (var0 < 16.0 && this.canMoveLeft()) {
                    this.getScreen().deselect(this);
                    return true;
                }

                if (var0 > 16.0 && var1 < 16.0 && this.canMoveUp()) {
                    List<ResourcePackList.ResourcePackEntry> var4 = this.parent.children();
                    int var5 = var4.indexOf(this);
                    var4.remove(this);
                    var4.add(var5 - 1, this);
                    this.getScreen().setChanged();
                    return true;
                }

                if (var0 > 16.0 && var1 > 16.0 && this.canMoveDown()) {
                    List<ResourcePackList.ResourcePackEntry> var6 = this.parent.children();
                    int var7 = var6.indexOf(this);
                    var6.remove(this);
                    var6.add(var7 + 1, this);
                    this.getScreen().setChanged();
                    return true;
                }
            }

            return false;
        }

        public ResourcePackSelectScreen getScreen() {
            return this.screen;
        }
    }
}
