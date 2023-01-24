package net.minecraft.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CampfireBlockEntity extends BlockEntity implements Clearable {
    private static final int BURN_COOL_SPEED = 2;
    private static final int NUM_SLOTS = 4;
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];
    private final RecipeManager.CachedCheck<Container, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

    public CampfireBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.CAMPFIRE, param0, param1);
    }

    public static void cookTick(Level param0, BlockPos param1, BlockState param2, CampfireBlockEntity param3) {
        boolean var0 = false;

        for(int var1 = 0; var1 < param3.items.size(); ++var1) {
            ItemStack var2 = param3.items.get(var1);
            if (!var2.isEmpty()) {
                var0 = true;
                param3.cookingProgress[var1]++;
                if (param3.cookingProgress[var1] >= param3.cookingTime[var1]) {
                    Container var3 = new SimpleContainer(var2);
                    ItemStack var4 = param3.quickCheck.getRecipeFor(var3, param0).map(param2x -> param2x.assemble(var3, param0.registryAccess())).orElse(var2);
                    if (var4.isItemEnabled(param0.enabledFeatures())) {
                        Containers.dropItemStack(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), var4);
                        param3.items.set(var1, ItemStack.EMPTY);
                        param0.sendBlockUpdated(param1, param2, param2, 3);
                        param0.gameEvent(GameEvent.BLOCK_CHANGE, param1, GameEvent.Context.of(param2));
                    }
                }
            }
        }

        if (var0) {
            setChanged(param0, param1, param2);
        }

    }

    public static void cooldownTick(Level param0, BlockPos param1, BlockState param2, CampfireBlockEntity param3) {
        boolean var0 = false;

        for(int var1 = 0; var1 < param3.items.size(); ++var1) {
            if (param3.cookingProgress[var1] > 0) {
                var0 = true;
                param3.cookingProgress[var1] = Mth.clamp(param3.cookingProgress[var1] - 2, 0, param3.cookingTime[var1]);
            }
        }

        if (var0) {
            setChanged(param0, param1, param2);
        }

    }

    public static void particleTick(Level param0, BlockPos param1, BlockState param2, CampfireBlockEntity param3) {
        RandomSource var0 = param0.random;
        if (var0.nextFloat() < 0.11F) {
            for(int var1 = 0; var1 < var0.nextInt(2) + 2; ++var1) {
                CampfireBlock.makeParticles(param0, param1, param2.getValue(CampfireBlock.SIGNAL_FIRE), false);
            }
        }

        int var2 = param2.getValue(CampfireBlock.FACING).get2DDataValue();

        for(int var3 = 0; var3 < param3.items.size(); ++var3) {
            if (!param3.items.get(var3).isEmpty() && var0.nextFloat() < 0.2F) {
                Direction var4 = Direction.from2DDataValue(Math.floorMod(var3 + var2, 4));
                float var5 = 0.3125F;
                double var6 = (double)param1.getX()
                    + 0.5
                    - (double)((float)var4.getStepX() * 0.3125F)
                    + (double)((float)var4.getClockWise().getStepX() * 0.3125F);
                double var7 = (double)param1.getY() + 0.5;
                double var8 = (double)param1.getZ()
                    + 0.5
                    - (double)((float)var4.getStepZ() * 0.3125F)
                    + (double)((float)var4.getClockWise().getStepZ() * 0.3125F);

                for(int var9 = 0; var9 < 4; ++var9) {
                    param0.addParticle(ParticleTypes.SMOKE, var6, var7, var8, 0.0, 5.0E-4, 0.0);
                }
            }
        }

    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items.clear();
        ContainerHelper.loadAllItems(param0, this.items);
        if (param0.contains("CookingTimes", 11)) {
            int[] var0 = param0.getIntArray("CookingTimes");
            System.arraycopy(var0, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, var0.length));
        }

        if (param0.contains("CookingTotalTimes", 11)) {
            int[] var1 = param0.getIntArray("CookingTotalTimes");
            System.arraycopy(var1, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, var1.length));
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        ContainerHelper.saveAllItems(param0, this.items, true);
        param0.putIntArray("CookingTimes", this.cookingProgress);
        param0.putIntArray("CookingTotalTimes", this.cookingTime);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = new CompoundTag();
        ContainerHelper.saveAllItems(var0, this.items, true);
        return var0;
    }

    public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack param0) {
        return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SimpleContainer(param0), this.level);
    }

    public boolean placeFood(@Nullable Entity param0, ItemStack param1, int param2) {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            ItemStack var1 = this.items.get(var0);
            if (var1.isEmpty()) {
                this.cookingTime[var0] = param2;
                this.cookingProgress[var0] = 0;
                this.items.set(var0, param1.split(1));
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(param0, this.getBlockState()));
                this.markUpdated();
                return true;
            }
        }

        return false;
    }

    private void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    public void dowse() {
        if (this.level != null) {
            this.markUpdated();
        }

    }
}
