package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartSpawner extends AbstractMinecart {
    private final BaseSpawner spawner = new BaseSpawner() {
        @Override
        public void broadcastEvent(Level param0, BlockPos param1, int param2) {
            param0.broadcastEntityEvent(MinecartSpawner.this, (byte)param2);
        }
    };
    private final Runnable ticker;

    public MinecartSpawner(EntityType<? extends MinecartSpawner> param0, Level param1) {
        super(param0, param1);
        this.ticker = this.createTicker(param1);
    }

    public MinecartSpawner(Level param0, double param1, double param2, double param3) {
        super(EntityType.SPAWNER_MINECART, param0, param1, param2, param3);
        this.ticker = this.createTicker(param0);
    }

    private Runnable createTicker(Level param0) {
        return param0 instanceof ServerLevel
            ? () -> this.spawner.serverTick((ServerLevel)param0, this.blockPosition())
            : () -> this.spawner.clientTick(param0, this.blockPosition());
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.SPAWNER;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.SPAWNER.defaultBlockState();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.spawner.load(this.level, this.blockPosition(), param0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.spawner.save(this.level, this.blockPosition(), param0);
    }

    @Override
    public void handleEntityEvent(byte param0) {
        this.spawner.onEventTriggered(this.level, param0);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticker.run();
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }
}
