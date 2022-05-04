package net.minecraft.network.protocol.game;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, ArgumentSignatures argumentSignatures) implements Packet<ServerGamePacketListener> {
    private static final int MAX_MESSAGE_LENGTH = 256;

    public ServerboundChatCommandPacket(String param0, Instant param1, ArgumentSignatures param2) {
        this.command = StringUtil.trimChatMessage(param0);
        this.timeStamp = param1;
        this.argumentSignatures = param2;
    }

    public ServerboundChatCommandPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(256), Instant.ofEpochSecond(param0.readLong()), new ArgumentSignatures(param0));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.command);
        param0.writeLong(this.timeStamp.getEpochSecond());
        this.argumentSignatures.write(param0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatCommand(this);
    }

    private Instant getExpiresAt() {
        return this.timeStamp.plus(ServerboundChatPacket.MESSAGE_EXPIRES_AFTER);
    }

    public boolean hasExpired(Instant param0) {
        return param0.isAfter(this.getExpiresAt());
    }

    public CommandSigningContext signingContext(UUID param0) {
        return new CommandSigningContext.PlainArguments(param0, this.timeStamp, this.argumentSignatures);
    }
}
