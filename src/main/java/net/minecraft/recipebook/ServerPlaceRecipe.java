package net.minecraft.recipebook;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Logger;

public class ServerPlaceRecipe<C extends Container> implements PlaceRecipe<Integer> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StackedContents stackedContents = new StackedContents();
    protected Inventory inventory;
    protected RecipeBookMenu<C> menu;

    public ServerPlaceRecipe(RecipeBookMenu<C> param0) {
        this.menu = param0;
    }

    public void recipeClicked(ServerPlayer param0, @Nullable Recipe<C> param1, boolean param2) {
        if (param1 != null && param0.getRecipeBook().contains(param1)) {
            this.inventory = param0.getInventory();
            if (this.testClearGrid() || param0.isCreative()) {
                this.stackedContents.clear();
                param0.getInventory().fillStackedContents(this.stackedContents);
                this.menu.fillCraftSlotsStackedContents(this.stackedContents);
                if (this.stackedContents.canCraft(param1, null)) {
                    this.handleRecipeClicked(param1, param2);
                } else {
                    this.clearGrid();
                    param0.connection.send(new ClientboundPlaceGhostRecipePacket(param0.containerMenu.containerId, param1));
                }

                param0.getInventory().setChanged();
            }
        }
    }

    protected void clearGrid() {
        for(int var0 = 0; var0 < this.menu.getSize(); ++var0) {
            if (this.menu.shouldMoveToInventory(var0)) {
                ItemStack var1 = this.menu.getSlot(var0).getItem().copy();
                this.inventory.placeItemBackInInventory(var1, false);
                this.menu.getSlot(var0).set(var1);
            }
        }

        this.menu.clearCraftingContent();
    }

    protected void handleRecipeClicked(Recipe<C> param0, boolean param1) {
        boolean var0 = this.menu.recipeMatches(param0);
        int var1 = this.stackedContents.getBiggestCraftableStack(param0, null);
        if (var0) {
            for(int var2 = 0; var2 < this.menu.getGridHeight() * this.menu.getGridWidth() + 1; ++var2) {
                if (var2 != this.menu.getResultSlotIndex()) {
                    ItemStack var3 = this.menu.getSlot(var2).getItem();
                    if (!var3.isEmpty() && Math.min(var1, var3.getMaxStackSize()) < var3.getCount() + 1) {
                        return;
                    }
                }
            }
        }

        int var4 = this.getStackSize(param1, var1, var0);
        IntList var5 = new IntArrayList();
        if (this.stackedContents.canCraft(param0, var5, var4)) {
            int var6 = var4;

            for(int var7 : var5) {
                int var8 = StackedContents.fromStackingIndex(var7).getMaxStackSize();
                if (var8 < var6) {
                    var6 = var8;
                }
            }

            if (this.stackedContents.canCraft(param0, var5, var6)) {
                this.clearGrid();
                this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), param0, var5.iterator(), var6);
            }
        }

    }

    @Override
    public void addItemToSlot(Iterator<Integer> param0, int param1, int param2, int param3, int param4) {
        Slot var0 = this.menu.getSlot(param1);
        ItemStack var1 = StackedContents.fromStackingIndex(param0.next());
        if (!var1.isEmpty()) {
            for(int var2 = 0; var2 < param2; ++var2) {
                this.moveItemToGrid(var0, var1);
            }
        }

    }

    protected int getStackSize(boolean param0, int param1, boolean param2) {
        int var0 = 1;
        if (param0) {
            var0 = param1;
        } else if (param2) {
            var0 = 64;

            for(int var1 = 0; var1 < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++var1) {
                if (var1 != this.menu.getResultSlotIndex()) {
                    ItemStack var2 = this.menu.getSlot(var1).getItem();
                    if (!var2.isEmpty() && var0 > var2.getCount()) {
                        var0 = var2.getCount();
                    }
                }
            }

            if (var0 < 64) {
                ++var0;
            }
        }

        return var0;
    }

    protected void moveItemToGrid(Slot param0, ItemStack param1) {
        int var0 = this.inventory.findSlotMatchingUnusedItem(param1);
        if (var0 != -1) {
            ItemStack var1 = this.inventory.getItem(var0);
            if (!var1.isEmpty()) {
                if (var1.getCount() > 1) {
                    this.inventory.removeItem(var0, 1);
                } else {
                    this.inventory.removeItemNoUpdate(var0);
                }

                if (param0.getItem().isEmpty()) {
                    param0.set(var1.copyWithCount(1));
                } else {
                    param0.getItem().grow(1);
                }

            }
        }
    }

    private boolean testClearGrid() {
        List<ItemStack> var0 = Lists.newArrayList();
        int var1 = this.getAmountOfFreeSlotsInInventory();

        for(int var2 = 0; var2 < this.menu.getGridWidth() * this.menu.getGridHeight() + 1; ++var2) {
            if (var2 != this.menu.getResultSlotIndex()) {
                ItemStack var3 = this.menu.getSlot(var2).getItem().copy();
                if (!var3.isEmpty()) {
                    int var4 = this.inventory.getSlotWithRemainingSpace(var3);
                    if (var4 == -1 && var0.size() <= var1) {
                        for(ItemStack var5 : var0) {
                            if (var5.sameItem(var3) && var5.getCount() != var5.getMaxStackSize() && var5.getCount() + var3.getCount() <= var5.getMaxStackSize()
                                )
                             {
                                var5.grow(var3.getCount());
                                var3.setCount(0);
                                break;
                            }
                        }

                        if (!var3.isEmpty()) {
                            if (var0.size() >= var1) {
                                return false;
                            }

                            var0.add(var3);
                        }
                    } else if (var4 == -1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int getAmountOfFreeSlotsInInventory() {
        int var0 = 0;

        for(ItemStack var1 : this.inventory.items) {
            if (var1.isEmpty()) {
                ++var0;
            }
        }

        return var0;
    }
}
