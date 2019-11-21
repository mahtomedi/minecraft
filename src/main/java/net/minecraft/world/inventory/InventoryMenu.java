package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InventoryMenu extends RecipeBookMenu<CraftingContainer> {
    public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{
        EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET
    };
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final CraftingContainer craftSlots = new CraftingContainer(this, 2, 2);
    private final ResultContainer resultSlots = new ResultContainer();
    public final boolean active;
    private final Player owner;

    public InventoryMenu(Inventory param0, boolean param1, Player param2) {
        super(null, 0);
        this.active = param1;
        this.owner = param2;
        this.addSlot(new ResultSlot(param0.player, this.craftSlots, this.resultSlots, 0, 154, 28));

        for(int var0 = 0; var0 < 2; ++var0) {
            for(int var1 = 0; var1 < 2; ++var1) {
                this.addSlot(new Slot(this.craftSlots, var1 + var0 * 2, 98 + var1 * 18, 18 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 4; ++var2) {
            final EquipmentSlot var3 = SLOT_IDS[var2];
            this.addSlot(new Slot(param0, 39 - var2, 8, 8 + var2 * 18) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack param0) {
                    return var3 == Mob.getEquipmentSlotForItem(param0);
                }

                @Override
                public boolean mayPickup(Player param0) {
                    ItemStack var0 = this.getItem();
                    return !var0.isEmpty() && !param0.isCreative() && EnchantmentHelper.hasBindingCurse(var0) ? false : super.mayPickup(param0);
                }

                @OnlyIn(Dist.CLIENT)
                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.TEXTURE_EMPTY_SLOTS[var3.getIndex()]);
                }
            });
        }

        for(int var4 = 0; var4 < 3; ++var4) {
            for(int var5 = 0; var5 < 9; ++var5) {
                this.addSlot(new Slot(param0, var5 + (var4 + 1) * 9, 8 + var5 * 18, 84 + var4 * 18));
            }
        }

        for(int var6 = 0; var6 < 9; ++var6) {
            this.addSlot(new Slot(param0, var6, 8 + var6 * 18, 142));
        }

        this.addSlot(new Slot(param0, 40, 77, 62) {
            @OnlyIn(Dist.CLIENT)
            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents param0) {
        this.craftSlots.fillStackedContents(param0);
    }

    @Override
    public void clearCraftingContent() {
        this.resultSlots.clearContent();
        this.craftSlots.clearContent();
    }

    @Override
    public boolean recipeMatches(Recipe<? super CraftingContainer> param0) {
        return param0.matches(this.craftSlots, this.owner.level);
    }

    @Override
    public void slotsChanged(Container param0) {
        CraftingMenu.slotChangedCraftingGrid(this.containerId, this.owner.level, this.owner, this.craftSlots, this.resultSlots);
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.resultSlots.clearContent();
        if (!param0.level.isClientSide) {
            this.clearContainer(param0, param0.level, this.craftSlots);
        }
    }

    @Override
    public boolean stillValid(Player param0) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            EquipmentSlot var3 = Mob.getEquipmentSlotForItem(var0);
            if (param1 == 0) {
                if (!this.moveItemStackTo(var2, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 >= 1 && param1 < 5) {
                if (!this.moveItemStackTo(var2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 5 && param1 < 9) {
                if (!this.moveItemStackTo(var2, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (var3.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - var3.getIndex()).hasItem()) {
                int var4 = 8 - var3.getIndex();
                if (!this.moveItemStackTo(var2, var4, var4 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (var3 == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
                if (!this.moveItemStackTo(var2, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 9 && param1 < 36) {
                if (!this.moveItemStackTo(var2, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 36 && param1 < 45) {
                if (!this.moveItemStackTo(var2, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 9, 45, false)) {
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

            ItemStack var5 = var1.onTake(param0, var2);
            if (param1 == 0) {
                param0.drop(var5, false);
            }
        }

        return var0;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return param1.container != this.resultSlots && super.canTakeItemForPickAll(param0, param1);
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    @Override
    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSize() {
        return 5;
    }
}
