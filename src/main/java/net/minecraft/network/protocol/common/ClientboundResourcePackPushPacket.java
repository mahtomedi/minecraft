package net.minecraft.network.protocol.common;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPushPacket(UUID id, String url, String hash, boolean required, @Nullable Component prompt)
    implements Packet<ClientCommonPacketListener> {
    public static final int MAX_HASH_LENGTH = 40;

    public ClientboundResourcePackPushPacket(UUID param0, String param1, String param2, boolean param3, @Nullable Component param4) {
        if (param2.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + param2.length() + ")");
        } else {
            this.id = param0;
            this.url = param1;
            this.hash = param2;
            this.required = param3;
            this.prompt = param4;
        }
    }

    public ClientboundResourcePackPushPacket(FriendlyByteBuf param0) {
        this(param0.readUUID(), param0.readUtf(), param0.readUtf(40), param0.readBoolean(), param0.readNullable(FriendlyByteBuf::readComponentTrusted));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.id);
        param0.writeUtf(this.url);
        param0.writeUtf(this.hash);
        param0.writeBoolean(this.required);
        param0.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
    }

    public void handle(ClientCommonPacketListener param0) {
        param0.handleResourcePackPush(this);
    }
}
