package net.minecraft.network.protocol.game;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, ArgumentSignatures argumentSignatures, boolean signedPreview)
    implements Packet<ServerGamePacketListener> {
    public ServerboundChatCommandPacket(String param0, Instant param1, ArgumentSignatures param2, boolean param3) {
        param0 = StringUtil.trimChatMessage(param0);
        this.command = param0;
        this.timeStamp = param1;
        this.argumentSignatures = param2;
        this.signedPreview = param3;
    }

    public ServerboundChatCommandPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(256), param0.readInstant(), new ArgumentSignatures(param0), param0.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.command, 256);
        param0.writeInstant(this.timeStamp);
        this.argumentSignatures.write(param0);
        param0.writeBoolean(this.signedPreview);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleChatCommand(this);
    }

    public CommandSigningContext signingContext(UUID param0) {
        return new CommandSigningContext.SignedArguments(param0, this.timeStamp, this.argumentSignatures, this.signedPreview);
    }
}
