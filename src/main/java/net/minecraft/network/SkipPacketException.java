package net.minecraft.network;

import io.netty.handler.codec.EncoderException;

public class SkipPacketException extends EncoderException {
    public SkipPacketException(Throwable param0) {
        super(param0);
    }
}
