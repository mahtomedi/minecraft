package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MinecartSpawner extends AbstractMinecart {
    private final BaseSpawner spawner = new BaseSpawner() {
        @Override
        public void broadcastEvent(int param0) {
            MinecartSpawner.this.level.broadcastEntityEvent(MinecartSpawner.this, (byte)param0);
        }

        @Override
        public Level getLevel() {
            return MinecartSpawner.this.level;
        }

        @Override
        public BlockPos getPos() {
            return MinecartSpawner.this.blockPosition();
        }
    };

    public MinecartSpawner(EntityType<? extends MinecartSpawner> param0, Level param1) {
        super(param0, param1);
    }

    public MinecartSpawner(Level param0, double param1, double param2, double param3) {
        super(EntityType.SPAWNER_MINECART, param0, param1, param2, param3);
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
        this.spawner.load(param0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.spawner.save(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        this.spawner.onEventTriggered(param0);
    }

    @Override
    public void tick() {
        super.tick();
        this.spawner.tick();
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }
}
