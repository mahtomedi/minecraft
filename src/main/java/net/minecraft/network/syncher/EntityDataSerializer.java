package net.minecraft.network.syncher;

import java.util.Optional;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T> {
    void write(FriendlyByteBuf var1, T var2);

    T read(FriendlyByteBuf var1);

    default EntityDataAccessor<T> createAccessor(int param0) {
        return new EntityDataAccessor<>(param0, this);
    }

    T copy(T var1);

    static <T> EntityDataSerializer<T> simple(final FriendlyByteBuf.Writer<T> param0, final FriendlyByteBuf.Reader<T> param1) {
        return new EntityDataSerializer.ForValueType<T>() {
            @Override
            public void write(FriendlyByteBuf param0x, T param1x) {
                param0.accept((T)param0, param1);
            }

            @Override
            public T read(FriendlyByteBuf param0x) {
                return param1.apply((T)param0);
            }
        };
    }

    static <T> EntityDataSerializer<Optional<T>> optional(FriendlyByteBuf.Writer<T> param0, FriendlyByteBuf.Reader<T> param1) {
        return simple(param0.asOptional(), param1.asOptional());
    }

    static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> param0) {
        return simple(FriendlyByteBuf::writeEnum, param1 -> param1.readEnum(param0));
    }

    static <T> EntityDataSerializer<T> simpleId(IdMap<T> param0) {
        return simple((param1, param2) -> param1.writeId(param0, (T)param2), param1 -> param1.<T>readById(param0));
    }

    public interface ForValueType<T> extends EntityDataSerializer<T> {
        @Override
        default T copy(T param0) {
            return param0;
        }
    }
}
