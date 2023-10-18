package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;

public class CrafterMenu extends AbstractContainerMenu implements ContainerListener {
    public final ResultContainer resultSlots = new ResultContainer();
    protected static final int SLOT_COUNT = 9;
    private static final int INV_SLOT_START = 9;
    private static final int INV_SLOT_END = 36;
    private static final int USE_ROW_SLOT_START = 36;
    private static final int USE_ROW_SLOT_END = 45;
    private final ContainerData containerData;
    private final Player player;
    private final CraftingContainer container;

    public CrafterMenu(int param0, Inventory param1) {
        super(MenuType.CRAFTER_3x3, param0);
        this.player = param1.player;
        this.containerData = new SimpleContainerData(10);
        this.container = new TransientCraftingContainer(this, 3, 3);
        this.addSlots(param1);
    }

    public CrafterMenu(int param0, Inventory param1, CraftingContainer param2, ContainerData param3) {
        super(MenuType.CRAFTER_3x3, param0);
        this.player = param1.player;
        this.containerData = param3;
        this.container = param2;
        checkContainerSize(param2, 9);
        param2.startOpen(param1.player);
        this.addSlots(param1);
        this.addSlotListener(this);
    }

    private void addSlots(Inventory param0) {
        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 3; ++var1) {
                int var2 = var1 + var0 * 3;
                this.addSlot(new CrafterSlot(this.container, var2, 26 + var1 * 18, 17 + var0 * 18, this));
            }
        }

        for(int var3 = 0; var3 < 3; ++var3) {
            for(int var4 = 0; var4 < 9; ++var4) {
                this.addSlot(new Slot(param0, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for(int var5 = 0; var5 < 9; ++var5) {
            this.addSlot(new Slot(param0, var5, 8 + var5 * 18, 142));
        }

        this.addSlot(new NonInteractiveResultSlot(this.resultSlots, 0, 134, 35));
        this.addDataSlots(this.containerData);
        this.refreshRecipeResult();
    }

    public void setSlotState(int param0, boolean param1) {
        CrafterSlot var0 = (CrafterSlot)this.getSlot(param0);
        this.containerData.set(var0.index, param1 ? 0 : 1);
        this.broadcastChanges();
    }

    public boolean isSlotDisabled(int param0) {
        if (param0 > -1 && param0 < 9) {
            return this.containerData.get(param0) == 1;
        } else {
            return false;
        }
    }

    public boolean isPowered() {
        return this.containerData.get(9) == 1;
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 < 9) {
                if (!this.moveItemStackTo(var2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }

            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
        }

        return var0;
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.container.stillValid(param0);
    }

    private void refreshRecipeResult() {
        Player var1 = this.player;
        if (var1 instanceof ServerPlayer var0) {
            Level var1x = var0.level();
            ItemStack var2x = CrafterBlock.getPotentialResults(var1x, this.container)
                .map(param1 -> param1.assemble(this.container, var1.registryAccess()))
                .orElse(ItemStack.EMPTY);
            this.resultSlots.setItem(0, var2x);
        }

    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public void slotChanged(AbstractContainerMenu param0, int param1, ItemStack param2) {
        this.refreshRecipeResult();
    }

    @Override
    public void dataChanged(AbstractContainerMenu param0, int param1, int param2) {
    }
}
