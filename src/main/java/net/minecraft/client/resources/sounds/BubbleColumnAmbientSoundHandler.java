package net.minecraft.client.resources.sounds;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BubbleColumnAmbientSoundHandler implements AmbientSoundHandler {
    private final LocalPlayer player;
    private boolean wasInBubbleColumn;
    private boolean firstTick = true;

    public BubbleColumnAmbientSoundHandler(LocalPlayer param0) {
        this.player = param0;
    }

    @Override
    public void tick() {
        Level var0 = this.player.level();
        BlockState var1 = var0.getBlockStatesIfLoaded(this.player.getBoundingBox().inflate(0.0, -0.4F, 0.0).deflate(1.0E-6))
            .filter(param0 -> param0.is(Blocks.BUBBLE_COLUMN))
            .findFirst()
            .orElse(null);
        if (var1 != null) {
            if (!this.wasInBubbleColumn && !this.firstTick && var1.is(Blocks.BUBBLE_COLUMN) && !this.player.isSpectator()) {
                boolean var2 = var1.getValue(BubbleColumnBlock.DRAG_DOWN);
                if (var2) {
                    this.player.playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0F, 1.0F);
                } else {
                    this.player.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
                }
            }

            this.wasInBubbleColumn = true;
        } else {
            this.wasInBubbleColumn = false;
        }

        this.firstTick = false;
    }
}
