package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundServerDataPacket implements Packet<ClientGamePacketListener> {
    private final Optional<Component> motd;
    private final Optional<String> iconBase64;
    private final boolean previewsChat;
    private final boolean enforcesSecureChat;

    public ClientboundServerDataPacket(@Nullable Component param0, @Nullable String param1, boolean param2, boolean param3) {
        this.motd = Optional.ofNullable(param0);
        this.iconBase64 = Optional.ofNullable(param1);
        this.previewsChat = param2;
        this.enforcesSecureChat = param3;
    }

    public ClientboundServerDataPacket(FriendlyByteBuf param0) {
        this.motd = param0.readOptional(FriendlyByteBuf::readComponent);
        this.iconBase64 = param0.readOptional(FriendlyByteBuf::readUtf);
        this.previewsChat = param0.readBoolean();
        this.enforcesSecureChat = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeOptional(this.motd, FriendlyByteBuf::writeComponent);
        param0.writeOptional(this.iconBase64, FriendlyByteBuf::writeUtf);
        param0.writeBoolean(this.previewsChat);
        param0.writeBoolean(this.enforcesSecureChat);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleServerData(this);
    }

    public Optional<Component> getMotd() {
        return this.motd;
    }

    public Optional<String> getIconBase64() {
        return this.iconBase64;
    }

    public boolean previewsChat() {
        return this.previewsChat;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }
}
