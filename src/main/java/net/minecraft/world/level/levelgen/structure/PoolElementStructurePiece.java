package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoolElementStructurePiece extends StructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureManager structureManager;

    public PoolElementStructurePiece(StructureManager param0, StructurePoolElement param1, BlockPos param2, int param3, Rotation param4, BoundingBox param5) {
        super(StructurePieceType.JIGSAW, 0);
        this.structureManager = param0;
        this.element = param1;
        this.position = param2;
        this.groundLevelDelta = param3;
        this.rotation = param4;
        this.boundingBox = param5;
    }

    public PoolElementStructurePiece(StructureManager param0, CompoundTag param1) {
        super(StructurePieceType.JIGSAW, param1);
        this.structureManager = param0;
        this.position = new BlockPos(param1.getInt("PosX"), param1.getInt("PosY"), param1.getInt("PosZ"));
        this.groundLevelDelta = param1.getInt("ground_level_delta");
        this.element = StructurePoolElement.CODEC
            .parse(NbtOps.INSTANCE, param1.getCompound("pool_element"))
            .resultOrPartial(LOGGER::error)
            .orElse(EmptyPoolElement.INSTANCE);
        this.rotation = Rotation.valueOf(param1.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(param0, this.position, this.rotation);
        ListTag var0 = param1.getList("junctions", 10);
        this.junctions.clear();
        var0.forEach(param0x -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(NbtOps.INSTANCE, param0x))));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        param0.putInt("PosX", this.position.getX());
        param0.putInt("PosY", this.position.getY());
        param0.putInt("PosZ", this.position.getZ());
        param0.putInt("ground_level_delta", this.groundLevelDelta);
        StructurePoolElement.CODEC
            .encodeStart(NbtOps.INSTANCE, this.element)
            .resultOrPartial(LOGGER::error)
            .ifPresent(param1 -> param0.put("pool_element", param1));
        param0.putString("rotation", this.rotation.name());
        ListTag var0 = new ListTag();

        for(JigsawJunction var1 : this.junctions) {
            var0.add(var1.serialize(NbtOps.INSTANCE).getValue());
        }

        param0.put("junctions", var0);
    }

    @Override
    public boolean postProcess(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, ChunkPos param5, BlockPos param6
    ) {
        return this.place(param0, param1, param2, param3, param4, param6, false);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BoundingBox param4, BlockPos param5, boolean param6
    ) {
        return this.element.place(this.structureManager, param0, param1, param2, this.position, param5, this.rotation, param4, param3, param6);
    }

    @Override
    public void move(int param0, int param1, int param2) {
        super.move(param0, param1, param2);
        this.position = this.position.offset(param0, param1, param2);
    }

    @Override
    public Rotation getRotation() {
        return this.rotation;
    }

    @Override
    public String toString() {
        return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
    }

    public StructurePoolElement getElement() {
        return this.element;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction param0) {
        this.junctions.add(param0);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }
}
