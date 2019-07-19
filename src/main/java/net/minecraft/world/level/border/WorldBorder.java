package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldBorder {
    private final List<BorderChangeListener> listeners = Lists.newArrayList();
    private double damagePerBlock = 0.2;
    private double damageSafeZone = 5.0;
    private int warningTime = 15;
    private int warningBlocks = 5;
    private double centerX;
    private double centerZ;
    private int absoluteMaxSize = 29999984;
    private WorldBorder.BorderExtent extent = new WorldBorder.StaticBorderExtent(6.0E7);

    public boolean isWithinBounds(BlockPos param0) {
        return (double)(param0.getX() + 1) > this.getMinX()
            && (double)param0.getX() < this.getMaxX()
            && (double)(param0.getZ() + 1) > this.getMinZ()
            && (double)param0.getZ() < this.getMaxZ();
    }

    public boolean isWithinBounds(ChunkPos param0) {
        return (double)param0.getMaxBlockX() > this.getMinX()
            && (double)param0.getMinBlockX() < this.getMaxX()
            && (double)param0.getMaxBlockZ() > this.getMinZ()
            && (double)param0.getMinBlockZ() < this.getMaxZ();
    }

    public boolean isWithinBounds(AABB param0) {
        return param0.maxX > this.getMinX() && param0.minX < this.getMaxX() && param0.maxZ > this.getMinZ() && param0.minZ < this.getMaxZ();
    }

    public double getDistanceToBorder(Entity param0) {
        return this.getDistanceToBorder(param0.x, param0.z);
    }

    public VoxelShape getCollisionShape() {
        return this.extent.getCollisionShape();
    }

    public double getDistanceToBorder(double param0, double param1) {
        double var0 = param1 - this.getMinZ();
        double var1 = this.getMaxZ() - param1;
        double var2 = param0 - this.getMinX();
        double var3 = this.getMaxX() - param0;
        double var4 = Math.min(var2, var3);
        var4 = Math.min(var4, var0);
        return Math.min(var4, var1);
    }

    @OnlyIn(Dist.CLIENT)
    public BorderStatus getStatus() {
        return this.extent.getStatus();
    }

    public double getMinX() {
        return this.extent.getMinX();
    }

    public double getMinZ() {
        return this.extent.getMinZ();
    }

    public double getMaxX() {
        return this.extent.getMaxX();
    }

    public double getMaxZ() {
        return this.extent.getMaxZ();
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double param0, double param1) {
        this.centerX = param0;
        this.centerZ = param1;
        this.extent.onCenterChange();

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderCenterSet(this, param0, param1);
        }

    }

    public double getSize() {
        return this.extent.getSize();
    }

    public long getLerpRemainingTime() {
        return this.extent.getLerpRemainingTime();
    }

    public double getLerpTarget() {
        return this.extent.getLerpTarget();
    }

    public void setSize(double param0) {
        this.extent = new WorldBorder.StaticBorderExtent(param0);

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderSizeSet(this, param0);
        }

    }

    public void lerpSizeBetween(double param0, double param1, long param2) {
        this.extent = (WorldBorder.BorderExtent)(param0 == param1
            ? new WorldBorder.StaticBorderExtent(param1)
            : new WorldBorder.MovingBorderExtent(param0, param1, param2));

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderSizeLerping(this, param0, param1, param2);
        }

    }

    protected List<BorderChangeListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(BorderChangeListener param0) {
        this.listeners.add(param0);
    }

    public void setAbsoluteMaxSize(int param0) {
        this.absoluteMaxSize = param0;
        this.extent.onAbsoluteMaxSizeChange();
    }

    public int getAbsoluteMaxSize() {
        return this.absoluteMaxSize;
    }

    public double getDamageSafeZone() {
        return this.damageSafeZone;
    }

    public void setDamageSafeZone(double param0) {
        this.damageSafeZone = param0;

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderSetDamageSafeZOne(this, param0);
        }

    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double param0) {
        this.damagePerBlock = param0;

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderSetDamagePerBlock(this, param0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public double getLerpSpeed() {
        return this.extent.getLerpSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int param0) {
        this.warningTime = param0;

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderSetWarningTime(this, param0);
        }

    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int param0) {
        this.warningBlocks = param0;

        for(BorderChangeListener var0 : this.getListeners()) {
            var0.onBorderSetWarningBlocks(this, param0);
        }

    }

    public void tick() {
        this.extent = this.extent.update();
    }

    public void saveWorldBorderData(LevelData param0) {
        param0.setBorderSize(this.getSize());
        param0.setBorderX(this.getCenterX());
        param0.setBorderZ(this.getCenterZ());
        param0.setBorderSafeZone(this.getDamageSafeZone());
        param0.setBorderDamagePerBlock(this.getDamagePerBlock());
        param0.setBorderWarningBlocks(this.getWarningBlocks());
        param0.setBorderWarningTime(this.getWarningTime());
        param0.setBorderSizeLerpTarget(this.getLerpTarget());
        param0.setBorderSizeLerpTime(this.getLerpRemainingTime());
    }

    public void readBorderData(LevelData param0) {
        this.setCenter(param0.getBorderX(), param0.getBorderZ());
        this.setDamagePerBlock(param0.getBorderDamagePerBlock());
        this.setDamageSafeZone(param0.getBorderSafeZone());
        this.setWarningBlocks(param0.getBorderWarningBlocks());
        this.setWarningTime(param0.getBorderWarningTime());
        if (param0.getBorderSizeLerpTime() > 0L) {
            this.lerpSizeBetween(param0.getBorderSize(), param0.getBorderSizeLerpTarget(), param0.getBorderSizeLerpTime());
        } else {
            this.setSize(param0.getBorderSize());
        }

    }

    interface BorderExtent {
        double getMinX();

        double getMaxX();

        double getMinZ();

        double getMaxZ();

        double getSize();

        @OnlyIn(Dist.CLIENT)
        double getLerpSpeed();

        long getLerpRemainingTime();

        double getLerpTarget();

        @OnlyIn(Dist.CLIENT)
        BorderStatus getStatus();

        void onAbsoluteMaxSizeChange();

        void onCenterChange();

        WorldBorder.BorderExtent update();

        VoxelShape getCollisionShape();
    }

    class MovingBorderExtent implements WorldBorder.BorderExtent {
        private final double from;
        private final double to;
        private final long lerpEnd;
        private final long lerpBegin;
        private final double lerpDuration;

        private MovingBorderExtent(double param0, double param1, long param2) {
            this.from = param0;
            this.to = param1;
            this.lerpDuration = (double)param2;
            this.lerpBegin = Util.getMillis();
            this.lerpEnd = this.lerpBegin + param2;
        }

        @Override
        public double getMinX() {
            return Math.max(WorldBorder.this.getCenterX() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
        }

        @Override
        public double getMinZ() {
            return Math.max(WorldBorder.this.getCenterZ() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
        }

        @Override
        public double getMaxX() {
            return Math.min(WorldBorder.this.getCenterX() + this.getSize() / 2.0, (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxZ() {
            return Math.min(WorldBorder.this.getCenterZ() + this.getSize() / 2.0, (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getSize() {
            double var0 = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
            return var0 < 1.0 ? Mth.lerp(var0, this.from, this.to) : this.to;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public double getLerpSpeed() {
            return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
        }

        @Override
        public long getLerpRemainingTime() {
            return this.lerpEnd - Util.getMillis();
        }

        @Override
        public double getLerpTarget() {
            return this.to;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public BorderStatus getStatus() {
            return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
        }

        @Override
        public void onCenterChange() {
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
        }

        @Override
        public WorldBorder.BorderExtent update() {
            return (WorldBorder.BorderExtent)(this.getLerpRemainingTime() <= 0L ? WorldBorder.this.new StaticBorderExtent(this.to) : this);
        }

        @Override
        public VoxelShape getCollisionShape() {
            return Shapes.join(
                Shapes.INFINITY,
                Shapes.box(
                    Math.floor(this.getMinX()),
                    Double.NEGATIVE_INFINITY,
                    Math.floor(this.getMinZ()),
                    Math.ceil(this.getMaxX()),
                    Double.POSITIVE_INFINITY,
                    Math.ceil(this.getMaxZ())
                ),
                BooleanOp.ONLY_FIRST
            );
        }
    }

    class StaticBorderExtent implements WorldBorder.BorderExtent {
        private final double size;
        private double minX;
        private double minZ;
        private double maxX;
        private double maxZ;
        private VoxelShape shape;

        public StaticBorderExtent(double param0) {
            this.size = param0;
            this.updateBox();
        }

        @Override
        public double getMinX() {
            return this.minX;
        }

        @Override
        public double getMaxX() {
            return this.maxX;
        }

        @Override
        public double getMinZ() {
            return this.minZ;
        }

        @Override
        public double getMaxZ() {
            return this.maxZ;
        }

        @Override
        public double getSize() {
            return this.size;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public BorderStatus getStatus() {
            return BorderStatus.STATIONARY;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public double getLerpSpeed() {
            return 0.0;
        }

        @Override
        public long getLerpRemainingTime() {
            return 0L;
        }

        @Override
        public double getLerpTarget() {
            return this.size;
        }

        private void updateBox() {
            this.minX = Math.max(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
            this.minZ = Math.max(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize));
            this.maxX = Math.min(WorldBorder.this.getCenterX() + this.size / 2.0, (double)WorldBorder.this.absoluteMaxSize);
            this.maxZ = Math.min(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)WorldBorder.this.absoluteMaxSize);
            this.shape = Shapes.join(
                Shapes.INFINITY,
                Shapes.box(
                    Math.floor(this.getMinX()),
                    Double.NEGATIVE_INFINITY,
                    Math.floor(this.getMinZ()),
                    Math.ceil(this.getMaxX()),
                    Double.POSITIVE_INFINITY,
                    Math.ceil(this.getMaxZ())
                ),
                BooleanOp.ONLY_FIRST
            );
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
            this.updateBox();
        }

        @Override
        public void onCenterChange() {
            this.updateBox();
        }

        @Override
        public WorldBorder.BorderExtent update() {
            return this;
        }

        @Override
        public VoxelShape getCollisionShape() {
            return this.shape;
        }
    }
}
