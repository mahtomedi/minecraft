package net.minecraft.network.protocol.login.custom;

import java.lang.runtime.ObjectMethods;
import net.minecraft.network.FriendlyByteBuf;

public final class DiscardedQueryAnswerPayload extends Record implements CustomQueryAnswerPayload {
    public static final DiscardedQueryAnswerPayload INSTANCE = new DiscardedQueryAnswerPayload();

    @Override
    public void write(FriendlyByteBuf param0) {
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap<"toString",DiscardedQueryAnswerPayload,"">(this);
    }

    @Override
    public final int hashCode() {
        return ObjectMethods.bootstrap<"hashCode",DiscardedQueryAnswerPayload,"">(this);
    }

    @Override
    public final boolean equals(Object param0) {
        return ObjectMethods.bootstrap<"equals",DiscardedQueryAnswerPayload,"">(this, param0);
    }
}
