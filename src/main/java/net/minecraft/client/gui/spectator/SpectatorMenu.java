package net.minecraft.client.gui.spectator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorMenu {
    private static final SpectatorMenuItem CLOSE_ITEM = new SpectatorMenu.CloseSpectatorItem();
    private static final SpectatorMenuItem SCROLL_LEFT = new SpectatorMenu.ScrollMenuItem(-1, true);
    private static final SpectatorMenuItem SCROLL_RIGHT_ENABLED = new SpectatorMenu.ScrollMenuItem(1, true);
    private static final SpectatorMenuItem SCROLL_RIGHT_DISABLED = new SpectatorMenu.ScrollMenuItem(1, false);
    public static final SpectatorMenuItem EMPTY_SLOT = new SpectatorMenuItem() {
        @Override
        public void selectItem(SpectatorMenu param0) {
        }

        @Override
        public Component getName() {
            return TextComponent.EMPTY;
        }

        @Override
        public void renderIcon(PoseStack param0, float param1, int param2) {
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
    private final SpectatorMenuListener listener;
    private SpectatorMenuCategory category;
    private int selectedSlot = -1;
    private int page;

    public SpectatorMenu(SpectatorMenuListener param0) {
        this.category = new RootSpectatorMenuCategory();
        this.listener = param0;
    }

    public SpectatorMenuItem getItem(int param0) {
        int var0 = param0 + this.page * 6;
        if (this.page > 0 && param0 == 0) {
            return SCROLL_LEFT;
        } else if (param0 == 7) {
            return var0 < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED;
        } else if (param0 == 8) {
            return CLOSE_ITEM;
        } else {
            return var0 >= 0 && var0 < this.category.getItems().size() ? MoreObjects.firstNonNull(this.category.getItems().get(var0), EMPTY_SLOT) : EMPTY_SLOT;
        }
    }

    public List<SpectatorMenuItem> getItems() {
        List<SpectatorMenuItem> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 <= 8; ++var1) {
            var0.add(this.getItem(var1));
        }

        return var0;
    }

    public SpectatorMenuItem getSelectedItem() {
        return this.getItem(this.selectedSlot);
    }

    public SpectatorMenuCategory getSelectedCategory() {
        return this.category;
    }

    public void selectSlot(int param0) {
        SpectatorMenuItem var0 = this.getItem(param0);
        if (var0 != EMPTY_SLOT) {
            if (this.selectedSlot == param0 && var0.isEnabled()) {
                var0.selectItem(this);
            } else {
                this.selectedSlot = param0;
            }
        }

    }

    public void exit() {
        this.listener.onSpectatorMenuClosed(this);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public void selectCategory(SpectatorMenuCategory param0) {
        this.category = param0;
        this.selectedSlot = -1;
        this.page = 0;
    }

    public SpectatorPage getCurrentPage() {
        return new SpectatorPage(this.category, this.getItems(), this.selectedSlot);
    }

    @OnlyIn(Dist.CLIENT)
    static class CloseSpectatorItem implements SpectatorMenuItem {
        private CloseSpectatorItem() {
        }

        @Override
        public void selectItem(SpectatorMenu param0) {
            param0.exit();
        }

        @Override
        public Component getName() {
            return new TranslatableComponent("spectatorMenu.close");
        }

        @Override
        public void renderIcon(PoseStack param0, float param1, int param2) {
            Minecraft.getInstance().getTextureManager().bind(SpectatorGui.SPECTATOR_LOCATION);
            GuiComponent.blit(param0, 0, 0, 128.0F, 0.0F, 16, 16, 256, 256);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ScrollMenuItem implements SpectatorMenuItem {
        private final int direction;
        private final boolean enabled;

        public ScrollMenuItem(int param0, boolean param1) {
            this.direction = param0;
            this.enabled = param1;
        }

        @Override
        public void selectItem(SpectatorMenu param0) {
            param0.page = param0.page + this.direction;
        }

        @Override
        public Component getName() {
            return this.direction < 0 ? new TranslatableComponent("spectatorMenu.previous_page") : new TranslatableComponent("spectatorMenu.next_page");
        }

        @Override
        public void renderIcon(PoseStack param0, float param1, int param2) {
            Minecraft.getInstance().getTextureManager().bind(SpectatorGui.SPECTATOR_LOCATION);
            if (this.direction < 0) {
                GuiComponent.blit(param0, 0, 0, 144.0F, 0.0F, 16, 16, 256, 256);
            } else {
                GuiComponent.blit(param0, 0, 0, 160.0F, 0.0F, 16, 16, 256, 256);
            }

        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }
    }
}
