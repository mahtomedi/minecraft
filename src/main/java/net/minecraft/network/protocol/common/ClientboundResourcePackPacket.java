package net.minecraft.network.protocol.common;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientCommonPacketListener> {
    public static final int MAX_HASH_LENGTH = 40;
    private final String url;
    private final String hash;
    private final boolean required;
    @Nullable
    private final Component prompt;

    public ClientboundResourcePackPacket(String param0, String param1, boolean param2, @Nullable Component param3) {
        if (param1.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + param1.length() + ")");
        } else {
            this.url = param0;
            this.hash = param1;
            this.required = param2;
            this.prompt = param3;
        }
    }

    public ClientboundResourcePackPacket(FriendlyByteBuf param0) {
        this.url = param0.readUtf();
        this.hash = param0.readUtf(40);
        this.required = param0.readBoolean();
        this.prompt = param0.readNullable(FriendlyByteBuf::readComponentTrusted);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.url);
        param0.writeUtf(this.hash);
        param0.writeBoolean(this.required);
        param0.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
    }

    public void handle(ClientCommonPacketListener param0) {
        param0.handleResourcePack(this);
    }

    public String getUrl() {
        return this.url;
    }

    public String getHash() {
        return this.hash;
    }

    public boolean isRequired() {
        return this.required;
    }

    @Nullable
    public Component getPrompt() {
        return this.prompt;
    }
}
