package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
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
        super(StructurePieceType.JIGSAW, 0, param5);
        this.structureManager = param0;
        this.element = param1;
        this.position = param2;
        this.groundLevelDelta = param3;
        this.rotation = param4;
    }

    public PoolElementStructurePiece(ServerLevel param0, CompoundTag param1) {
        super(StructurePieceType.JIGSAW, param1);
        this.structureManager = param0.getStructureManager();
        this.position = new BlockPos(param1.getInt("PosX"), param1.getInt("PosY"), param1.getInt("PosZ"));
        this.groundLevelDelta = param1.getInt("ground_level_delta");
        RegistryReadOps<Tag> var0 = RegistryReadOps.create(NbtOps.INSTANCE, param0.getServer().getResourceManager(), param0.getServer().registryAccess());
        this.element = StructurePoolElement.CODEC
            .parse(var0, param1.getCompound("pool_element"))
            .resultOrPartial(LOGGER::error)
            .orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
        this.rotation = Rotation.valueOf(param1.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
        ListTag var1 = param1.getList("junctions", 10);
        this.junctions.clear();
        var1.forEach(param1x -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(var0, param1x))));
    }

    @Override
    protected void addAdditionalSaveData(ServerLevel param0, CompoundTag param1) {
        param1.putInt("PosX", this.position.getX());
        param1.putInt("PosY", this.position.getY());
        param1.putInt("PosZ", this.position.getZ());
        param1.putInt("ground_level_delta", this.groundLevelDelta);
        RegistryWriteOps<Tag> var0 = RegistryWriteOps.create(NbtOps.INSTANCE, param0.getServer().registryAccess());
        StructurePoolElement.CODEC.encodeStart(var0, this.element).resultOrPartial(LOGGER::error).ifPresent(param1x -> param1.put("pool_element", param1x));
        param1.putString("rotation", this.rotation.name());
        ListTag var1 = new ListTag();

        for(JigsawJunction var2 : this.junctions) {
            var1.add(var2.serialize(var0).getValue());
        }

        param1.put("junctions", var1);
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
