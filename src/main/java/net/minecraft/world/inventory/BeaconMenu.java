package net.minecraft.world.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class BeaconMenu extends AbstractContainerMenu {
    private static final int PAYMENT_SLOT = 0;
    private static final int SLOT_COUNT = 1;
    private static final int DATA_COUNT = 3;
    private static final int INV_SLOT_START = 1;
    private static final int INV_SLOT_END = 28;
    private static final int USE_ROW_SLOT_START = 28;
    private static final int USE_ROW_SLOT_END = 37;
    private static final int NO_EFFECT = 0;
    private final Container beacon = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int param0, ItemStack param1) {
            return param1.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    private final BeaconMenu.PaymentSlot paymentSlot;
    private final ContainerLevelAccess access;
    private final ContainerData beaconData;

    public BeaconMenu(int param0, Container param1) {
        this(param0, param1, new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    public BeaconMenu(int param0, Container param1, ContainerData param2, ContainerLevelAccess param3) {
        super(MenuType.BEACON, param0);
        checkContainerDataCount(param2, 3);
        this.beaconData = param2;
        this.access = param3;
        this.paymentSlot = new BeaconMenu.PaymentSlot(this.beacon, 0, 136, 110);
        this.addSlot(this.paymentSlot);
        this.addDataSlots(param2);
        int var0 = 36;
        int var1 = 137;

        for(int var2 = 0; var2 < 3; ++var2) {
            for(int var3 = 0; var3 < 9; ++var3) {
                this.addSlot(new Slot(param1, var3 + var2 * 9 + 9, 36 + var3 * 18, 137 + var2 * 18));
            }
        }

        for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlot(new Slot(param1, var4, 36 + var4 * 18, 195));
        }

    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        if (!param0.level().isClientSide) {
            ItemStack var0 = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
            if (!var0.isEmpty()) {
                param0.drop(var0, false);
            }

        }
    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.BEACON);
    }

    @Override
    public void setData(int param0, int param1) {
        super.setData(param0, param1);
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 0) {
                if (!this.moveItemStackTo(var2, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (!this.paymentSlot.hasItem() && this.paymentSlot.mayPlace(var2) && var2.getCount() == 1) {
                if (!this.moveItemStackTo(var2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 1 && param1 < 28) {
                if (!this.moveItemStackTo(var2, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 28 && param1 < 37) {
                if (!this.moveItemStackTo(var2, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 1, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.setByPlayer(ItemStack.EMPTY);
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

    public int getLevels() {
        return this.beaconData.get(0);
    }

    public static int encodeEffect(@Nullable MobEffect param0) {
        return param0 == null ? 0 : BuiltInRegistries.MOB_EFFECT.getId(param0) + 1;
    }

    @Nullable
    public static MobEffect decodeEffect(int param0) {
        return param0 == 0 ? null : BuiltInRegistries.MOB_EFFECT.byId(param0 - 1);
    }

    @Nullable
    public MobEffect getPrimaryEffect() {
        return decodeEffect(this.beaconData.get(1));
    }

    @Nullable
    public MobEffect getSecondaryEffect() {
        return decodeEffect(this.beaconData.get(2));
    }

    public void updateEffects(Optional<MobEffect> param0, Optional<MobEffect> param1) {
        if (this.paymentSlot.hasItem()) {
            this.beaconData.set(1, encodeEffect(param0.orElse(null)));
            this.beaconData.set(2, encodeEffect(param1.orElse(null)));
            this.paymentSlot.remove(1);
            this.access.execute(Level::blockEntityChanged);
        }

    }

    public boolean hasPayment() {
        return !this.beacon.getItem(0).isEmpty();
    }

    class PaymentSlot extends Slot {
        public PaymentSlot(Container param0, int param1, int param2, int param3) {
            super(param0, param1, param2, param3);
        }

        @Override
        public boolean mayPlace(ItemStack param0) {
            return param0.is(ItemTags.BEACON_PAYMENT_ITEMS);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
