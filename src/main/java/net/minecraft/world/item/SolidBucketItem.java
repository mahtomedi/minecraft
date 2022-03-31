package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class SolidBucketItem extends BlockItem implements DispensibleContainerItem {
    private final SoundEvent placeSound;

    public SolidBucketItem(Block param0, SoundEvent param1, Item.Properties param2) {
        super(param0, param2);
        this.placeSound = param1;
    }

    @Override
    public InteractionResult useOn(UseOnContext param0) {
        InteractionResult var0 = super.useOn(param0);
        Player var1 = param0.getPlayer();
        if (var0.consumesAction() && var1 != null && !var1.isCreative()) {
            InteractionHand var2 = param0.getHand();
            var1.setItemInHand(var2, Items.BUCKET.getDefaultInstance());
        }

        return var0;
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState param0) {
        return this.placeSound;
    }

    @Override
    public boolean emptyContents(@Nullable Player param0, Level param1, BlockPos param2, @Nullable BlockHitResult param3) {
        if (param1.isInWorldBounds(param2) && param1.isEmptyBlock(param2)) {
            if (!param1.isClientSide) {
                param1.setBlock(param2, this.getBlock().defaultBlockState(), 3);
            }

            param1.gameEvent(param0, GameEvent.FLUID_PLACE, param2);
            param1.playSound(param0, param2, this.placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}
