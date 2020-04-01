package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.BookAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LecternScreen extends BookViewScreen implements MenuAccess<LecternMenu> {
    private final LecternMenu menu;
    private final ContainerListener listener = new ContainerListener() {
        @Override
        public void refreshContainer(AbstractContainerMenu param0, NonNullList<ItemStack> param1) {
            LecternScreen.this.bookChanged();
        }

        @Override
        public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
            LecternScreen.this.bookChanged();
        }

        @Override
        public void setContainerData(AbstractContainerMenu param0, int param1, int param2) {
            if (param1 == 0) {
                LecternScreen.this.pageChanged();
            }

        }
    };

    public LecternScreen(LecternMenu param0, Inventory param1, Component param2) {
        this.menu = param0;
    }

    public LecternMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void init() {
        super.init();
        this.menu.addSlotListener(this.listener);
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }

    @Override
    protected void createMenuControls() {
        if (this.minecraft.player.mayBuild()) {
            this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, I18n.get("gui.done"), param0 -> this.minecraft.setScreen(null)));
            this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, I18n.get("lectern.take_book"), param0 -> this.sendButtonClick(3)));
        } else {
            super.createMenuControls();
        }

    }

    @Override
    protected void pageBack() {
        this.sendButtonClick(1);
    }

    @Override
    protected void pageForward() {
        this.sendButtonClick(2);
    }

    @Override
    protected boolean forcePage(int param0) {
        if (param0 != this.menu.getPage()) {
            this.sendButtonClick(100 + param0);
            return true;
        } else {
            return false;
        }
    }

    private void sendButtonClick(int param0) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, param0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void bookChanged() {
        ItemStack var0 = this.menu.getBook();
        this.setBookAccess(BookAccess.fromItem(var0));
    }

    private void pageChanged() {
        this.setPage(this.menu.getPage());
    }
}
