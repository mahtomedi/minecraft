package net.minecraft.world.inventory;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BrewingStandMenu extends AbstractContainerMenu {
    private final Container brewingStand;
    private final ContainerData brewingStandData;
    private final Slot ingredientSlot;

    public BrewingStandMenu(int param0, Inventory param1) {
        this(param0, param1, new SimpleContainer(5), new SimpleContainerData(2));
    }

    public BrewingStandMenu(int param0, Inventory param1, Container param2, ContainerData param3) {
        super(MenuType.BREWING_STAND, param0);
        checkContainerSize(param2, 5);
        checkContainerDataCount(param3, 2);
        this.brewingStand = param2;
        this.brewingStandData = param3;
        this.addSlot(new BrewingStandMenu.PotionSlot(param2, 0, 56, 51));
        this.addSlot(new BrewingStandMenu.PotionSlot(param2, 1, 79, 58));
        this.addSlot(new BrewingStandMenu.PotionSlot(param2, 2, 102, 51));
        this.ingredientSlot = this.addSlot(new BrewingStandMenu.IngredientsSlot(param2, 3, 79, 17));
        this.addSlot(new BrewingStandMenu.FuelSlot(param2, 4, 17, 17));
        this.addDataSlots(param3);

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
    public boolean stillValid(Player param0) {
        return this.brewingStand.stillValid(param0);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if ((param1 < 0 || param1 > 2) && param1 != 3 && param1 != 4) {
                if (BrewingStandMenu.FuelSlot.mayPlaceItem(var0)) {
                    if (this.moveItemStackTo(var2, 4, 5, false) || this.ingredientSlot.mayPlace(var2) && !this.moveItemStackTo(var2, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.ingredientSlot.mayPlace(var2)) {
                    if (!this.moveItemStackTo(var2, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (BrewingStandMenu.PotionSlot.mayPlaceItem(var0) && var0.getCount() == 1) {
                    if (!this.moveItemStackTo(var2, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 5 && param1 < 32) {
                    if (!this.moveItemStackTo(var2, 32, 41, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 32 && param1 < 41) {
                    if (!this.moveItemStackTo(var2, 5, 32, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(var2, 5, 41, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(var2, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
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

    @OnlyIn(Dist.CLIENT)
    public int getFuel() {
        return this.brewingStandData.get(1);
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrewingTicks() {
        return this.brewingStandData.get(0);
    }

    static class FuelSlot extends Slot {
        public FuelSlot(Container param0, int param1, int param2, int param3) {
            super(param0, param1, param2, param3);
        }

        @Override
        public boolean mayPlace(ItemStack param0) {
            return mayPlaceItem(param0);
        }

        public static boolean mayPlaceItem(ItemStack param0) {
            return param0.is(Items.BLAZE_POWDER);
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }
    }

    static class IngredientsSlot extends Slot {
        public IngredientsSlot(Container param0, int param1, int param2, int param3) {
            super(param0, param1, param2, param3);
        }

        @Override
        public boolean mayPlace(ItemStack param0) {
            return PotionBrewing.isIngredient(param0);
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }
    }

    static class PotionSlot extends Slot {
        public PotionSlot(Container param0, int param1, int param2, int param3) {
            super(param0, param1, param2, param3);
        }

        @Override
        public boolean mayPlace(ItemStack param0) {
            return mayPlaceItem(param0);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public ItemStack onTake(Player param0, ItemStack param1) {
            Potion var0 = PotionUtils.getPotion(param1);
            if (param0 instanceof ServerPlayer) {
                CriteriaTriggers.BREWED_POTION.trigger((ServerPlayer)param0, var0);
            }

            super.onTake(param0, param1);
            return param1;
        }

        public static boolean mayPlaceItem(ItemStack param0) {
            return param0.is(Items.POTION) || param0.is(Items.SPLASH_POTION) || param0.is(Items.LINGERING_POTION) || param0.is(Items.GLASS_BOTTLE);
        }
    }
}
