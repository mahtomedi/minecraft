package net.minecraft.world.item.context;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlaceContext extends UseOnContext {
    private final BlockPos relativePos;
    protected boolean replaceClicked = true;

    public BlockPlaceContext(Player param0, InteractionHand param1, ItemStack param2, BlockHitResult param3) {
        this(param0.level(), param0, param1, param2, param3);
    }

    public BlockPlaceContext(UseOnContext param0) {
        this(param0.getLevel(), param0.getPlayer(), param0.getHand(), param0.getItemInHand(), param0.getHitResult());
    }

    protected BlockPlaceContext(Level param0, @Nullable Player param1, InteractionHand param2, ItemStack param3, BlockHitResult param4) {
        super(param0, param1, param2, param3, param4);
        this.relativePos = param4.getBlockPos().relative(param4.getDirection());
        this.replaceClicked = param0.getBlockState(param4.getBlockPos()).canBeReplaced(this);
    }

    public static BlockPlaceContext at(BlockPlaceContext param0, BlockPos param1, Direction param2) {
        return new BlockPlaceContext(
            param0.getLevel(),
            param0.getPlayer(),
            param0.getHand(),
            param0.getItemInHand(),
            new BlockHitResult(
                new Vec3(
                    (double)param1.getX() + 0.5 + (double)param2.getStepX() * 0.5,
                    (double)param1.getY() + 0.5 + (double)param2.getStepY() * 0.5,
                    (double)param1.getZ() + 0.5 + (double)param2.getStepZ() * 0.5
                ),
                param2,
                param1,
                false
            )
        );
    }

    @Override
    public BlockPos getClickedPos() {
        return this.replaceClicked ? super.getClickedPos() : this.relativePos;
    }

    public boolean canPlace() {
        return this.replaceClicked || this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
    }

    public boolean replacingClickedOnBlock() {
        return this.replaceClicked;
    }

    public Direction getNearestLookingDirection() {
        return Direction.orderedByNearest(this.getPlayer())[0];
    }

    public Direction getNearestLookingVerticalDirection() {
        return Direction.getFacingAxis(this.getPlayer(), Direction.Axis.Y);
    }

    public Direction[] getNearestLookingDirections() {
        Direction[] var0 = Direction.orderedByNearest(this.getPlayer());
        if (this.replaceClicked) {
            return var0;
        } else {
            Direction var1 = this.getClickedFace();
            int var2 = 0;

            while(var2 < var0.length && var0[var2] != var1.getOpposite()) {
                ++var2;
            }

            if (var2 > 0) {
                System.arraycopy(var0, 0, var0, 1, var2);
                var0[0] = var1.getOpposite();
            }

            return var0;
        }
    }
}
