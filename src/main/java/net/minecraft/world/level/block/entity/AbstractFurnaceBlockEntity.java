package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractFurnaceBlockEntity
    extends BaseContainerBlockEntity
    implements WorldlyContainer,
    RecipeHolder,
    StackedContentsCompatible,
    TickableBlockEntity {
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;
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
    protected final RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> param0, RecipeType<? extends AbstractCookingRecipe> param1) {
        super(param0);
        this.recipeType = param1;
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
        return var0;
    }

    private static void add(Map<Item, Integer> param0, Tag<Item> param1, int param2) {
        for(Item var0 : param1.getValues()) {
            param0.put(var0, param2);
        }

    }

    private static void add(Map<Item, Integer> param0, ItemLike param1, int param2) {
        param0.put(param1.asItem(), param2);
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
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putShort("BurnTime", (short)this.litTime);
        param0.putShort("CookTime", (short)this.cookingProgress);
        param0.putShort("CookTimeTotal", (short)this.cookingTotalTime);
        ContainerHelper.saveAllItems(param0, this.items);
        CompoundTag var0 = new CompoundTag();
        this.recipesUsed.forEach((param1, param2) -> var0.putInt(param1.toString(), param2));
        param0.put("RecipesUsed", var0);
        return param0;
    }

    @Override
    public void tick() {
        boolean var0 = this.isLit();
        boolean var1 = false;
        if (this.isLit()) {
            --this.litTime;
        }

        if (!this.level.isClientSide) {
            ItemStack var2 = this.items.get(1);
            if (this.isLit() || !var2.isEmpty() && !this.items.get(0).isEmpty()) {
                Recipe<?> var3 = (Recipe)this.level.getRecipeManager().getRecipeFor(this.recipeType, this, this.level).orElse(null);
                if (!this.isLit() && this.canBurn(var3)) {
                    this.litTime = this.getBurnDuration(var2);
                    this.litDuration = this.litTime;
                    if (this.isLit()) {
                        var1 = true;
                        if (!var2.isEmpty()) {
                            Item var4 = var2.getItem();
                            var2.shrink(1);
                            if (var2.isEmpty()) {
                                Item var5 = var4.getCraftingRemainingItem();
                                this.items.set(1, var5 == null ? ItemStack.EMPTY : new ItemStack(var5));
                            }
                        }
                    }
                }

                if (this.isLit() && this.canBurn(var3)) {
                    ++this.cookingProgress;
                    if (this.cookingProgress == this.cookingTotalTime) {
                        this.cookingProgress = 0;
                        this.cookingTotalTime = this.getTotalCookTime();
                        this.burn(var3);
                        var1 = true;
                    }
                } else {
                    this.cookingProgress = 0;
                }
            } else if (!this.isLit() && this.cookingProgress > 0) {
                this.cookingProgress = Mth.clamp(this.cookingProgress - 2, 0, this.cookingTotalTime);
            }

            if (var0 != this.isLit()) {
                var1 = true;
                this.level
                    .setBlock(
                        this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(this.isLit())), 3
                    );
            }
        }

        if (var1) {
            this.setChanged();
        }

    }

    protected boolean canBurn(@Nullable Recipe<?> param0) {
        if (!this.items.get(0).isEmpty() && param0 != null) {
            ItemStack var0 = param0.getResultItem();
            if (var0.isEmpty()) {
                return false;
            } else {
                ItemStack var1 = this.items.get(2);
                if (var1.isEmpty()) {
                    return true;
                } else if (!var1.sameItem(var0)) {
                    return false;
                } else if (var1.getCount() < this.getMaxStackSize() && var1.getCount() < var1.getMaxStackSize()) {
                    return true;
                } else {
                    return var1.getCount() < var0.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    private void burn(@Nullable Recipe<?> param0) {
        if (param0 != null && this.canBurn(param0)) {
            ItemStack var0 = this.items.get(0);
            ItemStack var1 = param0.getResultItem();
            ItemStack var2 = this.items.get(2);
            if (var2.isEmpty()) {
                this.items.set(2, var1.copy());
            } else if (var2.getItem() == var1.getItem()) {
                var2.grow(1);
            }

            if (!this.level.isClientSide) {
                this.setRecipeUsed(param0);
            }

            if (var0.getItem() == Blocks.WET_SPONGE.asItem() && !this.items.get(1).isEmpty() && this.items.get(1).getItem() == Items.BUCKET) {
                this.items.set(1, new ItemStack(Items.WATER_BUCKET));
            }

            var0.shrink(1);
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

    protected int getTotalCookTime() {
        return this.level.getRecipeManager().getRecipeFor(this.recipeType, this, this.level).map(AbstractCookingRecipe::getCookingTime).orElse(200);
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
            Item var0 = param1.getItem();
            if (var0 != Items.WATER_BUCKET && var0 != Items.BUCKET) {
                return false;
            }
        }

        return true;
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
            this.cookingTotalTime = this.getTotalCookTime();
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
            return isFuel(param1) || param1.getItem() == Items.BUCKET && var0.getItem() != Items.BUCKET;
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
    public void awardAndReset(Player param0) {
    }

    public void awardResetAndExperience(Player param0) {
        List<Recipe<?>> var0 = Lists.newArrayList();

        for(Entry<ResourceLocation> var1 : this.recipesUsed.object2IntEntrySet()) {
            param0.level.getRecipeManager().byKey(var1.getKey()).ifPresent(param3 -> {
                var0.add(param3);
                createExperience(param0, var1.getIntValue(), ((AbstractCookingRecipe)param3).getExperience());
            });
        }

        param0.awardRecipes(var0);
        this.recipesUsed.clear();
    }

    private static void createExperience(Player param0, int param1, float param2) {
        if (param2 == 0.0F) {
            param1 = 0;
        } else if (param2 < 1.0F) {
            int var0 = Mth.floor((float)param1 * param2);
            if (var0 < Mth.ceil((float)param1 * param2) && Math.random() < (double)((float)param1 * param2 - (float)var0)) {
                ++var0;
            }

            param1 = var0;
        }

        while(param1 > 0) {
            int var1 = ExperienceOrb.getExperienceValue(param1);
            param1 -= var1;
            param0.level.addFreshEntity(new ExperienceOrb(param0.level, param0.getX(), param0.getY() + 0.5, param0.getZ() + 0.5, var1));
        }

    }

    @Override
    public void fillStackedContents(StackedContents param0) {
        for(ItemStack var0 : this.items) {
            param0.accountStack(var0);
        }

    }
}
