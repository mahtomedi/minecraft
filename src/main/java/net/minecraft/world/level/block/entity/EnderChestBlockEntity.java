package net.minecraft.world.level.block.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
    value = Dist.CLIENT,
    _interface = LidBlockEntity.class
)
public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity, TickableBlockEntity {
    public float openness;
    public float oOpenness;
    public int openCount;
    private int tickInterval;

    public EnderChestBlockEntity() {
        super(BlockEntityType.ENDER_CHEST);
    }

    @Override
    public void tick() {
        if (++this.tickInterval % 20 * 4 == 0) {
            this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
        }

        this.oOpenness = this.openness;
        int var0 = this.worldPosition.getX();
        int var1 = this.worldPosition.getY();
        int var2 = this.worldPosition.getZ();
        float var3 = 0.1F;
        if (this.openCount > 0 && this.openness == 0.0F) {
            double var4 = (double)var0 + 0.5;
            double var5 = (double)var2 + 0.5;
            this.level
                .playSound(
                    null, var4, (double)var1 + 0.5, var5, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F
                );
        }

        if (this.openCount == 0 && this.openness > 0.0F || this.openCount > 0 && this.openness < 1.0F) {
            float var6 = this.openness;
            if (this.openCount > 0) {
                this.openness += 0.1F;
            } else {
                this.openness -= 0.1F;
            }

            if (this.openness > 1.0F) {
                this.openness = 1.0F;
            }

            float var7 = 0.5F;
            if (this.openness < 0.5F && var6 >= 0.5F) {
                double var8 = (double)var0 + 0.5;
                double var9 = (double)var2 + 0.5;
                this.level
                    .playSound(
                        null,
                        var8,
                        (double)var1 + 0.5,
                        var9,
                        SoundEvents.ENDER_CHEST_CLOSE,
                        SoundSource.BLOCKS,
                        0.5F,
                        this.level.random.nextFloat() * 0.1F + 0.9F
                    );
            }

            if (this.openness < 0.0F) {
                this.openness = 0.0F;
            }
        }

    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.openCount = param1;
            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    @Override
    public void setRemoved() {
        this.clearCache();
        super.setRemoved();
    }

    public void startOpen() {
        ++this.openCount;
        this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
    }

    public void stopOpen() {
        --this.openCount;
        this.level.blockEvent(this.worldPosition, Blocks.ENDER_CHEST, 1, this.openCount);
    }

    public boolean stillValid(Player param0) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(
                param0.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5)
                    > 64.0
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getOpenNess(float param0) {
        return Mth.lerp(param0, this.oOpenness, this.openness);
    }
}
