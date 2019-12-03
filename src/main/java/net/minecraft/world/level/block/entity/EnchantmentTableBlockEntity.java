package net.minecraft.world.level.block.entity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;

public class EnchantmentTableBlockEntity extends BlockEntity implements Nameable, TickableBlockEntity {
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

    public EnchantmentTableBlockEntity() {
        super(BlockEntityType.ENCHANTING_TABLE);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (this.hasCustomName()) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        return param0;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

    }

    @Override
    public void tick() {
        this.oOpen = this.open;
        this.oRot = this.rot;
        Player var0 = this.level
            .getNearestPlayer(
                (double)((float)this.worldPosition.getX() + 0.5F),
                (double)((float)this.worldPosition.getY() + 0.5F),
                (double)((float)this.worldPosition.getZ() + 0.5F),
                3.0,
                false
            );
        if (var0 != null) {
            double var1 = var0.getX() - ((double)this.worldPosition.getX() + 0.5);
            double var2 = var0.getZ() - ((double)this.worldPosition.getZ() + 0.5);
            this.tRot = (float)Mth.atan2(var2, var1);
            this.open += 0.1F;
            if (this.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float var3 = this.flipT;

                do {
                    this.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(var3 == this.flipT);
            }
        } else {
            this.tRot += 0.02F;
            this.open -= 0.1F;
        }

        while(this.rot >= (float) Math.PI) {
            this.rot -= (float) (Math.PI * 2);
        }

        while(this.rot < (float) -Math.PI) {
            this.rot += (float) (Math.PI * 2);
        }

        while(this.tRot >= (float) Math.PI) {
            this.tRot -= (float) (Math.PI * 2);
        }

        while(this.tRot < (float) -Math.PI) {
            this.tRot += (float) (Math.PI * 2);
        }

        float var4 = this.tRot - this.rot;

        while(var4 >= (float) Math.PI) {
            var4 -= (float) (Math.PI * 2);
        }

        while(var4 < (float) -Math.PI) {
            var4 += (float) (Math.PI * 2);
        }

        this.rot += var4 * 0.4F;
        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        ++this.time;
        this.oFlip = this.flip;
        float var5 = (this.flipT - this.flip) * 0.4F;
        float var6 = 0.2F;
        var5 = Mth.clamp(var5, -0.2F, 0.2F);
        this.flipA += (var5 - this.flipA) * 0.9F;
        this.flip += this.flipA;
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
