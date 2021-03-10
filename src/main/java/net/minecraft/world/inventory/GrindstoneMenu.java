package net.minecraft.world.inventory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class GrindstoneMenu extends AbstractContainerMenu {
    private final Container resultSlots = new ResultContainer();
    private final Container repairSlots = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            GrindstoneMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;

    public GrindstoneMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public GrindstoneMenu(int param0, Inventory param1, final ContainerLevelAccess param2) {
        super(MenuType.GRINDSTONE, param0);
        this.access = param2;
        this.addSlot(new Slot(this.repairSlots, 0, 49, 19) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.isDamageableItem() || param0.is(Items.ENCHANTED_BOOK) || param0.isEnchanted();
            }
        });
        this.addSlot(new Slot(this.repairSlots, 1, 49, 40) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return param0.isDamageableItem() || param0.is(Items.ENCHANTED_BOOK) || param0.isEnchanted();
            }
        });
        this.addSlot(new Slot(this.resultSlots, 2, 129, 34) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return false;
            }

            @Override
            public void onTake(Player param0, ItemStack param1) {
                param2.execute((param0x, param1x) -> {
                    if (param0x instanceof ServerLevel) {
                        ExperienceOrb.award((ServerLevel)param0x, Vec3.atCenterOf(param1x), this.getExperienceAmount(param0x));
                    }

                    param0x.levelEvent(1042, param1x, 0);
                });
                GrindstoneMenu.this.repairSlots.setItem(0, ItemStack.EMPTY);
                GrindstoneMenu.this.repairSlots.setItem(1, ItemStack.EMPTY);
            }

            private int getExperienceAmount(Level param0) {
                int var0 = 0;
                var0 += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(0));
                var0 += this.getExperienceFromItem(GrindstoneMenu.this.repairSlots.getItem(1));
                if (var0 > 0) {
                    int var1 = (int)Math.ceil((double)var0 / 2.0);
                    return var1 + param0.random.nextInt(var1);
                } else {
                    return 0;
                }
            }

            private int getExperienceFromItem(ItemStack param0) {
                int var0 = 0;
                Map<Enchantment, Integer> var1 = EnchantmentHelper.getEnchantments(param0);

                for(Entry<Enchantment, Integer> var2 : var1.entrySet()) {
                    Enchantment var3 = var2.getKey();
                    Integer var4 = var2.getValue();
                    if (!var3.isCurse()) {
                        var0 += var3.getMinCost(var4);
                    }
                }

                return var0;
            }
        });

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 9; ++var1) {
                this.addSlot(new Slot(param1, var1 + var0 * 9 + 9, 8 + var1 * 18, 84 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            this.addSlot(new Slot(param1, var2, 8 + var2 * 18, 142));
        }

    }

    @Override
    public void slotsChanged(Container param0) {
        super.slotsChanged(param0);
        if (param0 == this.repairSlots) {
            this.createResult();
        }

    }

    private void createResult() {
        ItemStack var0 = this.repairSlots.getItem(0);
        ItemStack var1 = this.repairSlots.getItem(1);
        boolean var2 = !var0.isEmpty() || !var1.isEmpty();
        boolean var3 = !var0.isEmpty() && !var1.isEmpty();
        if (!var2) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            boolean var4 = !var0.isEmpty() && !var0.is(Items.ENCHANTED_BOOK) && !var0.isEnchanted()
                || !var1.isEmpty() && !var1.is(Items.ENCHANTED_BOOK) && !var1.isEnchanted();
            if (var0.getCount() > 1 || var1.getCount() > 1 || !var3 && var4) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
                this.broadcastChanges();
                return;
            }

            int var5 = 1;
            int var10;
            ItemStack var11;
            if (var3) {
                if (!var0.is(var1.getItem())) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.broadcastChanges();
                    return;
                }

                Item var6 = var0.getItem();
                int var7 = var6.getMaxDamage() - var0.getDamageValue();
                int var8 = var6.getMaxDamage() - var1.getDamageValue();
                int var9 = var7 + var8 + var6.getMaxDamage() * 5 / 100;
                var10 = Math.max(var6.getMaxDamage() - var9, 0);
                var11 = this.mergeEnchants(var0, var1);
                if (!var11.isDamageableItem()) {
                    if (!ItemStack.matches(var0, var1)) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.broadcastChanges();
                        return;
                    }

                    var5 = 2;
                }
            } else {
                boolean var12 = !var0.isEmpty();
                var10 = var12 ? var0.getDamageValue() : var1.getDamageValue();
                var11 = var12 ? var0 : var1;
            }

            this.resultSlots.setItem(0, this.removeNonCurses(var11, var10, var5));
        }

        this.broadcastChanges();
    }

    private ItemStack mergeEnchants(ItemStack param0, ItemStack param1) {
        ItemStack var0 = param0.copy();
        Map<Enchantment, Integer> var1 = EnchantmentHelper.getEnchantments(param1);

        for(Entry<Enchantment, Integer> var2 : var1.entrySet()) {
            Enchantment var3 = var2.getKey();
            if (!var3.isCurse() || EnchantmentHelper.getItemEnchantmentLevel(var3, var0) == 0) {
                var0.enchant(var3, var2.getValue());
            }
        }

        return var0;
    }

    private ItemStack removeNonCurses(ItemStack param0, int param1, int param2) {
        ItemStack var0 = param0.copy();
        var0.removeTagKey("Enchantments");
        var0.removeTagKey("StoredEnchantments");
        if (param1 > 0) {
            var0.setDamageValue(param1);
        } else {
            var0.removeTagKey("Damage");
        }

        var0.setCount(param2);
        Map<Enchantment, Integer> var1 = EnchantmentHelper.getEnchantments(param0)
            .entrySet()
            .stream()
            .filter(param0x -> param0x.getKey().isCurse())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        EnchantmentHelper.setEnchantments(var1, var0);
        var0.setRepairCost(0);
        if (var0.is(Items.ENCHANTED_BOOK) && var1.size() == 0) {
            var0 = new ItemStack(Items.BOOK);
            if (param0.hasCustomHoverName()) {
                var0.setHoverName(param0.getHoverName());
            }
        }

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            var0.setRepairCost(AnvilMenu.calculateIncreasedRepairCost(var0.getBaseRepairCost()));
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.access.execute((param1, param2) -> this.clearContainer(param0, this.repairSlots));
    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.GRINDSTONE);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            ItemStack var3 = this.repairSlots.getItem(0);
            ItemStack var4 = this.repairSlots.getItem(1);
            if (param1 == 2) {
                if (!this.moveItemStackTo(var2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 != 0 && param1 != 1) {
                if (!var3.isEmpty() && !var4.isEmpty()) {
                    if (param1 >= 3 && param1 < 30) {
                        if (!this.moveItemStackTo(var2, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (param1 >= 30 && param1 < 39 && !this.moveItemStackTo(var2, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(var2, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 3, 39, false)) {
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
}
