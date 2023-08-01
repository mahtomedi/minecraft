package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

public class ClientboundSetDisplayObjectivePacket implements Packet<ClientGamePacketListener> {
    private final DisplaySlot slot;
    private final String objectiveName;

    public ClientboundSetDisplayObjectivePacket(DisplaySlot param0, @Nullable Objective param1) {
        this.slot = param0;
        if (param1 == null) {
            this.objectiveName = "";
        } else {
            this.objectiveName = param1.getName();
        }

    }

    public ClientboundSetDisplayObjectivePacket(FriendlyByteBuf param0) {
        this.slot = param0.readById(DisplaySlot.BY_ID);
        this.objectiveName = param0.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeById(DisplaySlot::id, this.slot);
        param0.writeUtf(this.objectiveName);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetDisplayObjective(this);
    }

    public DisplaySlot getSlot() {
        return this.slot;
    }

    @Nullable
    public String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}
