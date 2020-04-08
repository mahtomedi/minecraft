package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.block.Block;

public enum EnchantmentCategory {
    ARMOR {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof ArmorItem;
        }
    },
    ARMOR_FEET {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof ArmorItem && ((ArmorItem)param0).getSlot() == EquipmentSlot.FEET;
        }
    },
    ARMOR_LEGS {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof ArmorItem && ((ArmorItem)param0).getSlot() == EquipmentSlot.LEGS;
        }
    },
    ARMOR_CHEST {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof ArmorItem && ((ArmorItem)param0).getSlot() == EquipmentSlot.CHEST;
        }
    },
    ARMOR_HEAD {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof ArmorItem && ((ArmorItem)param0).getSlot() == EquipmentSlot.HEAD;
        }
    },
    WEAPON {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof SwordItem;
        }
    },
    DIGGER {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof DiggerItem;
        }
    },
    FISHING_ROD {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof FishingRodItem;
        }
    },
    TRIDENT {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof TridentItem;
        }
    },
    BREAKABLE {
        @Override
        public boolean canEnchant(Item param0) {
            return param0.canBeDepleted();
        }
    },
    BOW {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof BowItem;
        }
    },
    WEARABLE {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof Wearable || Block.byItem(param0) instanceof Wearable;
        }
    },
    CROSSBOW {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof CrossbowItem;
        }
    },
    VANISHABLE {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof Vanishable || Block.byItem(param0) instanceof Vanishable || BREAKABLE.canEnchant(param0);
        }
    };

    private EnchantmentCategory() {
    }

    public abstract boolean canEnchant(Item var1);
}
