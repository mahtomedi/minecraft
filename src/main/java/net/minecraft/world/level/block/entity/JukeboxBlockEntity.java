package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem {
    private static final int SONG_END_PADDING = 20;
    private ItemStack item = ItemStack.EMPTY;
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
            this.item = ItemStack.of(param0.getCompound("RecordItem"));
        }

        this.isPlaying = param0.getBoolean("IsPlaying");
        this.recordStartedTick = param0.getLong("RecordStartTick");
        this.tickCount = param0.getLong("TickCount");
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (!this.getTheItem().isEmpty()) {
            param0.put("RecordItem", this.getTheItem().save(new CompoundTag()));
        }

        param0.putBoolean("IsPlaying", this.isPlaying);
        param0.putLong("RecordStartTick", this.recordStartedTick);
        param0.putLong("TickCount", this.tickCount);
    }

    public boolean isRecordPlaying() {
        return !this.getTheItem().isEmpty() && this.isPlaying;
    }

    private void setHasRecordBlockState(@Nullable Entity param0, boolean param1) {
        if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(param1)), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(param0, this.getBlockState()));
        }

    }

    @VisibleForTesting
    public void startPlaying() {
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(null, 1010, this.getBlockPos(), Item.getId(this.getTheItem().getItem()));
        this.setChanged();
    }

    private void stopPlaying() {
        this.isPlaying = false;
        this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(1011, this.getBlockPos(), 0);
        this.setChanged();
    }

    private void tick(Level param0, BlockPos param1, BlockState param2) {
        ++this.ticksSinceLastEvent;
        if (this.isRecordPlaying()) {
            Item var5 = this.getTheItem().getItem();
            if (var5 instanceof RecordItem var0) {
                if (this.shouldRecordStopPlaying(var0)) {
                    this.stopPlaying();
                } else if (this.shouldSendJukeboxPlayingEvent()) {
                    this.ticksSinceLastEvent = 0;
                    param0.gameEvent(GameEvent.JUKEBOX_PLAY, param1, GameEvent.Context.of(param2));
                    this.spawnMusicParticles(param0, param1);
                }
            }
        }

        ++this.tickCount;
    }

    private boolean shouldRecordStopPlaying(RecordItem param0) {
        return this.tickCount >= this.recordStartedTick + (long)param0.getLengthInTicks() + 20L;
    }

    private boolean shouldSendJukeboxPlayingEvent() {
        return this.ticksSinceLastEvent >= 20;
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int param0) {
        ItemStack var0 = this.item;
        this.item = ItemStack.EMPTY;
        if (!var0.isEmpty()) {
            this.setHasRecordBlockState(null, false);
            this.stopPlaying();
        }

        return var0;
    }

    @Override
    public void setTheItem(ItemStack param0) {
        if (param0.is(ItemTags.MUSIC_DISCS) && this.level != null) {
            this.item = param0;
            this.setHasRecordBlockState(null, true);
            this.startPlaying();
        } else if (param0.isEmpty()) {
            this.splitTheItem(1);
        }

    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        return param1.is(ItemTags.MUSIC_DISCS) && this.getItem(param0).isEmpty();
    }

    @Override
    public boolean canTakeItem(Container param0, int param1, ItemStack param2) {
        return param0.hasAnyMatching(ItemStack::isEmpty);
    }

    private void spawnMusicParticles(Level param0, BlockPos param1) {
        if (param0 instanceof ServerLevel var0) {
            Vec3 var1 = Vec3.atBottomCenterOf(param1).add(0.0, 1.2F, 0.0);
            float var2 = (float)param0.getRandom().nextInt(4) / 24.0F;
            var0.sendParticles(ParticleTypes.NOTE, var1.x(), var1.y(), var1.z(), 0, (double)var2, 0.0, 0.0, 1.0);
        }

    }

    public void popOutRecord() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPos var0 = this.getBlockPos();
            ItemStack var1 = this.getTheItem();
            if (!var1.isEmpty()) {
                this.removeTheItem();
                Vec3 var2 = Vec3.atLowerCornerWithOffset(var0, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7F);
                ItemStack var3 = var1.copy();
                ItemEntity var4 = new ItemEntity(this.level, var2.x(), var2.y(), var2.z(), var3);
                var4.setDefaultPickUpDelay();
                this.level.addFreshEntity(var4);
            }
        }
    }

    public static void playRecordTick(Level param0, BlockPos param1, BlockState param2, JukeboxBlockEntity param3) {
        param3.tick(param0, param1, param2);
    }

    @VisibleForTesting
    public void setRecordWithoutPlaying(ItemStack param0) {
        this.item = param0;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }
}
