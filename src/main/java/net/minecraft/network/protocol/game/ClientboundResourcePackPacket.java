package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
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
        if (param0.readBoolean()) {
            this.prompt = param0.readComponent();
        } else {
            this.prompt = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.url);
        param0.writeUtf(this.hash);
        param0.writeBoolean(this.required);
        if (this.prompt != null) {
            param0.writeBoolean(true);
            param0.writeComponent(this.prompt);
        } else {
            param0.writeBoolean(false);
        }

    }

    public void handle(ClientGamePacketListener param0) {
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
