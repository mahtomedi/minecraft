package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LISTENER_RADIUS = 8;
    private static final int WARNING_SOUND_RADIUS = 10;
    private static final int WARDEN_SPAWN_ATTEMPTS = 20;
    private static final int WARDEN_SPAWN_RANGE_XZ = 5;
    private static final int WARDEN_SPAWN_RANGE_Y = 6;
    private static final int DARKNESS_RADIUS = 40;
    private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
        param0.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
        param0.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
    });
    private static final int SHRIEKING_TICKS = 90;
    private int warningLevel;
    private VibrationListener listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this, null, 0, 0);

    public SculkShriekerBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_SHRIEKER, param0, param1);
    }

    public VibrationListener getListener() {
        return this.listener;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("warning_level", 99)) {
            this.warningLevel = param0.getInt("warning_level");
        }

        if (param0.contains("listener", 10)) {
            VibrationListener.codec(this)
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.listener = param0x);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putInt("warning_level", this.warningLevel);
        VibrationListener.codec(this)
            .encodeStart(NbtOps.INSTANCE, this.listener)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
    }

    @Override
    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.SHRIEKER_CAN_LISTEN;
    }

    @Override
    public boolean shouldListen(ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable GameEvent.Context param4) {
        return !this.isRemoved() && this.canShriek(param0);
    }

    @Override
    public void onSignalReceive(
        ServerLevel param0, GameEventListener param1, BlockPos param2, GameEvent param3, @Nullable Entity param4, @Nullable Entity param5, int param6
    ) {
        this.shriek(param0, param5 != null ? param5 : param4);
    }

    private boolean canShriek(ServerLevel param0) {
        BlockState var0 = this.getBlockState();
        if (var0.getValue(SculkShriekerBlock.SHRIEKING)) {
            return false;
        } else if (!var0.getValue(SculkShriekerBlock.CAN_SUMMON)) {
            return true;
        } else {
            BlockPos var1 = this.getBlockPos();
            return tryGetSpawnTracker(param0, var1).map(param2 -> param2.canWarn(param0, var1)).orElse(false);
        }
    }

    public void shriek(ServerLevel param0, @Nullable Entity param1) {
        BlockState var0 = this.getBlockState();
        if (this.canShriek(param0) && this.tryToWarn(param0, var0)) {
            BlockPos var1 = this.getBlockPos();
            param0.setBlock(var1, var0.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
            param0.scheduleTick(var1, var0.getBlock(), 90);
            param0.levelEvent(3007, var1, 0);
            param0.gameEvent(GameEvent.SHRIEK, var1, GameEvent.Context.of(param1));
        }

    }

    private boolean tryToWarn(ServerLevel param0, BlockState param1) {
        if (param1.getValue(SculkShriekerBlock.CAN_SUMMON)) {
            BlockPos var0 = this.getBlockPos();
            Optional<WardenSpawnTracker> var1 = tryGetSpawnTracker(param0, var0).filter(param2 -> param2.warn(param0, var0));
            if (var1.isEmpty()) {
                return false;
            }

            this.warningLevel = var1.get().getWarningLevel();
        }

        return true;
    }

    private static Optional<WardenSpawnTracker> tryGetSpawnTracker(ServerLevel param0, BlockPos param1) {
        Player var0 = param0.getNearestPlayer(
            (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), 16.0, EntitySelector.NO_SPECTATORS.and(Entity::isAlive)
        );
        return var0 == null ? Optional.empty() : Optional.of(var0.getWardenSpawnTracker());
    }

    public void replyOrSummon(ServerLevel param0) {
        if (this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON)) {
            Warden.applyDarknessAround(param0, Vec3.atCenterOf(this.getBlockPos()), null, 40);
            if (this.warningLevel >= 3) {
                trySummonWarden(param0, this.getBlockPos());
                return;
            }
        }

        this.playWardenReplySound();
    }

    private void playWardenReplySound() {
        SoundEvent var0 = SOUND_BY_LEVEL.get(this.warningLevel);
        if (var0 != null) {
            BlockPos var1 = this.getBlockPos();
            int var2 = var1.getX() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
            int var3 = var1.getY() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
            int var4 = var1.getZ() + Mth.randomBetweenInclusive(this.level.random, -10, 10);
            this.level.playSound(null, (double)var2, (double)var3, (double)var4, var0, SoundSource.HOSTILE, 5.0F, 1.0F);
        }

    }

    private static void trySummonWarden(ServerLevel param0, BlockPos param1) {
        if (param0.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING)) {
            SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, param0, param1, 20, 5, 6)
                .ifPresent(param0x -> param0x.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F));
        }

    }

    @Override
    public void onSignalSchedule() {
        this.setChanged();
    }
}
