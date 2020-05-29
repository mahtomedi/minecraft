package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class JigsawBlockEntity extends BlockEntity {
    private ResourceLocation name = new ResourceLocation("empty");
    private ResourceLocation target = new ResourceLocation("empty");
    private ResourceLocation pool = new ResourceLocation("empty");
    private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public JigsawBlockEntity() {
        this(BlockEntityType.JIGSAW);
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getName() {
        return this.name;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTarget() {
        return this.target;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getPool() {
        return this.pool;
    }

    @OnlyIn(Dist.CLIENT)
    public String getFinalState() {
        return this.finalState;
    }

    @OnlyIn(Dist.CLIENT)
    public JigsawBlockEntity.JointType getJoint() {
        return this.joint;
    }

    public void setName(ResourceLocation param0) {
        this.name = param0;
    }

    public void setTarget(ResourceLocation param0) {
        this.target = param0;
    }

    public void setPool(ResourceLocation param0) {
        this.pool = param0;
    }

    public void setFinalState(String param0) {
        this.finalState = param0;
    }

    public void setJoint(JigsawBlockEntity.JointType param0) {
        this.joint = param0;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putString("name", this.name.toString());
        param0.putString("target", this.target.toString());
        param0.putString("pool", this.pool.toString());
        param0.putString("final_state", this.finalState);
        param0.putString("joint", this.joint.getSerializedName());
        return param0;
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.name = new ResourceLocation(param1.getString("name"));
        this.target = new ResourceLocation(param1.getString("target"));
        this.pool = new ResourceLocation(param1.getString("pool"));
        this.finalState = param1.getString("final_state");
        this.joint = JigsawBlockEntity.JointType.byName(param1.getString("joint"))
            .orElseGet(
                () -> JigsawBlock.getFrontFacing(param0).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE
            );
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 12, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public void generate(ServerLevel param0, int param1, boolean param2) {
        ChunkGenerator var0 = param0.getChunkSource().getGenerator();
        StructureManager var1 = param0.getStructureManager();
        StructureFeatureManager var2 = param0.structureFeatureManager();
        Random var3 = param0.getRandom();
        BlockPos var4 = this.getBlockPos();
        List<PoolElementStructurePiece> var5 = Lists.newArrayList();
        StructureTemplate var6 = new StructureTemplate();
        var6.fillFromWorld(param0, var4, new BlockPos(1, 1, 1), false, null);
        StructurePoolElement var7 = new SinglePoolElement(var6, ImmutableList.of(), StructureTemplatePool.Projection.RIGID);
        JigsawBlockEntity.RuntimePiece var8 = new JigsawBlockEntity.RuntimePiece(var1, var7, var4, 1, Rotation.NONE, new BoundingBox(var4, var4));
        JigsawPlacement.addPieces(var8, param1, JigsawBlockEntity.RuntimePiece::new, var0, var1, var5, var3);

        for(PoolElementStructurePiece var9 : var5) {
            var9.place(param0, var2, var0, var3, BoundingBox.infinite(), var4, param2);
        }

    }

    public static enum JointType implements StringRepresentable {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private final String name;

        private JointType(String param0) {
            this.name = param0;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Optional<JigsawBlockEntity.JointType> byName(String param0) {
            return Arrays.stream(values()).filter(param1 -> param1.getSerializedName().equals(param0)).findFirst();
        }
    }

    public static final class RuntimePiece extends PoolElementStructurePiece {
        public RuntimePiece(StructureManager param0, StructurePoolElement param1, BlockPos param2, int param3, Rotation param4, BoundingBox param5) {
            super(StructurePieceType.RUNTIME, param0, param1, param2, param3, param4, param5);
        }

        public RuntimePiece(StructureManager param0, CompoundTag param1) {
            super(param0, param1, StructurePieceType.RUNTIME);
        }
    }
}
