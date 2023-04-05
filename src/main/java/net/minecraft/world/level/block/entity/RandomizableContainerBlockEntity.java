package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity {
    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    protected RandomizableContainerBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
    }

    public static void setLootTable(BlockGetter param0, RandomSource param1, BlockPos param2, ResourceLocation param3) {
        BlockEntity var0 = param0.getBlockEntity(param2);
        if (var0 instanceof RandomizableContainerBlockEntity) {
            ((RandomizableContainerBlockEntity)var0).setLootTable(param3, param1.nextLong());
        }

    }

    protected boolean tryLoadLootTable(CompoundTag param0) {
        if (param0.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(param0.getString("LootTable"));
            this.lootTableSeed = param0.getLong("LootTableSeed");
            return true;
        } else {
            return false;
        }
    }

    protected boolean trySaveLootTable(CompoundTag param0) {
        if (this.lootTable == null) {
            return false;
        } else {
            param0.putString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                param0.putLong("LootTableSeed", this.lootTableSeed);
            }

            return true;
        }
    }

    public void unpackLootTable(@Nullable Player param0) {
        if (this.lootTable != null && this.level.getServer() != null) {
            LootTable var0 = this.level.getServer().getLootData().getLootTable(this.lootTable);
            if (param0 instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)param0, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder var1 = new LootContext.Builder((ServerLevel)this.level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .withOptionalRandomSeed(this.lootTableSeed);
            if (param0 != null) {
                var1.withLuck(param0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, param0);
            }

            var0.fill(this, var1.create(LootContextParamSets.CHEST));
        }

    }

    public void setLootTable(ResourceLocation param0, long param1) {
        this.lootTable = param0;
        this.lootTableSeed = param1;
    }

    @Override
    public boolean isEmpty() {
        this.unpackLootTable(null);
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int param0) {
        this.unpackLootTable(null);
        return this.getItems().get(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        this.unpackLootTable(null);
        ItemStack var0 = ContainerHelper.removeItem(this.getItems(), param0, param1);
        if (!var0.isEmpty()) {
            this.setChanged();
        }

        return var0;
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        this.unpackLootTable(null);
        return ContainerHelper.takeItem(this.getItems(), param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.unpackLootTable(null);
        this.getItems().set(param0, param1);
        if (param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public boolean stillValid(Player param0) {
        return Container.stillValidBlockEntity(this, param0);
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> var1);

    @Override
    public boolean canOpen(Player param0) {
        return super.canOpen(param0) && (this.lootTable == null || !param0.isSpectator());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        if (this.canOpen(param2)) {
            this.unpackLootTable(param1.player);
            return this.createMenu(param0, param1);
        } else {
            return null;
        }
    }
}
