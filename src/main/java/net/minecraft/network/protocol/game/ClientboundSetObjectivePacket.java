package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetObjectivePacket implements Packet<ClientGamePacketListener> {
    private String objectiveName;
    private Component displayName;
    private ObjectiveCriteria.RenderType renderType;
    private int method;

    public ClientboundSetObjectivePacket() {
    }

    public ClientboundSetObjectivePacket(Objective param0, int param1) {
        this.objectiveName = param0.getName();
        this.displayName = param0.getDisplayName();
        this.renderType = param0.getRenderType();
        this.method = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.objectiveName = param0.readUtf(16);
        this.method = param0.readByte();
        if (this.method == 0 || this.method == 2) {
            this.displayName = param0.readComponent();
            this.renderType = param0.readEnum(ObjectiveCriteria.RenderType.class);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.objectiveName);
        param0.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            param0.writeComponent(this.displayName);
            param0.writeEnum(this.renderType);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddObjective(this);
    }

    @OnlyIn(Dist.CLIENT)
    public String getObjectiveName() {
        return this.objectiveName;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDisplayName() {
        return this.displayName;
    }

    @OnlyIn(Dist.CLIENT)
    public int getMethod() {
        return this.method;
    }

    @OnlyIn(Dist.CLIENT)
    public ObjectiveCriteria.RenderType getRenderType() {
        return this.renderType;
    }
}
