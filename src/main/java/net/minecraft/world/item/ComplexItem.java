package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ComplexItem extends Item {
    public ComplexItem(Item.Properties param0) {
        super(param0);
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Nullable
    public Packet<?> getUpdatePacket(ItemStack param0, Level param1, Player param2) {
        return null;
    }
}
