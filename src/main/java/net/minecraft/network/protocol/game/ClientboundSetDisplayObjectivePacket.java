package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetDisplayObjectivePacket implements Packet<ClientGamePacketListener> {
    private int slot;
    private String objectiveName;

    public ClientboundSetDisplayObjectivePacket() {
    }

    public ClientboundSetDisplayObjectivePacket(int param0, @Nullable Objective param1) {
        this.slot = param0;
        if (param1 == null) {
            this.objectiveName = "";
        } else {
            this.objectiveName = param1.getName();
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.slot = param0.readByte();
        this.objectiveName = param0.readUtf(16);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.slot);
        param0.writeUtf(this.objectiveName);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetDisplayObjective(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getSlot() {
        return this.slot;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}
