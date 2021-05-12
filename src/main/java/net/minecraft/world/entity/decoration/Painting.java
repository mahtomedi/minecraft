package net.minecraft.world.entity.decoration;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class Painting extends HangingEntity {
    public Motive motive;

    public Painting(EntityType<? extends Painting> param0, Level param1) {
        super(param0, param1);
    }

    public Painting(Level param0, BlockPos param1, Direction param2) {
        super(EntityType.PAINTING, param0, param1);
        List<Motive> var0 = Lists.newArrayList();
        int var1 = 0;

        for(Motive var2 : Registry.MOTIVE) {
            this.motive = var2;
            this.setDirection(param2);
            if (this.survives()) {
                var0.add(var2);
                int var3 = var2.getWidth() * var2.getHeight();
                if (var3 > var1) {
                    var1 = var3;
                }
            }
        }

        if (!var0.isEmpty()) {
            Iterator<Motive> var4 = var0.iterator();

            while(var4.hasNext()) {
                Motive var5 = var4.next();
                if (var5.getWidth() * var5.getHeight() < var1) {
                    var4.remove();
                }
            }

            this.motive = var0.get(this.random.nextInt(var0.size()));
        }

        this.setDirection(param2);
    }

    public Painting(Level param0, BlockPos param1, Direction param2, Motive param3) {
        this(param0, param1, param2);
        this.motive = param3;
        this.setDirection(param2);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        param0.putString("Motive", Registry.MOTIVE.getKey(this.motive).toString());
        param0.putByte("Facing", (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(param0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.motive = Registry.MOTIVE.get(ResourceLocation.tryParse(param0.getString("Motive")));
        this.direction = Direction.from2DDataValue(param0.getByte("Facing"));
        super.readAdditionalSaveData(param0);
        this.setDirection(this.direction);
    }

    @Override
    public int getWidth() {
        return this.motive == null ? 1 : this.motive.getWidth();
    }

    @Override
    public int getHeight() {
        return this.motive == null ? 1 : this.motive.getHeight();
    }

    @Override
    public void dropItem(@Nullable Entity param0) {
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (param0 instanceof Player var0 && var0.getAbilities().instabuild) {
                return;
            }

            this.spawnAtLocation(Items.PAINTING);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double param0, double param1, double param2, float param3, float param4) {
        this.setPos(param0, param1, param2);
    }

    @Override
    public void lerpTo(double param0, double param1, double param2, float param3, float param4, int param5, boolean param6) {
        BlockPos var0 = this.pos.offset(param0 - this.getX(), param1 - this.getY(), param2 - this.getZ());
        this.setPos((double)var0.getX(), (double)var0.getY(), (double)var0.getZ());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddPaintingPacket(this);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}
