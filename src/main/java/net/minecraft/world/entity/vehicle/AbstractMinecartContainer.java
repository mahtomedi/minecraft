package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements Container, MenuProvider {
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    private boolean dropEquipment = true;
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    protected AbstractMinecartContainer(EntityType<?> param0, Level param1) {
        super(param0, param1);
    }

    protected AbstractMinecartContainer(EntityType<?> param0, double param1, double param2, double param3, Level param4) {
        super(param0, param4, param1, param2, param3);
    }

    @Override
    public void destroy(DamageSource param0) {
        super.destroy(param0);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            Containers.dropContents(this.level, this, this);
            if (!this.level.isClientSide) {
                Entity var0 = param0.getDirectEntity();
                if (var0 != null && var0.getType() == EntityType.PLAYER) {
                    PiglinAi.angerNearbyPiglins((Player)var0, true);
                }
            }
        }

    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.itemStacks) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int param0) {
        this.unpackLootTable(null);
        return this.itemStacks.get(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        this.unpackLootTable(null);
        return ContainerHelper.removeItem(this.itemStacks, param0, param1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        this.unpackLootTable(null);
        ItemStack var0 = this.itemStacks.get(param0);
        if (var0.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.itemStacks.set(param0, ItemStack.EMPTY);
            return var0;
        }
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.unpackLootTable(null);
        this.itemStacks.set(param0, param1);
        if (!param1.isEmpty() && param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

    }

    @Override
    public boolean setSlot(int param0, ItemStack param1) {
        if (param0 >= 0 && param0 < this.getContainerSize()) {
            this.setItem(param0, param1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player param0) {
        if (this.removed) {
            return false;
        } else {
            return !(param0.distanceToSqr(this) > 64.0);
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel param0) {
        this.dropEquipment = false;
        return super.changeDimension(param0);
    }

    @Override
    public void remove() {
        if (!this.level.isClientSide && this.dropEquipment) {
            Containers.dropContents(this.level, this, this);
        }

        super.remove();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.lootTable != null) {
            param0.putString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                param0.putLong("LootTableSeed", this.lootTableSeed);
            }
        } else {
            ContainerHelper.saveAllItems(param0, this.itemStacks);
        }

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (param0.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(param0.getString("LootTable"));
            this.lootTableSeed = param0.getLong("LootTableSeed");
        } else {
            ContainerHelper.loadAllItems(param0, this.itemStacks);
        }

    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        param0.openMenu(this);
        if (!param0.level.isClientSide) {
            PiglinAi.angerNearbyPiglins(param0, true);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    protected void applyNaturalSlowdown() {
        float var0 = 0.98F;
        if (this.lootTable == null) {
            int var1 = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
            var0 += (float)var1 * 0.001F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)var0, 0.0, (double)var0));
    }

    public void unpackLootTable(@Nullable Player param0) {
        if (this.lootTable != null && this.level.getServer() != null) {
            LootTable var0 = this.level.getServer().getLootTables().get(this.lootTable);
            if (param0 instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)param0, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder var1 = new LootContext.Builder((ServerLevel)this.level)
                .withParameter(LootContextParams.BLOCK_POS, this.blockPosition())
                .withOptionalRandomSeed(this.lootTableSeed);
            if (param0 != null) {
                var1.withLuck(param0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, param0);
            }

            var0.fill(this, var1.create(LootContextParamSets.CHEST));
        }

    }

    @Override
    public void clearContent() {
        this.unpackLootTable(null);
        this.itemStacks.clear();
    }

    public void setLootTable(ResourceLocation param0, long param1) {
        this.lootTable = param0;
        this.lootTableSeed = param1;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        if (this.lootTable != null && param2.isSpectator()) {
            return null;
        } else {
            this.unpackLootTable(param1.player);
            return this.createMenu(param0, param1);
        }
    }

    protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);
}
