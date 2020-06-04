package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundClientInformationPacket implements Packet<ServerGamePacketListener> {
    private String language;
    private int viewDistance;
    private ChatVisiblity chatVisibility;
    private boolean chatColors;
    private int modelCustomisation;
    private HumanoidArm mainHand;

    public ServerboundClientInformationPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundClientInformationPacket(String param0, int param1, ChatVisiblity param2, boolean param3, int param4, HumanoidArm param5) {
        this.language = param0;
        this.viewDistance = param1;
        this.chatVisibility = param2;
        this.chatColors = param3;
        this.modelCustomisation = param4;
        this.mainHand = param5;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.language = param0.readUtf(16);
        this.viewDistance = param0.readByte();
        this.chatVisibility = param0.readEnum(ChatVisiblity.class);
        this.chatColors = param0.readBoolean();
        this.modelCustomisation = param0.readUnsignedByte();
        this.mainHand = param0.readEnum(HumanoidArm.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.language);
        param0.writeByte(this.viewDistance);
        param0.writeEnum(this.chatVisibility);
        param0.writeBoolean(this.chatColors);
        param0.writeByte(this.modelCustomisation);
        param0.writeEnum(this.mainHand);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleClientInformation(this);
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
}
