package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class JukeboxBlockEntity extends BlockEntity implements Clearable {
    private ItemStack record = ItemStack.EMPTY;
    private int ticksSinceLastEvent;
    private long tickCount;
    private long recordStartedTick;
    private boolean isPlaying;

    public JukeboxBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.JUKEBOX, param0, param1);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("RecordItem", 10)) {
            this.setRecord(ItemStack.of(param0.getCompound("RecordItem")));
        }

        this.isPlaying = param0.getBoolean("IsPlaying");
        this.recordStartedTick = param0.getLong("RecordStartTick");
        this.tickCount = param0.getLong("TickCount");
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (!this.getRecord().isEmpty()) {
            param0.put("RecordItem", this.getRecord().save(new CompoundTag()));
        }

        param0.putBoolean("IsPlaying", this.isPlaying);
        param0.putLong("RecordStartTick", this.recordStartedTick);
        param0.putLong("TickCount", this.tickCount);
    }

    public ItemStack getRecord() {
        return this.record;
    }

    public void setRecord(ItemStack param0) {
        this.record = param0;
        this.setChanged();
    }

    public void playRecord() {
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
    }

    @Override
    public void clearContent() {
        this.setRecord(ItemStack.EMPTY);
        this.isPlaying = false;
    }

    public static void playRecordTick(Level param0, BlockPos param1, BlockState param2, JukeboxBlockEntity param3) {
        ++param3.ticksSinceLastEvent;
        if (recordIsPlaying(param2, param3)) {
            Item var5 = param3.getRecord().getItem();
            if (var5 instanceof RecordItem var0) {
                if (recordShouldStopPlaying(param3, var0)) {
                    param0.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, param1, GameEvent.Context.of(param2));
                    param3.isPlaying = false;
                } else if (shouldSendJukeboxPlayingEvent(param3)) {
                    param3.ticksSinceLastEvent = 0;
                    param0.gameEvent(GameEvent.JUKEBOX_PLAY, param1, GameEvent.Context.of(param2));
                    spawnMusicParticles(param0, param1);
                }
            }
        }

        ++param3.tickCount;
    }

    private static void spawnMusicParticles(Level param0, BlockPos param1) {
        if (param0 instanceof ServerLevel var0) {
            Vec3 var1 = Vec3.atBottomCenterOf(param1).add(0.0, 1.2F, 0.0);
            float var2 = (float)param0.getRandom().nextInt(4) / 24.0F;
            var0.sendParticles(ParticleTypes.NOTE, var1.x(), var1.y(), var1.z(), 0, (double)var2, 0.0, 0.0, 1.0);
        }

    }

    private static boolean recordIsPlaying(BlockState param0, JukeboxBlockEntity param1) {
        return param0.getValue(JukeboxBlock.HAS_RECORD) && param1.isPlaying;
    }

    private static boolean recordShouldStopPlaying(JukeboxBlockEntity param0, RecordItem param1) {
        return param0.tickCount >= param0.recordStartedTick + (long)param1.getLengthInTicks();
    }

    private static boolean shouldSendJukeboxPlayingEvent(JukeboxBlockEntity param0) {
        return param0.ticksSinceLastEvent >= 20;
    }
}
