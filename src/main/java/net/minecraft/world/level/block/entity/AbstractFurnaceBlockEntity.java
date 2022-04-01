package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
    protected static final int SLOT_INPUT = 0;
    protected static final int SLOT_FUEL = 1;
    protected static final int SLOT_RESULT = 2;
    public static final int DATA_LIT_TIME = 0;
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int NUM_DATA_VALUES = 4;
    public static final int BURN_TIME_STANDARD = 200;
    public static final int BURN_COOL_SPEED = 2;
    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    int litTime;
    int litDuration;
    int cookingProgress;
    int cookingTotalTime;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int param0) {
            switch(param0) {
                case 0:
                    return AbstractFurnaceBlockEntity.this.litTime;
                case 1:
                    return AbstractFurnaceBlockEntity.this.litDuration;
                case 2:
                    return AbstractFurnaceBlockEntity.this.cookingProgress;
                case 3:
                    return AbstractFurnaceBlockEntity.this.cookingTotalTime;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int param0, int param1) {
            switch(param0) {
                case 0:
                    AbstractFurnaceBlockEntity.this.litTime = param1;
                    break;
                case 1:
                    AbstractFurnaceBlockEntity.this.litDuration = param1;
                    break;
                case 2:
                    AbstractFurnaceBlockEntity.this.cookingProgress = param1;
                    break;
                case 3:
                    AbstractFurnaceBlockEntity.this.cookingTotalTime = param1;
            }

        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2, RecipeType<? extends AbstractCookingRecipe> param3) {
        super(param0, param1, param2);
        this.recipeType = param3;
    }

    public static Map<Item, Integer> getFuel() {
        Map<Item, Integer> var0 = Maps.newLinkedHashMap();
        add(var0, Items.LAVA_BUCKET, 20000);
        add(var0, Blocks.COAL_BLOCK, 16000);
        add(var0, Items.BLAZE_ROD, 2400);
        add(var0, Items.COAL, 1600);
        add(var0, Items.CHARCOAL, 1600);
        add(var0, ItemTags.LOGS, 300);
        add(var0, ItemTags.PLANKS, 300);
        add(var0, ItemTags.WOODEN_STAIRS, 300);
        add(var0, ItemTags.WOODEN_SLABS, 150);
        add(var0, ItemTags.WOODEN_TRAPDOORS, 300);
        add(var0, ItemTags.WOODEN_PRESSURE_PLATES, 300);
        add(var0, Blocks.OAK_FENCE, 300);
        add(var0, Blocks.BIRCH_FENCE, 300);
        add(var0, Blocks.SPRUCE_FENCE, 300);
        add(var0, Blocks.JUNGLE_FENCE, 300);
        add(var0, Blocks.DARK_OAK_FENCE, 300);
        add(var0, Blocks.ACACIA_FENCE, 300);
        add(var0, Blocks.OAK_FENCE_GATE, 300);
        add(var0, Blocks.BIRCH_FENCE_GATE, 300);
        add(var0, Blocks.SPRUCE_FENCE_GATE, 300);
        add(var0, Blocks.JUNGLE_FENCE_GATE, 300);
        add(var0, Blocks.DARK_OAK_FENCE_GATE, 300);
        add(var0, Blocks.ACACIA_FENCE_GATE, 300);
        add(var0, Blocks.NOTE_BLOCK, 300);
        add(var0, Blocks.BOOKSHELF, 300);
        add(var0, Blocks.LECTERN, 300);
        add(var0, Blocks.JUKEBOX, 300);
        add(var0, Blocks.CHEST, 300);
        add(var0, Blocks.TRAPPED_CHEST, 300);
        add(var0, Blocks.CRAFTING_TABLE, 300);
        add(var0, Blocks.DAYLIGHT_DETECTOR, 300);
        add(var0, ItemTags.BANNERS, 300);
        add(var0, Items.BOW, 300);
        add(var0, Items.FISHING_ROD, 300);
        add(var0, Blocks.LADDER, 300);
        add(var0, ItemTags.SIGNS, 200);
        add(var0, Items.WOODEN_SHOVEL, 200);
        add(var0, Items.WOODEN_SWORD, 200);
        add(var0, Items.WOODEN_HOE, 200);
        add(var0, Items.WOODEN_AXE, 200);
        add(var0, Items.WOODEN_PICKAXE, 200);
        add(var0, ItemTags.WOODEN_DOORS, 200);
        add(var0, ItemTags.BOATS, 1200);
        add(var0, ItemTags.WOOL, 100);
        add(var0, ItemTags.WOODEN_BUTTONS, 100);
        add(var0, Items.STICK, 100);
        add(var0, ItemTags.SAPLINGS, 100);
        add(var0, Items.BOWL, 100);
        add(var0, ItemTags.CARPETS, 67);
        add(var0, Blocks.DRIED_KELP_BLOCK, 4001);
        add(var0, Items.CROSSBOW, 300);
        add(var0, Blocks.BAMBOO, 50);
        add(var0, Blocks.DEAD_BUSH, 100);
        add(var0, Blocks.SCAFFOLDING, 400);
        add(var0, Blocks.LOOM, 300);
        add(var0, Blocks.BARREL, 300);
        add(var0, Blocks.CARTOGRAPHY_TABLE, 300);
        add(var0, Blocks.FLETCHING_TABLE, 300);
        add(var0, Blocks.SMITHING_TABLE, 300);
        add(var0, Blocks.COMPOSTER, 300);
        add(var0, Blocks.AZALEA, 100);
        add(var0, Blocks.FLOWERING_AZALEA, 100);
        return var0;
    }

    private static boolean isNeverAFurnaceFuel(Item param0) {
        return param0.builtInRegistryHolder().is(ItemTags.NON_FLAMMABLE_WOOD);
    }

    private static void add(Map<Item, Integer> param0, TagKey<Item> param1, int param2) {
        for(Holder<Item> var0 : Registry.ITEM.getTagOrEmpty(param1)) {
            if (!isNeverAFurnaceFuel(var0.value())) {
                param0.put(var0.value(), param2);
            }
        }

    }

    private static void add(Map<Item, Integer> param0, ItemLike param1, int param2) {
        Item var0 = param1.asItem();
        if (isNeverAFurnaceFuel(var0)) {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw (IllegalStateException)Util.pauseInIde(
                    new IllegalStateException(
                        "A developer tried to explicitly make fire resistant item " + var0.getName(null).getString() + " a furnace fuel. That will not work!"
                    )
                );
            }
        } else {
            param0.put(var0, param2);
        }
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(param0, this.items);
        this.litTime = param0.getShort("BurnTime");
        this.cookingProgress = param0.getShort("CookTime");
        this.cookingTotalTime = param0.getShort("CookTimeTotal");
        this.litDuration = this.getBurnDuration(this.items.get(1));
        CompoundTag var0 = param0.getCompound("RecipesUsed");

        for(String var1 : var0.getAllKeys()) {
            this.recipesUsed.put(new ResourceLocation(var1), var0.getInt(var1));
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putShort("BurnTime", (short)this.litTime);
        param0.putShort("CookTime", (short)this.cookingProgress);
        param0.putShort("CookTimeTotal", (short)this.cookingTotalTime);
        ContainerHelper.saveAllItems(param0, this.items);
        CompoundTag var0 = new CompoundTag();
        this.recipesUsed.forEach((param1, param2) -> var0.putInt(param1.toString(), param2));
        param0.put("RecipesUsed", var0);
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, AbstractFurnaceBlockEntity param3) {
        boolean var0 = param3.isLit();
        boolean var1 = false;
        if (param3.isLit()) {
            --param3.litTime;
        }

        ItemStack var2 = param3.items.get(1);
        if (param3.isLit() || !var2.isEmpty() && !param3.items.get(0).isEmpty()) {
            Recipe<?> var3 = (Recipe)param0.getRecipeManager().getRecipeFor(param3.recipeType, param3, param0).orElse(null);
            int var4 = param3.getMaxStackSize();
            if (!param3.isLit() && canBurn(var3, param3.items, var4)) {
                param3.litTime = param3.getBurnDuration(var2);
                param3.litDuration = param3.litTime;
                if (param3.isLit()) {
                    var1 = true;
                    if (!var2.isEmpty()) {
                        Item var5 = var2.getItem();
                        var2.shrink(1);
                        if (var2.isEmpty()) {
                            Item var6 = var5.getCraftingRemainingItem();
                            param3.items.set(1, var6 == null ? ItemStack.EMPTY : new ItemStack(var6));
                        }
                    }
                }
            }

            if (param3.isLit() && canBurn(var3, param3.items, var4)) {
                ++param3.cookingProgress;
                if (param3.cookingProgress == param3.cookingTotalTime) {
                    param3.cookingProgress = 0;
                    param3.cookingTotalTime = getTotalCookTime(param0, param3.recipeType, param3);
                    if (burn(var3, param3.items, var4)) {
                        param3.setRecipeUsed(var3);
                    }

                    var1 = true;
                }
            } else {
                param3.cookingProgress = 0;
            }
        } else if (!param3.isLit() && param3.cookingProgress > 0) {
            param3.cookingProgress = Mth.clamp(param3.cookingProgress - 2, 0, param3.cookingTotalTime);
        }

        if (var0 != param3.isLit()) {
            var1 = true;
            param2 = param2.setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(param3.isLit()));
            param0.setBlock(param1, param2, 3);
        }

        if (var1) {
            setChanged(param0, param1, param2);
        }

    }

    private static boolean canBurn(@Nullable Recipe<?> param0, NonNullList<ItemStack> param1, int param2) {
        if (!param1.get(0).isEmpty() && param0 != null) {
            ItemStack var0 = param0.getResultItem();
            if (var0.isEmpty()) {
                return false;
            } else {
                ItemStack var1 = param1.get(2);
                if (var1.isEmpty()) {
                    return true;
                } else if (!var1.sameItem(var0)) {
                    return false;
                } else if (var1.getCount() < param2 && var1.getCount() < var1.getMaxStackSize()) {
                    return true;
                } else {
                    return var1.getCount() < var0.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    private static boolean burn(@Nullable Recipe<?> param0, NonNullList<ItemStack> param1, int param2) {
        if (param0 != null && canBurn(param0, param1, param2)) {
            ItemStack var0 = param1.get(0);
            ItemStack var1 = param0.getResultItem();
            ItemStack var2 = param1.get(2);
            if (var2.isEmpty()) {
                param1.set(2, var1.copy());
            } else if (var2.is(var1.getItem())) {
                var2.grow(1);
            }

            if (var0.is(Blocks.WET_SPONGE.asItem()) && !param1.get(1).isEmpty() && param1.get(1).is(Items.BUCKET)) {
                param1.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            var0.shrink(1);
            return true;
        } else {
            return false;
        }
    }

    protected int getBurnDuration(ItemStack param0) {
        if (param0.isEmpty()) {
            return 0;
        } else {
            Item var0 = param0.getItem();
            return getFuel().getOrDefault(var0, 0);
        }
    }

    private static int getTotalCookTime(Level param0, RecipeType<? extends AbstractCookingRecipe> param1, Container param2) {
        return param0.getRecipeManager().getRecipeFor(param1, param2, param0).map(AbstractCookingRecipe::getCookingTime).orElse(200);
    }

    public static boolean isFuel(ItemStack param0) {
        return getFuel().containsKey(param0.getItem());
    }

    @Override
    public int[] getSlotsForFace(Direction param0) {
        if (param0 == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return param0 == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int param0, ItemStack param1, @Nullable Direction param2) {
        return this.canPlaceItem(param0, param1);
    }

    @Override
    public boolean canTakeItemThroughFace(int param0, ItemStack param1, Direction param2) {
        if (param2 == Direction.DOWN && param0 == 1) {
            return param1.is(Items.WATER_BUCKET) || param1.is(Items.BUCKET);
        } else {
            return true;
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.items) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int param0) {
        return this.items.get(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        return ContainerHelper.removeItem(this.items, param0, param1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return ContainerHelper.takeItem(this.items, param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        ItemStack var0 = this.items.get(param0);
        boolean var1 = !param1.isEmpty() && param1.sameItem(var0) && ItemStack.tagMatches(param1, var0);
        this.items.set(param0, param1);
        if (param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

        if (param0 == 0 && !var1) {
            this.cookingTotalTime = getTotalCookTime(this.level, this.recipeType, this);
            this.cookingProgress = 0;
            this.setChanged();
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return param0.distanceToSqr(
                    (double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5
                )
                <= 64.0;
        }
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        if (param0 == 2) {
            return false;
        } else if (param0 != 1) {
            return true;
        } else {
            ItemStack var0 = this.items.get(1);
            return isFuel(param1) || param1.is(Items.BUCKET) && !var0.is(Items.BUCKET);
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public void setRecipeUsed(@Nullable Recipe<?> param0) {
        if (param0 != null) {
            ResourceLocation var0 = param0.getId();
            this.recipesUsed.addTo(var0, 1);
        }

    }

    @Nullable
    @Override
    public Recipe<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player param0) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer param0) {
        List<Recipe<?>> var0 = this.getRecipesToAwardAndPopExperience(param0.getLevel(), param0.position());
        param0.awardRecipes(var0);
        this.recipesUsed.clear();
    }

    public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel param0, Vec3 param1) {
        List<Recipe<?>> var0 = Lists.newArrayList();

        for(Entry<ResourceLocation> var1 : this.recipesUsed.object2IntEntrySet()) {
            param0.getRecipeManager().byKey(var1.getKey()).ifPresent(param4 -> {
                var0.add(param4);
                createExperience(param0, param1, var1.getIntValue(), ((AbstractCookingRecipe)param4).getExperience());
            });
        }

        return var0;
    }

    private static void createExperience(ServerLevel param0, Vec3 param1, int param2, float param3) {
        int var0 = Mth.floor((float)param2 * param3);
        float var1 = Mth.frac((float)param2 * param3);
        if (var1 != 0.0F && Math.random() < (double)var1) {
            ++var0;
        }

        ExperienceOrb.award(param0, param1, var0);
    }

    @Override
    public void fillStackedContents(StackedContents param0) {
        for(ItemStack var0 : this.items) {
            param0.accountStack(var0);
        }

    }
}
