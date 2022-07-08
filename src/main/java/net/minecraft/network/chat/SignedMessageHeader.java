package net.minecraft.network.chat;

import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature, UUID sender) {
    public SignedMessageHeader(FriendlyByteBuf param0) {
        this(param0.readNullable(MessageSignature::new), param0.readUUID());
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeNullable(this.previousSignature, (param0x, param1) -> param1.write(param0x));
        param0.writeUUID(this.sender);
    }

    public void updateSignature(SignatureUpdater.Output param0, byte[] param1) throws SignatureException {
        if (this.previousSignature != null) {
            param0.update(this.previousSignature.bytes());
        }

        param0.update(UUIDUtil.uuidToByteArray(this.sender));
        param0.update(param1);
    }
}
