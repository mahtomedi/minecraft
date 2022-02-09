package net.minecraft.world.level;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClipContext {
    private final Vec3 from;
    private final Vec3 to;
    private final ClipContext.Block block;
    private final ClipContext.Fluid fluid;
    private final CollisionContext collisionContext;

    public ClipContext(Vec3 param0, Vec3 param1, ClipContext.Block param2, ClipContext.Fluid param3, Entity param4) {
        this.from = param0;
        this.to = param1;
        this.block = param2;
        this.fluid = param3;
        this.collisionContext = CollisionContext.of(param4);
    }

    public Vec3 getTo() {
        return this.to;
    }

    public Vec3 getFrom() {
        return this.from;
    }

    public VoxelShape getBlockShape(BlockState param0, BlockGetter param1, BlockPos param2) {
        return this.block.get(param0, param1, param2, this.collisionContext);
    }

    public VoxelShape getFluidShape(FluidState param0, BlockGetter param1, BlockPos param2) {
        return this.fluid.canPick(param0) ? param0.getShape(param1, param2) : Shapes.empty();
    }

    public static enum Block implements ClipContext.ShapeGetter {
        COLLIDER(BlockBehaviour.BlockStateBase::getCollisionShape),
        OUTLINE(BlockBehaviour.BlockStateBase::getShape),
        VISUAL(BlockBehaviour.BlockStateBase::getVisualShape),
        FALLDAMAGE_RESETTING((param0, param1, param2, param3) -> param0.is(BlockTags.FALL_DAMAGE_RESETTING) ? Shapes.block() : Shapes.empty());

        private final ClipContext.ShapeGetter shapeGetter;

        private Block(ClipContext.ShapeGetter param0) {
            this.shapeGetter = param0;
        }

        @Override
        public VoxelShape get(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
            return this.shapeGetter.get(param0, param1, param2, param3);
        }
    }

    public static enum Fluid {
        NONE(param0 -> false),
        SOURCE_ONLY(FluidState::isSource),
        ANY(param0 -> !param0.isEmpty()),
        WATER(param0 -> param0.is(FluidTags.WATER));

        private final Predicate<FluidState> canPick;

        private Fluid(Predicate<FluidState> param0) {
            this.canPick = param0;
        }

        public boolean canPick(FluidState param0) {
            return this.canPick.test(param0);
        }
    }

    public interface ShapeGetter {
        VoxelShape get(BlockState var1, BlockGetter var2, BlockPos var3, CollisionContext var4);
    }
}
