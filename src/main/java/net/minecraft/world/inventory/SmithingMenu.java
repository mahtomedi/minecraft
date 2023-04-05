package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends ItemCombinerMenu {
    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int ADDITIONAL_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
    public static final int BASE_SLOT_X_PLACEMENT = 26;
    public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
    private static final int RESULT_SLOT_X_PLACEMENT = 98;
    public static final int SLOT_Y_PLACEMENT = 48;
    private final Level level;
    @Nullable
    private SmithingRecipe selectedRecipe;
    private final List<SmithingRecipe> recipes;

    public SmithingMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int param0, Inventory param1, ContainerLevelAccess param2) {
        super(MenuType.SMITHING, param0, param1, param2);
        this.level = param1.player.level;
        this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
            .withSlot(0, 8, 48, param0 -> this.recipes.stream().anyMatch(param1 -> param1.isTemplateIngredient(param0)))
            .withSlot(
                1,
                26,
                48,
                param0 -> this.recipes.stream().anyMatch(param1 -> param1.isBaseIngredient(param0) && param1.isTemplateIngredient(this.slots.get(0).getItem()))
            )
            .withSlot(
                2,
                44,
                48,
                param0 -> this.recipes
                        .stream()
                        .anyMatch(param1 -> param1.isAdditionIngredient(param0) && param1.isTemplateIngredient(this.slots.get(0).getItem()))
            )
            .withResultSlot(3, 98, 48)
            .build();
    }

    @Override
    protected boolean isValidBlock(BlockState param0) {
        return param0.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected boolean mayPickup(Player param0, boolean param1) {
        return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
    }

    @Override
    protected void onTake(Player param0, ItemStack param1) {
        param1.onCraftedBy(param0.level, param0, param1.getCount());
        this.resultSlots.awardUsedRecipes(param0);
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((param0x, param1x) -> param0x.levelEvent(1044, param1x, 0));
    }

    private void shrinkStackInSlot(int param0) {
        ItemStack var0 = this.inputSlots.getItem(param0);
        if (!var0.isEmpty()) {
            var0.shrink(1);
            this.inputSlots.setItem(param0, var0);
        }

    }

    @Override
    public void createResult() {
        List<SmithingRecipe> var0 = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
        if (var0.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            SmithingRecipe var1 = var0.get(0);
            ItemStack var2 = var1.assemble(this.inputSlots, this.level.registryAccess());
            if (var2.isItemEnabled(this.level.enabledFeatures())) {
                this.selectedRecipe = var1;
                this.resultSlots.setRecipeUsed(var1);
                this.resultSlots.setItem(0, var2);
            }
        }

    }

    @Override
    public int getSlotToQuickMoveTo(ItemStack param0) {
        return this.recipes
            .stream()
            .map(param1 -> findSlotMatchingIngredient(param1, param0))
            .filter(Optional::isPresent)
            .findFirst()
            .orElse(Optional.of(0))
            .get();
    }

    private static Optional<Integer> findSlotMatchingIngredient(SmithingRecipe param0, ItemStack param1) {
        if (param0.isTemplateIngredient(param1)) {
            return Optional.of(0);
        } else if (param0.isBaseIngredient(param1)) {
            return Optional.of(1);
        } else {
            return param0.isAdditionIngredient(param1) ? Optional.of(2) : Optional.empty();
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack param0, Slot param1) {
        return param1.container != this.resultSlots && super.canTakeItemForPickAll(param0, param1);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack param0) {
        return this.recipes.stream().map(param1 -> findSlotMatchingIngredient(param1, param0)).anyMatch(Optional::isPresent);
    }
}
