package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WARNING_SOUND_RADIUS = 10;
    private static final int WARDEN_SPAWN_ATTEMPTS = 20;
    private static final int WARDEN_SPAWN_RANGE_XZ = 5;
    private static final int WARDEN_SPAWN_RANGE_Y = 6;
    private static final int DARKNESS_RADIUS = 40;
    private static final int SHRIEKING_TICKS = 90;
    private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), param0 -> {
        param0.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
        param0.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
        param0.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
        param0.put(4, SoundEvents.WARDEN_LISTENING_ANGRY);
    });
    private int warningLevel;
    private final VibrationSystem.User vibrationUser = new SculkShriekerBlockEntity.VibrationUser();
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

    public SculkShriekerBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.SCULK_SHRIEKER, param0, param1);
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("warning_level", 99)) {
            this.warningLevel = param0.getInt("warning_level");
        }

        if (param0.contains("listener", 10)) {
            VibrationSystem.Data.CODEC
                .parse(new Dynamic<>(NbtOps.INSTANCE, param0.getCompound("listener")))
                .resultOrPartial(LOGGER::error)
                .ifPresent(param0x -> this.vibrationData = param0x);
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putInt("warning_level", this.warningLevel);
        VibrationSystem.Data.CODEC
            .encodeStart(NbtOps.INSTANCE, this.vibrationData)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("listener", param1));
    }

    @Nullable
    public static ServerPlayer tryGetPlayer(@Nullable Entity param0) {
        if (param0 instanceof ServerPlayer var0) {
            return var0;
        } else {
            if (param0 != null) {
                LivingEntity var5 = param0.getControllingPassenger();
                if (var5 instanceof ServerPlayer var1) {
                    return var1;
                }
            }

            if (param0 instanceof Projectile var2) {
                Entity var3x = var2.getOwner();
                if (var3x instanceof ServerPlayer var3) {
                    return var3;
                }
            }

            if (param0 instanceof ItemEntity var4) {
                Entity var9 = var4.getOwner();
                if (var9 instanceof ServerPlayer var5) {
                    return var5;
                }
            }

            return null;
        }
    }

    public void tryShriek(ServerLevel param0, @Nullable ServerPlayer param1) {
        if (param1 != null) {
            BlockState var0 = this.getBlockState();
            if (!var0.getValue(SculkShriekerBlock.SHRIEKING)) {
                this.warningLevel = 0;
                if (!this.canRespond(param0) || this.tryToWarn(param0, param1)) {
                    this.shriek(param0, param1);
                }
            }
        }
    }

    private boolean tryToWarn(ServerLevel param0, ServerPlayer param1) {
        OptionalInt var0 = WardenSpawnTracker.tryWarn(param0, this.getBlockPos(), param1);
        var0.ifPresent(param0x -> this.warningLevel = param0x);
        return var0.isPresent();
    }

    private void shriek(ServerLevel param0, @Nullable Entity param1) {
        BlockPos var0 = this.getBlockPos();
        BlockState var1 = this.getBlockState();
        param0.setBlock(var0, var1.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
        param0.scheduleTick(var0, var1.getBlock(), 90);
        param0.levelEvent(3007, var0, 0);
        param0.gameEvent(GameEvent.SHRIEK, var0, GameEvent.Context.of(param1));
    }

    private boolean canRespond(ServerLevel param0) {
        return this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON)
            && param0.getDifficulty() != Difficulty.PEACEFUL
            && param0.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
    }

    public void tryRespond(ServerLevel param0) {
        if (this.canRespond(param0) && this.warningLevel > 0) {
            if (!this.trySummonWarden(param0)) {
                this.playWardenReplySound(param0);
            }

            Warden.applyDarknessAround(param0, Vec3.atCenterOf(this.getBlockPos()), null, 40);
        }

    }

    private void playWardenReplySound(Level param0) {
        SoundEvent var0 = SOUND_BY_LEVEL.get(this.warningLevel);
        if (var0 != null) {
            BlockPos var1 = this.getBlockPos();
            int var2 = var1.getX() + Mth.randomBetweenInclusive(param0.random, -10, 10);
            int var3 = var1.getY() + Mth.randomBetweenInclusive(param0.random, -10, 10);
            int var4 = var1.getZ() + Mth.randomBetweenInclusive(param0.random, -10, 10);
            param0.playSound(null, (double)var2, (double)var3, (double)var4, var0, SoundSource.HOSTILE, 5.0F, 1.0F);
        }

    }

    private boolean trySummonWarden(ServerLevel param0) {
        return this.warningLevel < 4
            ? false
            : SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, param0, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER)
                .isPresent();
    }

    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    class VibrationUser implements VibrationSystem.User {
        private static final int LISTENER_RADIUS = 8;
        private final PositionSource positionSource = new BlockPositionSource(SculkShriekerBlockEntity.this.worldPosition);

        public VibrationUser() {
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.SHRIEKER_CAN_LISTEN;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel param0, BlockPos param1, GameEvent param2, GameEvent.Context param3) {
            return !SculkShriekerBlockEntity.this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING)
                && SculkShriekerBlockEntity.tryGetPlayer(param3.sourceEntity()) != null;
        }

        @Override
        public void onReceiveVibration(ServerLevel param0, BlockPos param1, GameEvent param2, @Nullable Entity param3, @Nullable Entity param4, float param5) {
            SculkShriekerBlockEntity.this.tryShriek(param0, SculkShriekerBlockEntity.tryGetPlayer(param4 != null ? param4 : param3));
        }

        @Override
        public void onDataChanged() {
            SculkShriekerBlockEntity.this.setChanged();
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}
