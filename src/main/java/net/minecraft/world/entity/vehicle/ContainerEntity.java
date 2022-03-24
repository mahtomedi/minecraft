package net.minecraft.world.entity.vehicle;

import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface ContainerEntity extends Container, MenuProvider {
    Vec3 position();

    @Nullable
    ResourceLocation getLootTable();

    void setLootTable(@Nullable ResourceLocation var1);

    long getLootTableSeed();

    void setLootTableSeed(long var1);

    NonNullList<ItemStack> getItemStacks();

    void clearItemStacks();

    Level getLevel();

    boolean isRemoved();

    @Override
    default boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    default void addChestVehicleSaveData(CompoundTag param0) {
        if (this.getLootTable() != null) {
            param0.putString("LootTable", this.getLootTable().toString());
            if (this.getLootTableSeed() != 0L) {
                param0.putLong("LootTableSeed", this.getLootTableSeed());
            }
        } else {
            ContainerHelper.saveAllItems(param0, this.getItemStacks());
        }

    }

    default void readChestVehicleSaveData(CompoundTag param0) {
        this.clearItemStacks();
        if (param0.contains("LootTable", 8)) {
            this.setLootTable(new ResourceLocation(param0.getString("LootTable")));
            this.setLootTableSeed(param0.getLong("LootTableSeed"));
        } else {
            ContainerHelper.loadAllItems(param0, this.getItemStacks());
        }

    }

    default void dropChestVehicleContents(DamageSource param0, Level param1, Entity param2) {
        if (param1.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            Containers.dropContents(param1, param2, this);
            if (!param1.isClientSide) {
                Entity var0 = param0.getDirectEntity();
                if (var0 != null && var0.getType() == EntityType.PLAYER) {
                    PiglinAi.angerNearbyPiglins((Player)var0, true);
                }
            }

        }
    }

    default InteractionResult interactWithChestVehicle(BiConsumer<GameEvent, Entity> param0, Player param1) {
        param1.openMenu(this);
        if (!param1.level.isClientSide) {
            param0.accept(GameEvent.CONTAINER_OPEN, param1);
            PiglinAi.angerNearbyPiglins(param1, true);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    default void unpackChestVehicleLootTable(@Nullable Player param0) {
        MinecraftServer var0 = this.getLevel().getServer();
        if (this.getLootTable() != null && var0 != null) {
            LootTable var1 = var0.getLootTables().get(this.getLootTable());
            if (param0 != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)param0, this.getLootTable());
            }

            this.setLootTable(null);
            LootContext.Builder var2 = new LootContext.Builder((ServerLevel)this.getLevel())
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withOptionalRandomSeed(this.getLootTableSeed());
            if (param0 != null) {
                var2.withLuck(param0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, param0);
            }

            var1.fill(this, var2.create(LootContextParamSets.CHEST));
        }

    }

    default void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().clear();
    }

    default boolean isChestVehicleEmpty() {
        for(ItemStack var0 : this.getItemStacks()) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    default ItemStack removeChestVehicleItemNoUpdate(int param0) {
        this.unpackChestVehicleLootTable(null);
        ItemStack var0 = this.getItemStacks().get(param0);
        if (var0.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.getItemStacks().set(param0, ItemStack.EMPTY);
            return var0;
        }
    }

    default ItemStack getChestVehicleItem(int param0) {
        this.unpackChestVehicleLootTable(null);
        return this.getItemStacks().get(param0);
    }

    default ItemStack removeChestVehicleItem(int param0, int param1) {
        this.unpackChestVehicleLootTable(null);
        return ContainerHelper.removeItem(this.getItemStacks(), param0, param1);
    }

    default void setChestVehicleItem(int param0, ItemStack param1) {
        this.unpackChestVehicleLootTable(null);
        this.getItemStacks().set(param0, param1);
        if (!param1.isEmpty() && param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

    }

    default SlotAccess getChestVehicleSlot(final int param0) {
        return param0 >= 0 && param0 < this.getContainerSize() ? new SlotAccess() {
            @Override
            public ItemStack get() {
                return ContainerEntity.this.getChestVehicleItem(param0);
            }

            @Override
            public boolean set(ItemStack param0x) {
                ContainerEntity.this.setChestVehicleItem(param0, param0);
                return true;
            }
        } : SlotAccess.NULL;
    }

    default boolean isChestVehicleStillValid(Player param0) {
        return !this.isRemoved() && this.position().closerThan(param0.position(), 8.0);
    }
}
