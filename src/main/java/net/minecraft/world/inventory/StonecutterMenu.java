package net.minecraft.world.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StonecutterMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final DataSlot selectedRecipeIndex = DataSlot.standalone();
    private final Level level;
    private List<StonecutterRecipe> recipes = Lists.newArrayList();
    private ItemStack input = ItemStack.EMPTY;
    private long lastSoundTime;
    final Slot inputSlot;
    final Slot resultSlot;
    private Runnable slotUpdateListener = () -> {
    };
    public final Container container = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            StonecutterMenu.this.slotsChanged(this);
            StonecutterMenu.this.slotUpdateListener.run();
        }
    };
    private final ResultContainer resultContainer = new ResultContainer();

    public StonecutterMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public StonecutterMenu(int param0, Inventory param1, final ContainerLevelAccess param2) {
        super(MenuType.STONECUTTER, param0);
        this.access = param2;
        this.level = param1.player.level;
        this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack param0) {
                return false;
            }

            @Override
            public ItemStack onTake(Player param0, ItemStack param1) {
                ItemStack var0 = StonecutterMenu.this.inputSlot.remove(1);
                if (!var0.isEmpty()) {
                    StonecutterMenu.this.setupResultSlot();
                }

                param1.getItem().onCraftedBy(param1, param0.level, param0);
                param2.execute((param0x, param1x) -> {
                    long var0x = param0x.getGameTime();
                    if (StonecutterMenu.this.lastSoundTime != var0x) {
                        param0x.playSound(null, param1x, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        StonecutterMenu.this.lastSoundTime = var0x;
                    }

                });
                return super.onTake(param0, param1);
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

        this.addDataSlot(this.selectedRecipeIndex);
    }

    @OnlyIn(Dist.CLIENT)
    public int getSelectedRecipeIndex() {
        return this.selectedRecipeIndex.get();
    }

    @OnlyIn(Dist.CLIENT)
    public List<StonecutterRecipe> getRecipes() {
        return this.recipes;
    }

    @OnlyIn(Dist.CLIENT)
    public int getNumRecipes() {
        return this.recipes.size();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasInputItem() {
        return this.inputSlot.hasItem() && !this.recipes.isEmpty();
    }

    @Override
    public boolean stillValid(Player param0) {
        return stillValid(this.access, param0, Blocks.STONECUTTER);
    }

    @Override
    public boolean clickMenuButton(Player param0, int param1) {
        if (this.isValidRecipeIndex(param1)) {
            this.selectedRecipeIndex.set(param1);
            this.setupResultSlot();
        }

        return true;
    }

    private boolean isValidRecipeIndex(int param0) {
        return param0 >= 0 && param0 < this.recipes.size();
    }

    @Override
    public void slotsChanged(Container param0) {
        ItemStack var0 = this.inputSlot.getItem();
        if (var0.getItem() != this.input.getItem()) {
            this.input = var0.copy();
            this.setupRecipeList(param0, var0);
        }

    }

    private void setupRecipeList(Container param0, ItemStack param1) {
        this.recipes.clear();
        this.selectedRecipeIndex.set(-1);
        this.resultSlot.set(ItemStack.EMPTY);
        if (!param1.isEmpty()) {
            this.recipes = this.level.getRecipeManager().getRecipesFor(RecipeType.STONECUTTING, param0, this.level);
        }

    }

    private void setupResultSlot() {
        if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
            StonecutterRecipe var0 = this.recipes.get(this.selectedRecipeIndex.get());
            this.resultSlot.set(var0.assemble(this.container));
        } else {
            this.resultSlot.set(ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    @Override
    public MenuType<?> getType() {
        return MenuType.STONECUTTER;
    }

    @OnlyIn(Dist.CLIENT)
    public void registerUpdateListener(Runnable param0) {
        this.slotUpdateListener = param0;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return param1.container != this.resultContainer && super.canTakeItemForPickAll(param0, param1);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            Item var3 = var2.getItem();
            var0 = var2.copy();
            if (param1 == 1) {
                var3.onCraftedBy(var2, param0.level, param0);
                if (!this.moveItemStackTo(var2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 == 0) {
                if (!this.moveItemStackTo(var2, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.level.getRecipeManager().getRecipeFor(RecipeType.STONECUTTING, new SimpleContainer(var2), this.level).isPresent()) {
                if (!this.moveItemStackTo(var2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 2 && param1 < 29) {
                if (!this.moveItemStackTo(var2, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (param1 >= 29 && param1 < 38 && !this.moveItemStackTo(var2, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            }

            var1.setChanged();
            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
            this.broadcastChanges();
        }

        return var0;
    }

    @Override
    public void removed(Player param0) {
        super.removed(param0);
        this.resultContainer.removeItemNoUpdate(1);
        this.access.execute((param1, param2) -> this.clearContainer(param0, param0.level, this.container));
    }
}
