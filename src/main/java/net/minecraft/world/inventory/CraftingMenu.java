package net.minecraft.world.inventory;

import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CraftingMenu extends RecipeBookMenu<CraftingContainer> {
    private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    public CraftingMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public CraftingMenu(int param0, Inventory param1, ContainerLevelAccess param2) {
        super(MenuType.CRAFTING, param0);
        this.access = param2;
        this.player = param1.player;
        this.addSlot(new ResultSlot(param1.player, this.craftSlots, this.resultSlots, 0, 124, 35));

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 3; ++var1) {
                this.addSlot(new Slot(this.craftSlots, var1 + var0 * 3, 30 + var1 * 18, 17 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 3; ++var2) {
            for(int var3 = 0; var3 < 9; ++var3) {
                this.addSlot(new Slot(param1, var3 + var2 * 9 + 9, 8 + var3 * 18, 84 + var2 * 18));
            }
        }

        for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlot(new Slot(param1, var4, 8 + var4 * 18, 142));
        }

    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu param0, Level param1, Player param2, CraftingContainer param3, ResultContainer param4) {
        if (!param1.isClientSide) {
            ServerPlayer var0 = (ServerPlayer)param2;
            ItemStack var1 = ItemStack.EMPTY;
            Optional<CraftingRecipe> var2 = param1.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, param3, param1);
            if (var2.isPresent()) {
                CraftingRecipe var3 = var2.get();
                if (param4.setRecipeUsed(param1, var0, var3)) {
                    var1 = var3.assemble(param3);
                }
            }

            param4.setItem(0, var1);
            param0.setRemoteSlot(0, var1);
            var0.connection.send(new ClientboundContainerSetSlotPacket(param0.containerId, 0, var1));
        }
    }

    @Override
    public void slotsChanged(Container param0) {
        this.access.execute((param0x, param1) -> slotChangedCraftingGrid(this, param0x, this.player, this.craftSlots, this.resultSlots));
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents param0) {
        this.craftSlots.fillStackedContents(param0);
    }

    @Override
    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    @Override
    public boolean recipeMatches(Recipe<? super CraftingContainer> param0) {
        return param0.matches(this.craftSlots, this.player.level);
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.access.execute((param1, param2) -> this.clearContainer(param0, this.craftSlots));
    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.CRAFTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 0) {
                this.access.execute((param2, param3) -> var2.getItem().onCraftedBy(var2, param2, param0));
                if (!this.moveItemStackTo(var2, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 >= 10 && param1 < 46) {
                if (!this.moveItemStackTo(var2, 1, 10, false)) {
                    if (param1 < 37) {
                        if (!this.moveItemStackTo(var2, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(var2, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(var2, 10, 46, false)) {
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
            if (param1 == 0) {
                param0.drop(var2, false);
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

    @Override
    public int getSize() {
        return 10;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public boolean shouldMoveToInventory(int param0) {
        return param0 != this.getResultSlotIndex();
    }
}
