package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface RandomizableContainer extends Container {
    String LOOT_TABLE_TAG = "LootTable";
    String LOOT_TABLE_SEED_TAG = "LootTableSeed";

    @Nullable
    ResourceLocation getLootTable();

    void setLootTable(@Nullable ResourceLocation var1);

    default void setLootTable(ResourceLocation param0, long param1) {
        this.setLootTable(param0);
        this.setLootTableSeed(param1);
    }

    long getLootTableSeed();

    void setLootTableSeed(long var1);

    BlockPos getBlockPos();

    @Nullable
    Level getLevel();

    static void setBlockEntityLootTable(BlockGetter param0, RandomSource param1, BlockPos param2, ResourceLocation param3) {
        BlockEntity var0 = param0.getBlockEntity(param2);
        if (var0 instanceof RandomizableContainer var1) {
            var1.setLootTable(param3, param1.nextLong());
        }

    }

    default boolean tryLoadLootTable(CompoundTag param0) {
        if (param0.contains("LootTable", 8)) {
            this.setLootTable(new ResourceLocation(param0.getString("LootTable")));
            this.setLootTableSeed(param0.getLong("LootTableSeed"));
            return true;
        } else {
            return false;
        }
    }

    default boolean trySaveLootTable(CompoundTag param0) {
        ResourceLocation var0 = this.getLootTable();
        if (var0 == null) {
            return false;
        } else {
            param0.putString("LootTable", var0.toString());
            long var1 = this.getLootTableSeed();
            if (var1 != 0L) {
                param0.putLong("LootTableSeed", var1);
            }

            return true;
        }
    }

    default void unpackLootTable(@Nullable Player param0) {
        Level var0 = this.getLevel();
        BlockPos var1 = this.getBlockPos();
        ResourceLocation var2 = this.getLootTable();
        if (var2 != null && var0 != null && var0.getServer() != null) {
            LootTable var3 = var0.getServer().getLootData().getLootTable(var2);
            if (param0 instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)param0, var2);
            }

            this.setLootTable(null);
            LootParams.Builder var4 = new LootParams.Builder((ServerLevel)var0).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(var1));
            if (param0 != null) {
                var4.withLuck(param0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, param0);
            }

            var3.fill(this, var4.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }

    }
}
