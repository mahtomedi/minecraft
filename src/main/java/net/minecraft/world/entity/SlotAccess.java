package net.minecraft.world.entity;

import java.util.function.Predicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
    SlotAccess NULL = new SlotAccess() {
        @Override
        public ItemStack get() {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean set(ItemStack param0) {
            return false;
        }
    };

    static SlotAccess forContainer(final Container param0, final int param1, final Predicate<ItemStack> param2) {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return param0.getItem(param1);
            }

            @Override
            public boolean set(ItemStack param0x) {
                if (!param2.test(param0)) {
                    return false;
                } else {
                    param0.setItem(param1, param0);
                    return true;
                }
            }
        };
    }

    static SlotAccess forContainer(Container param0, int param1) {
        return forContainer(param0, param1, param0x -> true);
    }

    static SlotAccess forEquipmentSlot(final LivingEntity param0, final EquipmentSlot param1, final Predicate<ItemStack> param2) {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return param0.getItemBySlot(param1);
            }

            @Override
            public boolean set(ItemStack param0x) {
                if (!param2.test(param0)) {
                    return false;
                } else {
                    param0.setItemSlot(param1, param0);
                    return true;
                }
            }
        };
    }

    static SlotAccess forEquipmentSlot(LivingEntity param0, EquipmentSlot param1) {
        return forEquipmentSlot(param0, param1, param0x -> true);
    }

    ItemStack get();

    boolean set(ItemStack var1);
}
