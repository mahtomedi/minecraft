package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetTitlesPacket implements Packet<ClientGamePacketListener> {
    private ClientboundSetTitlesPacket.Type type;
    private Component text;
    private int fadeInTime;
    private int stayTime;
    private int fadeOutTime;

    public ClientboundSetTitlesPacket() {
    }

    public ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type param0, Component param1) {
        this(param0, param1, -1, -1, -1);
    }

    public ClientboundSetTitlesPacket(int param0, int param1, int param2) {
        this(ClientboundSetTitlesPacket.Type.TIMES, null, param0, param1, param2);
    }

    public ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type param0, @Nullable Component param1, int param2, int param3, int param4) {
        this.type = param0;
        this.text = param1;
        this.fadeInTime = param2;
        this.stayTime = param3;
        this.fadeOutTime = param4;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.type = param0.readEnum(ClientboundSetTitlesPacket.Type.class);
        if (this.type == ClientboundSetTitlesPacket.Type.TITLE
            || this.type == ClientboundSetTitlesPacket.Type.SUBTITLE
            || this.type == ClientboundSetTitlesPacket.Type.ACTIONBAR) {
            this.text = param0.readComponent();
        }

        if (this.type == ClientboundSetTitlesPacket.Type.TIMES) {
            this.fadeInTime = param0.readInt();
            this.stayTime = param0.readInt();
            this.fadeOutTime = param0.readInt();
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.type);
        if (this.type == ClientboundSetTitlesPacket.Type.TITLE
            || this.type == ClientboundSetTitlesPacket.Type.SUBTITLE
            || this.type == ClientboundSetTitlesPacket.Type.ACTIONBAR) {
            param0.writeComponent(this.text);
        }

        if (this.type == ClientboundSetTitlesPacket.Type.TIMES) {
            param0.writeInt(this.fadeInTime);
            param0.writeInt(this.stayTime);
            param0.writeInt(this.fadeOutTime);
        }

    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetTitles(this);
    }

    @OnlyIn(Dist.CLIENT)
    public ClientboundSetTitlesPacket.Type getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getText() {
        return this.text;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFadeInTime() {
        return this.fadeInTime;
    }

    @OnlyIn(Dist.CLIENT)
    public int getStayTime() {
        return this.stayTime;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFadeOutTime() {
        return this.fadeOutTime;
    }

    public static enum Type {
        TITLE,
        SUBTITLE,
        ACTIONBAR,
        TIMES,
        CLEAR,
        RESET;
    }
}
