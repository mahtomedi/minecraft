package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClientboundSetObjectivePacket implements Packet<ClientGamePacketListener> {
    public static final int METHOD_ADD = 0;
    public static final int METHOD_REMOVE = 1;
    public static final int METHOD_CHANGE = 2;
    private final String objectiveName;
    private final Component displayName;
    private final ObjectiveCriteria.RenderType renderType;
    @Nullable
    private final NumberFormat numberFormat;
    private final int method;

    public ClientboundSetObjectivePacket(Objective param0, int param1) {
        this.objectiveName = param0.getName();
        this.displayName = param0.getDisplayName();
        this.renderType = param0.getRenderType();
        this.numberFormat = param0.numberFormat();
        this.method = param1;
    }

    public ClientboundSetObjectivePacket(FriendlyByteBuf param0) {
        this.objectiveName = param0.readUtf();
        this.method = param0.readByte();
        if (this.method != 0 && this.method != 2) {
            this.displayName = CommonComponents.EMPTY;
            this.renderType = ObjectiveCriteria.RenderType.INTEGER;
            this.numberFormat = null;
        } else {
            this.displayName = param0.readComponentTrusted();
            this.renderType = param0.readEnum(ObjectiveCriteria.RenderType.class);
            this.numberFormat = param0.readNullable(NumberFormatTypes::readFromStream);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.objectiveName);
        param0.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            param0.writeComponent(this.displayName);
            param0.writeEnum(this.renderType);
            param0.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddObjective(this);
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public int getMethod() {
        return this.method;
    }

    public ObjectiveCriteria.RenderType getRenderType() {
        return this.renderType;
    }

    @Nullable
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }
}
