package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public class ServerboundClientInformationPacket implements Packet<ServerGamePacketListener> {
    public static final int MAX_LANGUAGE_LENGTH = 16;
    private final String language;
    private final int viewDistance;
    private final ChatVisiblity chatVisibility;
    private final boolean chatColors;
    private final int modelCustomisation;
    private final HumanoidArm mainHand;
    private final boolean textFilteringEnabled;

    public ServerboundClientInformationPacket(String param0, int param1, ChatVisiblity param2, boolean param3, int param4, HumanoidArm param5, boolean param6) {
        this.language = param0;
        this.viewDistance = param1;
        this.chatVisibility = param2;
        this.chatColors = param3;
        this.modelCustomisation = param4;
        this.mainHand = param5;
        this.textFilteringEnabled = param6;
    }

    public ServerboundClientInformationPacket(FriendlyByteBuf param0) {
        this.language = param0.readUtf(16);
        this.viewDistance = param0.readByte();
        this.chatVisibility = param0.readEnum(ChatVisiblity.class);
        this.chatColors = param0.readBoolean();
        this.modelCustomisation = param0.readUnsignedByte();
        this.mainHand = param0.readEnum(HumanoidArm.class);
        this.textFilteringEnabled = param0.readBoolean();
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
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleClientInformation(this);
    }

    public String getLanguage() {
        return this.language;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public ChatVisiblity getChatVisibility() {
        return this.chatVisibility;
    }

    public boolean getChatColors() {
        return this.chatColors;
    }

    public int getModelCustomisation() {
        return this.modelCustomisation;
    }

    public HumanoidArm getMainHand() {
        return this.mainHand;
    }

    public boolean isTextFilteringEnabled() {
        return this.textFilteringEnabled;
    }
}
