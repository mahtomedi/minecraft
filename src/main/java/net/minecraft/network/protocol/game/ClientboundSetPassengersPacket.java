package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener> {
    private int vehicle;
    private int[] passengers;

    public ClientboundSetPassengersPacket() {
    }

    public ClientboundSetPassengersPacket(Entity param0) {
        this.vehicle = param0.getId();
        List<Entity> var0 = param0.getPassengers();
        this.passengers = new int[var0.size()];

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            this.passengers[var1] = var0.get(var1).getId();
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.vehicle = param0.readVarInt();
        this.passengers = param0.readVarIntArray();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.vehicle);
        param0.writeVarIntArray(this.passengers);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetEntityPassengersPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int[] getPassengers() {
        return this.passengers;
    }

    @OnlyIn(Dist.CLIENT)
    public int getVehicle() {
        return this.vehicle;
    }
}
