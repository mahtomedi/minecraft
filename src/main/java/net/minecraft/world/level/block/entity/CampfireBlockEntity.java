package net.minecraft.world.level.block.entity;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;

public class CampfireBlockEntity extends BlockEntity implements Clearable, TickableBlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];

    public CampfireBlockEntity() {
        super(BlockEntityType.CAMPFIRE);
    }

    @Override
    public void tick() {
        boolean var0 = this.getBlockState().getValue(CampfireBlock.LIT);
        boolean var1 = this.level.isClientSide;
        if (var1) {
            if (var0) {
                this.makeParticles();
            }

        } else {
            if (var0) {
                this.cook();
            } else {
                for(int var2 = 0; var2 < this.items.size(); ++var2) {
                    if (this.cookingProgress[var2] > 0) {
                        this.cookingProgress[var2] = Mth.clamp(this.cookingProgress[var2] - 2, 0, this.cookingTime[var2]);
                    }
                }
            }

        }
    }

    private void cook() {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            ItemStack var1 = this.items.get(var0);
            if (!var1.isEmpty()) {
                this.cookingProgress[var0]++;
                if (this.cookingProgress[var0] >= this.cookingTime[var0]) {
                    Container var2 = new SimpleContainer(var1);
                    ItemStack var3 = this.level
                        .getRecipeManager()
                        .getRecipeFor(RecipeType.CAMPFIRE_COOKING, var2, this.level)
                        .map(param1 -> param1.assemble(var2))
                        .orElse(var1);
                    BlockPos var4 = this.getBlockPos();
                    Containers.dropItemStack(this.level, (double)var4.getX(), (double)var4.getY(), (double)var4.getZ(), var3);
                    this.items.set(var0, ItemStack.EMPTY);
                    this.markUpdated();
                }
            }
        }

    }

    private void makeParticles() {
        Level var0 = this.getLevel();
        if (var0 != null) {
            BlockPos var1 = this.getBlockPos();
            Random var2 = var0.random;
            if (var2.nextFloat() < 0.11F) {
                for(int var3 = 0; var3 < var2.nextInt(2) + 2; ++var3) {
                    CampfireBlock.makeParticles(var0, var1, this.getBlockState().getValue(CampfireBlock.SIGNAL_FIRE), false);
                }
            }

            int var4 = this.getBlockState().getValue(CampfireBlock.FACING).get2DDataValue();

            for(int var5 = 0; var5 < this.items.size(); ++var5) {
                if (!this.items.get(var5).isEmpty() && var2.nextFloat() < 0.2F) {
                    Direction var6 = Direction.from2DDataValue(Math.floorMod(var5 + var4, 4));
                    float var7 = 0.3125F;
                    double var8 = (double)var1.getX()
                        + 0.5
                        - (double)((float)var6.getStepX() * 0.3125F)
                        + (double)((float)var6.getClockWise().getStepX() * 0.3125F);
                    double var9 = (double)var1.getY() + 0.5;
                    double var10 = (double)var1.getZ()
                        + 0.5
                        - (double)((float)var6.getStepZ() * 0.3125F)
                        + (double)((float)var6.getClockWise().getStepZ() * 0.3125F);

                    for(int var11 = 0; var11 < 4; ++var11) {
                        var0.addParticle(ParticleTypes.SMOKE, var8, var9, var10, 0.0, 5.0E-4, 0.0);
                    }
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
    public CompoundTag save(CompoundTag param0) {
        this.saveMetadataAndItems(param0);
        param0.putIntArray("CookingTimes", this.cookingProgress);
        param0.putIntArray("CookingTotalTimes", this.cookingTime);
        return param0;
    }

    private CompoundTag saveMetadataAndItems(CompoundTag param0) {
        super.save(param0);
        ContainerHelper.saveAllItems(param0, this.items, true);
        return param0;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 13, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveMetadataAndItems(new CompoundTag());
    }

    public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack param0) {
        return this.items.stream().noneMatch(ItemStack::isEmpty)
            ? Optional.empty()
            : this.level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SimpleContainer(param0), this.level);
    }

    public boolean placeFood(ItemStack param0, int param1) {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            ItemStack var1 = this.items.get(var0);
            if (var1.isEmpty()) {
                this.cookingTime[var0] = param1;
                this.cookingProgress[var0] = 0;
                this.items.set(var0, param0.split(1));
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
        if (!this.getLevel().isClientSide) {
            Containers.dropContents(this.getLevel(), this.getBlockPos(), this.getItems());
        }

        this.markUpdated();
    }
}
