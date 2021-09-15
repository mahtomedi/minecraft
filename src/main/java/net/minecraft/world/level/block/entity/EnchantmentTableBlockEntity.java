package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final Random RANDOM = new Random();
    private Component name;

    public EnchantmentTableBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.ENCHANTING_TABLE, param0, param1);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (this.hasCustomName()) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

    }

    public static void bookAnimationTick(Level param0, BlockPos param1, BlockState param2, EnchantmentTableBlockEntity param3) {
        param3.oOpen = param3.open;
        param3.oRot = param3.rot;
        Player var0 = param0.getNearestPlayer((double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, 3.0, false);
        if (var0 != null) {
            double var1 = var0.getX() - ((double)param1.getX() + 0.5);
            double var2 = var0.getZ() - ((double)param1.getZ() + 0.5);
            param3.tRot = (float)Mth.atan2(var2, var1);
            param3.open += 0.1F;
            if (param3.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float var3 = param3.flipT;

                do {
                    param3.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(var3 == param3.flipT);
            }
        } else {
            param3.tRot += 0.02F;
            param3.open -= 0.1F;
        }

        while(param3.rot >= (float) Math.PI) {
            param3.rot -= (float) (Math.PI * 2);
        }

        while(param3.rot < (float) -Math.PI) {
            param3.rot += (float) (Math.PI * 2);
        }

        while(param3.tRot >= (float) Math.PI) {
            param3.tRot -= (float) (Math.PI * 2);
        }

        while(param3.tRot < (float) -Math.PI) {
            param3.tRot += (float) (Math.PI * 2);
        }

        float var4 = param3.tRot - param3.rot;

        while(var4 >= (float) Math.PI) {
            var4 -= (float) (Math.PI * 2);
        }

        while(var4 < (float) -Math.PI) {
            var4 += (float) (Math.PI * 2);
        }

        param3.rot += var4 * 0.4F;
        param3.open = Mth.clamp(param3.open, 0.0F, 1.0F);
        ++param3.time;
        param3.oFlip = param3.flip;
        float var5 = (param3.flipT - param3.flip) * 0.4F;
        float var6 = 0.2F;
        var5 = Mth.clamp(var5, -0.2F, 0.2F);
        param3.flipA += (var5 - param3.flipA) * 0.9F;
        param3.flip += param3.flipA;
    }

    @Override
    public Component getName() {
        return (Component)(this.name != null ? this.name : new TranslatableComponent("container.enchant"));
    }

    public void setCustomName(@Nullable Component param0) {
        this.name = param0;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }
}
