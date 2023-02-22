package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder {
    public static final double MAX_SIZE = 5.9999968E7;
    public static final double MAX_CENTER_COORDINATE = 2.9999984E7;
    private final List<BorderChangeListener> listeners = Lists.newArrayList();
    private double damagePerBlock = 0.2;
    private double damageSafeZone = 5.0;
    private int warningTime = 15;
    private int warningBlocks = 5;
    private double centerX;
    private double centerZ;
    int absoluteMaxSize = 29999984;
    private WorldBorder.BorderExtent extent = new WorldBorder.StaticBorderExtent(5.9999968E7);
    public static final WorldBorder.Settings DEFAULT_SETTINGS = new WorldBorder.Settings(0.0, 0.0, 0.2, 5.0, 5, 15, 5.9999968E7, 0L, 0.0);

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

    public boolean isWithinBounds(double param0, double param1) {
        return param0 > this.getMinX() && param0 < this.getMaxX() && param1 > this.getMinZ() && param1 < this.getMaxZ();
    }

    public boolean isWithinBounds(double param0, double param1, double param2) {
        return param0 > this.getMinX() - param2 && param0 < this.getMaxX() + param2 && param1 > this.getMinZ() - param2 && param1 < this.getMaxZ() + param2;
    }

    public boolean isWithinBounds(AABB param0) {
        return param0.maxX > this.getMinX() && param0.minX < this.getMaxX() && param0.maxZ > this.getMinZ() && param0.minZ < this.getMaxZ();
    }

    public BlockPos clampToBounds(double param0, double param1, double param2) {
        return BlockPos.containing(Mth.clamp(param0, this.getMinX(), this.getMaxX()), param1, Mth.clamp(param2, this.getMinZ(), this.getMaxZ()));
    }

    public double getDistanceToBorder(Entity param0) {
        return this.getDistanceToBorder(param0.getX(), param0.getZ());
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

    public boolean isInsideCloseToBorder(Entity param0, AABB param1) {
        double var0 = Math.max(Mth.absMax(param1.getXsize(), param1.getZsize()), 1.0);
        return this.getDistanceToBorder(param0) < var0 * 2.0 && this.isWithinBounds(param0.getX(), param0.getZ(), var0);
    }

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

    public void removeListener(BorderChangeListener param0) {
        this.listeners.remove(param0);
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

    public WorldBorder.Settings createSettings() {
        return new WorldBorder.Settings(this);
    }

    public void applySettings(WorldBorder.Settings param0) {
        this.setCenter(param0.getCenterX(), param0.getCenterZ());
        this.setDamagePerBlock(param0.getDamagePerBlock());
        this.setDamageSafeZone(param0.getSafeZone());
        this.setWarningBlocks(param0.getWarningBlocks());
        this.setWarningTime(param0.getWarningTime());
        if (param0.getSizeLerpTime() > 0L) {
            this.lerpSizeBetween(param0.getSize(), param0.getSizeLerpTarget(), param0.getSizeLerpTime());
        } else {
            this.setSize(param0.getSize());
        }

    }

    interface BorderExtent {
        double getMinX();

        double getMaxX();

        double getMinZ();

        double getMaxZ();

        double getSize();

        double getLerpSpeed();

        long getLerpRemainingTime();

        double getLerpTarget();

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

        MovingBorderExtent(double param0, double param1, long param2) {
            this.from = param0;
            this.to = param1;
            this.lerpDuration = (double)param2;
            this.lerpBegin = Util.getMillis();
            this.lerpEnd = this.lerpBegin + param2;
        }

        @Override
        public double getMinX() {
            return Mth.clamp(
                WorldBorder.this.getCenterX() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
        }

        @Override
        public double getMinZ() {
            return Mth.clamp(
                WorldBorder.this.getCenterZ() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
        }

        @Override
        public double getMaxX() {
            return Mth.clamp(
                WorldBorder.this.getCenterX() + this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
        }

        @Override
        public double getMaxZ() {
            return Mth.clamp(
                WorldBorder.this.getCenterZ() + this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
        }

        @Override
        public double getSize() {
            double var0 = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
            return var0 < 1.0 ? Mth.lerp(var0, this.from, this.to) : this.to;
        }

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

    public static class Settings {
        private final double centerX;
        private final double centerZ;
        private final double damagePerBlock;
        private final double safeZone;
        private final int warningBlocks;
        private final int warningTime;
        private final double size;
        private final long sizeLerpTime;
        private final double sizeLerpTarget;

        Settings(double param0, double param1, double param2, double param3, int param4, int param5, double param6, long param7, double param8) {
            this.centerX = param0;
            this.centerZ = param1;
            this.damagePerBlock = param2;
            this.safeZone = param3;
            this.warningBlocks = param4;
            this.warningTime = param5;
            this.size = param6;
            this.sizeLerpTime = param7;
            this.sizeLerpTarget = param8;
        }

        Settings(WorldBorder param0) {
            this.centerX = param0.getCenterX();
            this.centerZ = param0.getCenterZ();
            this.damagePerBlock = param0.getDamagePerBlock();
            this.safeZone = param0.getDamageSafeZone();
            this.warningBlocks = param0.getWarningBlocks();
            this.warningTime = param0.getWarningTime();
            this.size = param0.getSize();
            this.sizeLerpTime = param0.getLerpRemainingTime();
            this.sizeLerpTarget = param0.getLerpTarget();
        }

        public double getCenterX() {
            return this.centerX;
        }

        public double getCenterZ() {
            return this.centerZ;
        }

        public double getDamagePerBlock() {
            return this.damagePerBlock;
        }

        public double getSafeZone() {
            return this.safeZone;
        }

        public int getWarningBlocks() {
            return this.warningBlocks;
        }

        public int getWarningTime() {
            return this.warningTime;
        }

        public double getSize() {
            return this.size;
        }

        public long getSizeLerpTime() {
            return this.sizeLerpTime;
        }

        public double getSizeLerpTarget() {
            return this.sizeLerpTarget;
        }

        public static WorldBorder.Settings read(DynamicLike<?> param0, WorldBorder.Settings param1) {
            double var0 = Mth.clamp(param0.get("BorderCenterX").asDouble(param1.centerX), -2.9999984E7, 2.9999984E7);
            double var1 = Mth.clamp(param0.get("BorderCenterZ").asDouble(param1.centerZ), -2.9999984E7, 2.9999984E7);
            double var2 = param0.get("BorderSize").asDouble(param1.size);
            long var3 = param0.get("BorderSizeLerpTime").asLong(param1.sizeLerpTime);
            double var4 = param0.get("BorderSizeLerpTarget").asDouble(param1.sizeLerpTarget);
            double var5 = param0.get("BorderSafeZone").asDouble(param1.safeZone);
            double var6 = param0.get("BorderDamagePerBlock").asDouble(param1.damagePerBlock);
            int var7 = param0.get("BorderWarningBlocks").asInt(param1.warningBlocks);
            int var8 = param0.get("BorderWarningTime").asInt(param1.warningTime);
            return new WorldBorder.Settings(var0, var1, var6, var5, var7, var8, var2, var3, var4);
        }

        public void write(CompoundTag param0) {
            param0.putDouble("BorderCenterX", this.centerX);
            param0.putDouble("BorderCenterZ", this.centerZ);
            param0.putDouble("BorderSize", this.size);
            param0.putLong("BorderSizeLerpTime", this.sizeLerpTime);
            param0.putDouble("BorderSafeZone", this.safeZone);
            param0.putDouble("BorderDamagePerBlock", this.damagePerBlock);
            param0.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
            param0.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
            param0.putDouble("BorderWarningTime", (double)this.warningTime);
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

        @Override
        public BorderStatus getStatus() {
            return BorderStatus.STATIONARY;
        }

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
            this.minX = Mth.clamp(
                WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
            this.minZ = Mth.clamp(
                WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
            this.maxX = Mth.clamp(
                WorldBorder.this.getCenterX() + this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
            this.maxZ = Mth.clamp(
                WorldBorder.this.getCenterZ() + this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize
            );
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
