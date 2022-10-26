package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
    public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(SignedMessageLink::index),
                    UUIDUtil.CODEC.fieldOf("sender").forGetter(SignedMessageLink::sender),
                    UUIDUtil.CODEC.fieldOf("session_id").forGetter(SignedMessageLink::sessionId)
                )
                .apply(param0, SignedMessageLink::new)
    );

    public static SignedMessageLink unsigned(UUID param0) {
        return root(param0, Util.NIL_UUID);
    }

    public static SignedMessageLink root(UUID param0, UUID param1) {
        return new SignedMessageLink(0, param0, param1);
    }

    public void updateSignature(SignatureUpdater.Output param0) throws SignatureException {
        param0.update(UUIDUtil.uuidToByteArray(this.sender));
        param0.update(UUIDUtil.uuidToByteArray(this.sessionId));
        param0.update(Ints.toByteArray(this.index));
    }

    public boolean isDescendantOf(SignedMessageLink param0) {
        return this.index > param0.index() && this.sender.equals(param0.sender()) && this.sessionId.equals(param0.sessionId());
    }

    @Nullable
    public SignedMessageLink advance() {
        return this.index == Integer.MAX_VALUE ? null : new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
    }
}
