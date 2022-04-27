package net.minecraft.network.protocol.game;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ClientboundPlayerChatPacket(Component content, ChatType type, ChatSender sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature)
    implements Packet<ClientGamePacketListener> {
    private static final Duration MESSAGE_EXPIRES_AFTER = ServerboundChatPacket.MESSAGE_EXPIRES_AFTER.plus(Duration.ofMinutes(2L));

    public ClientboundPlayerChatPacket(FriendlyByteBuf param0) {
        this(
            param0.readComponent(),
            ChatType.getForIndex(param0.readByte()),
            new ChatSender(param0),
            Instant.ofEpochSecond(param0.readLong()),
            new Crypt.SaltSignaturePair(param0)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.content);
        param0.writeByte(this.type.getIndex());
        this.sender.write(param0);
        param0.writeLong(this.timeStamp.getEpochSecond());
        this.saltSignature.write(param0);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }

    public boolean isSignatureValid(ProfilePublicKey.Trusted param0) {
        try {
            Signature var0 = param0.verifySignature();
            Crypt.updateChatSignature(var0, this.saltSignature.salt(), this.sender.uuid(), this.timeStamp, this.content.getString());
            return var0.verify(this.saltSignature.signature());
        } catch (CryptException | GeneralSecurityException var3) {
            return false;
        }
    }

    private Instant getExpiresAt() {
        return this.timeStamp.plus(MESSAGE_EXPIRES_AFTER);
    }

    public boolean hasExpired(Instant param0) {
        return param0.isAfter(this.getExpiresAt());
    }
}
