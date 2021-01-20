package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LecternMenu extends AbstractContainerMenu {
    private final Container lectern;
    private final ContainerData lecternData;

    public LecternMenu(int param0) {
        this(param0, new SimpleContainer(1), new SimpleContainerData(1));
    }

    public LecternMenu(int param0, Container param1, ContainerData param2) {
        super(MenuType.LECTERN, param0);
        checkContainerSize(param1, 1);
        checkContainerDataCount(param2, 1);
        this.lectern = param1;
        this.lecternData = param2;
        this.addSlot(new Slot(param1, 0, 0, 0) {
            @Override
            public void setChanged() {
                super.setChanged();
                LecternMenu.this.slotsChanged(this.container);
            }
        });
        this.addDataSlots(param2);
    }

    @Override
    public boolean clickMenuButton(Player param0, int param1) {
        if (param1 >= 100) {
            int var0 = param1 - 100;
            this.setData(0, var0);
            return true;
        } else {
            switch(param1) {
                case 1:
                    int var2 = this.lecternData.get(0);
                    this.setData(0, var2 - 1);
                    return true;
                case 2:
                    int var1 = this.lecternData.get(0);
                    this.setData(0, var1 + 1);
                    return true;
                case 3:
                    if (!param0.mayBuild()) {
                        return false;
                    }

                    ItemStack var3 = this.lectern.removeItemNoUpdate(0);
                    this.lectern.setChanged();
                    if (!param0.getInventory().add(var3)) {
                        param0.drop(var3, false);
                    }

                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void setData(int param0, int param1) {
        super.setData(param0, param1);
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.lectern.stillValid(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getBook() {
        return this.lectern.getItem(0);
    }

    @OnlyIn(Dist.CLIENT)
    public int getPage() {
        return this.lecternData.get(0);
    }
}
