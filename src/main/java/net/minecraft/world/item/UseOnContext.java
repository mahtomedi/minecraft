package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class UseOnContext {
    protected final Player player;
    protected final InteractionHand hand;
    protected final BlockHitResult hitResult;
    protected final Level level;
    protected final ItemStack itemStack;

    public UseOnContext(Player param0, InteractionHand param1, BlockHitResult param2) {
        this(param0.level, param0, param1, param0.getItemInHand(param1), param2);
    }

    protected UseOnContext(Level param0, @Nullable Player param1, InteractionHand param2, ItemStack param3, BlockHitResult param4) {
        this.player = param1;
        this.hand = param2;
        this.hitResult = param4;
        this.itemStack = param3;
        this.level = param0;
    }

    public BlockPos getClickedPos() {
        return this.hitResult.getBlockPos();
    }

    public Direction getClickedFace() {
        return this.hitResult.getDirection();
    }

    public Vec3 getClickLocation() {
        return this.hitResult.getLocation();
    }

    public boolean isInside() {
        return this.hitResult.isInside();
    }

    public ItemStack getItemInHand() {
        return this.itemStack;
    }

    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public Level getLevel() {
        return this.level;
    }

    public Direction getHorizontalDirection() {
        return this.player == null ? Direction.NORTH : this.player.getDirection();
    }

    public boolean isSecondaryUseActive() {
        return this.player != null && this.player.isSecondaryUseActive();
    }

    public float getRotation() {
        return this.player == null ? 0.0F : this.player.yRot;
    }
}
