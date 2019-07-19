package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PumpkinBlock;

public enum EnchantmentCategory {
    ALL {
        @Override
        public boolean canEnchant(Item param0) {
            for(EnchantmentCategory var0 : EnchantmentCategory.values()) {
                if (var0 != EnchantmentCategory.ALL && var0.canEnchant(param0)) {
                    return true;
                }
            }

            return false;
        }
    },
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
            Block var0 = Block.byItem(param0);
            return param0 instanceof ArmorItem || param0 instanceof ElytraItem || var0 instanceof AbstractSkullBlock || var0 instanceof PumpkinBlock;
        }
    },
    CROSSBOW {
        @Override
        public boolean canEnchant(Item param0) {
            return param0 instanceof CrossbowItem;
        }
    };

    private EnchantmentCategory() {
    }

    public abstract boolean canEnchant(Item var1);
}
