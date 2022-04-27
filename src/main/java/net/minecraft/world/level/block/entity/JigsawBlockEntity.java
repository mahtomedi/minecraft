package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class JigsawBlockEntity extends BlockEntity {
    public static final String TARGET = "target";
    public static final String POOL = "pool";
    public static final String JOINT = "joint";
    public static final String NAME = "name";
    public static final String FINAL_STATE = "final_state";
    private ResourceLocation name = new ResourceLocation("empty");
    private ResourceLocation target = new ResourceLocation("empty");
    private ResourceLocation pool = new ResourceLocation("empty");
    private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.JIGSAW, param0, param1);
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public ResourceLocation getTarget() {
        return this.target;
    }

    public ResourceLocation getPool() {
        return this.pool;
    }

    public String getFinalState() {
        return this.finalState;
    }

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
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.putString("name", this.name.toString());
        param0.putString("target", this.target.toString());
        param0.putString("pool", this.pool.toString());
        param0.putString("final_state", this.finalState);
        param0.putString("joint", this.joint.getSerializedName());
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.name = new ResourceLocation(param0.getString("name"));
        this.target = new ResourceLocation(param0.getString("target"));
        this.pool = new ResourceLocation(param0.getString("pool"));
        this.finalState = param0.getString("final_state");
        this.joint = JigsawBlockEntity.JointType.byName(param0.getString("joint"))
            .orElseGet(
                () -> JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal()
                        ? JigsawBlockEntity.JointType.ALIGNED
                        : JigsawBlockEntity.JointType.ROLLABLE
            );
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void generate(ServerLevel param0, int param1, boolean param2) {
        ChunkGenerator var0 = param0.getChunkSource().getGenerator();
        StructureTemplateManager var1 = param0.getStructureManager();
        StructureManager var2 = param0.structureManager();
        RandomSource var3 = param0.getRandom();
        Registry<StructureTemplatePool> var4 = param0.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        ResourceKey<StructureTemplatePool> var5 = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, this.pool);
        Holder<StructureTemplatePool> var6 = var4.getHolderOrThrow(var5);
        BlockPos var7 = this.getBlockPos().relative(this.getBlockState().getValue(JigsawBlock.ORIENTATION).front());
        Structure.GenerationContext var8 = new Structure.GenerationContext(
            param0.registryAccess(),
            var0,
            var0.getBiomeSource(),
            param0.getChunkSource().randomState(),
            var1,
            param0.getSeed(),
            new ChunkPos(var7),
            param0,
            param0x -> true
        );
        Optional<Structure.GenerationStub> var9 = JigsawPlacement.addPieces(var8, var6, Optional.of(this.target), param1, var7, false, Optional.empty(), 128);
        if (var9.isPresent()) {
            StructurePiecesBuilder var10 = new StructurePiecesBuilder();
            var9.get().generator().accept(var10);

            for(StructurePiece var11 : var10.build().pieces()) {
                if (var11 instanceof PoolElementStructurePiece var12) {
                    var12.place(param0, var2, var0, var3, BoundingBox.infinite(), var7, param2);
                }
            }
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

        public Component getTranslatedName() {
            return Component.translatable("jigsaw_block.joint." + this.name);
        }
    }
}
