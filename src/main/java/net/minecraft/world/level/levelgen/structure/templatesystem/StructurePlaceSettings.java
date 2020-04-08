package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructurePlaceSettings {
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private BlockPos rotationPivot = BlockPos.ZERO;
    private boolean ignoreEntities;
    @Nullable
    private ChunkPos chunkPos;
    @Nullable
    private BoundingBox boundingBox;
    private boolean keepLiquids = true;
    @Nullable
    private Random random;
    @Nullable
    private int palette;
    private final List<StructureProcessor> processors = Lists.newArrayList();
    private boolean knownShape;

    public StructurePlaceSettings copy() {
        StructurePlaceSettings var0 = new StructurePlaceSettings();
        var0.mirror = this.mirror;
        var0.rotation = this.rotation;
        var0.rotationPivot = this.rotationPivot;
        var0.ignoreEntities = this.ignoreEntities;
        var0.chunkPos = this.chunkPos;
        var0.boundingBox = this.boundingBox;
        var0.keepLiquids = this.keepLiquids;
        var0.random = this.random;
        var0.palette = this.palette;
        var0.processors.addAll(this.processors);
        var0.knownShape = this.knownShape;
        return var0;
    }

    public StructurePlaceSettings setMirror(Mirror param0) {
        this.mirror = param0;
        return this;
    }

    public StructurePlaceSettings setRotation(Rotation param0) {
        this.rotation = param0;
        return this;
    }

    public StructurePlaceSettings setRotationPivot(BlockPos param0) {
        this.rotationPivot = param0;
        return this;
    }

    public StructurePlaceSettings setIgnoreEntities(boolean param0) {
        this.ignoreEntities = param0;
        return this;
    }

    public StructurePlaceSettings setChunkPos(ChunkPos param0) {
        this.chunkPos = param0;
        return this;
    }

    public StructurePlaceSettings setBoundingBox(BoundingBox param0) {
        this.boundingBox = param0;
        return this;
    }

    public StructurePlaceSettings setRandom(@Nullable Random param0) {
        this.random = param0;
        return this;
    }

    public StructurePlaceSettings setKnownShape(boolean param0) {
        this.knownShape = param0;
        return this;
    }

    public StructurePlaceSettings clearProcessors() {
        this.processors.clear();
        return this;
    }

    public StructurePlaceSettings addProcessor(StructureProcessor param0) {
        this.processors.add(param0);
        return this;
    }

    public StructurePlaceSettings popProcessor(StructureProcessor param0) {
        this.processors.remove(param0);
        return this;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public BlockPos getRotationPivot() {
        return this.rotationPivot;
    }

    public Random getRandom(@Nullable BlockPos param0) {
        if (this.random != null) {
            return this.random;
        } else {
            return param0 == null ? new Random(Util.getMillis()) : new Random(Mth.getSeed(param0));
        }
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    @Nullable
    public BoundingBox getBoundingBox() {
        if (this.boundingBox == null && this.chunkPos != null) {
            this.updateBoundingBoxFromChunkPos();
        }

        return this.boundingBox;
    }

    public boolean getKnownShape() {
        return this.knownShape;
    }

    public List<StructureProcessor> getProcessors() {
        return this.processors;
    }

    void updateBoundingBoxFromChunkPos() {
        if (this.chunkPos != null) {
            this.boundingBox = this.calculateBoundingBox(this.chunkPos);
        }

    }

    public boolean shouldKeepLiquids() {
        return this.keepLiquids;
    }

    public StructureTemplate.Palette getRandomPalette(List<StructureTemplate.Palette> param0, @Nullable BlockPos param1) {
        int var0 = param0.size();
        if (var0 == 0) {
            throw new IllegalStateException("No palettes");
        } else {
            return param0.get(this.getRandom(param1).nextInt(var0));
        }
    }

    @Nullable
    private BoundingBox calculateBoundingBox(@Nullable ChunkPos param0) {
        if (param0 == null) {
            return this.boundingBox;
        } else {
            int var0 = param0.x * 16;
            int var1 = param0.z * 16;
            return new BoundingBox(var0, 0, var1, var0 + 16 - 1, 255, var1 + 16 - 1);
        }
    }
}
