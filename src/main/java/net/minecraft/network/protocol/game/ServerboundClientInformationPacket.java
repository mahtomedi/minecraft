package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public record ServerboundClientInformationPacket(
    String language,
    int viewDistance,
    ChatVisiblity chatVisibility,
    boolean chatColors,
    int modelCustomisation,
    HumanoidArm mainHand,
    boolean textFilteringEnabled,
    boolean allowsListing
) implements Packet<ServerGamePacketListener> {
    public static final int MAX_LANGUAGE_LENGTH = 16;

    public ServerboundClientInformationPacket(FriendlyByteBuf param0) {
        this(
            param0.readUtf(16),
            param0.readByte(),
            param0.readEnum(ChatVisiblity.class),
            param0.readBoolean(),
            param0.readUnsignedByte(),
            param0.readEnum(HumanoidArm.class),
            param0.readBoolean(),
            param0.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.language);
        param0.writeByte(this.viewDistance);
        param0.writeEnum(this.chatVisibility);
        param0.writeBoolean(this.chatColors);
        param0.writeByte(this.modelCustomisation);
        param0.writeEnum(this.mainHand);
        param0.writeBoolean(this.textFilteringEnabled);
        param0.writeBoolean(this.allowsListing);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleClientInformation(this);
    }
}
