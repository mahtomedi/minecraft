package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CelebrateVillagersSurvivedRaid extends Behavior<Villager> {
    @Nullable
    private Raid currentRaid;

    public CelebrateVillagersSurvivedRaid(int param0, int param1) {
        super(ImmutableMap.of(), param0, param1);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Villager param1) {
        BlockPos var0 = param1.blockPosition();
        this.currentRaid = param0.getRaidAt(var0);
        return this.currentRaid != null && this.currentRaid.isVictory() && MoveToSkySeeingSpot.hasNoBlocksAbove(param0, param1, var0);
    }

    protected boolean canStillUse(ServerLevel param0, Villager param1, long param2) {
        return this.currentRaid != null && !this.currentRaid.isStopped();
    }

    protected void stop(ServerLevel param0, Villager param1, long param2) {
        this.currentRaid = null;
        param1.getBrain().updateActivityFromSchedule(param0.getDayTime(), param0.getGameTime());
    }

    protected void tick(ServerLevel param0, Villager param1, long param2) {
        RandomSource var0 = param1.getRandom();
        if (var0.nextInt(100) == 0) {
            param1.playCelebrateSound();
        }

        if (var0.nextInt(200) == 0 && MoveToSkySeeingSpot.hasNoBlocksAbove(param0, param1, param1.blockPosition())) {
            DyeColor var1 = Util.getRandom(DyeColor.values(), var0);
            int var2 = var0.nextInt(3);
            ItemStack var3 = this.getFirework(var1, var2);
            FireworkRocketEntity var4 = new FireworkRocketEntity(param1.level(), param1, param1.getX(), param1.getEyeY(), param1.getZ(), var3);
            param1.level().addFreshEntity(var4);
        }

    }

    private ItemStack getFirework(DyeColor param0, int param1) {
        ItemStack var0 = new ItemStack(Items.FIREWORK_ROCKET, 1);
        ItemStack var1 = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag var2 = var1.getOrCreateTagElement("Explosion");
        List<Integer> var3 = Lists.newArrayList();
        var3.add(param0.getFireworkColor());
        var2.putIntArray("Colors", var3);
        var2.putByte("Type", (byte)FireworkRocketItem.Shape.BURST.getId());
        CompoundTag var4 = var0.getOrCreateTagElement("Fireworks");
        ListTag var5 = new ListTag();
        CompoundTag var6 = var1.getTagElement("Explosion");
        if (var6 != null) {
            var5.add(var6);
        }

        var4.putByte("Flight", (byte)param1);
        if (!var5.isEmpty()) {
            var4.put("Explosions", var5);
        }

        return var0;
    }
}
