package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T> {
    void write(FriendlyByteBuf var1, T var2);

    T read(FriendlyByteBuf var1);

    default EntityDataAccessor<T> createAccessor(int param0) {
        return new EntityDataAccessor<>(param0, this);
    }

    T copy(T var1);

    static <T> EntityDataSerializer<T> simple(final BiConsumer<FriendlyByteBuf, T> param0, final Function<FriendlyByteBuf, T> param1) {
        return new EntityDataSerializer.ForValueType<T>() {
            @Override
            public void write(FriendlyByteBuf param0x, T param1x) {
                param0.accept(param0, param1);
            }

            @Override
            public T read(FriendlyByteBuf param0x) {
                return param1.apply(param0);
            }
        };
    }

    static <T> EntityDataSerializer<Optional<T>> optional(final BiConsumer<FriendlyByteBuf, T> param0, final Function<FriendlyByteBuf, T> param1) {
        return new EntityDataSerializer.ForValueType<Optional<T>>() {
            public void write(FriendlyByteBuf param0x, Optional<T> param1x) {
                if (param1.isPresent()) {
                    param0.writeBoolean(true);
                    param0.accept(param0, param1.get());
                } else {
                    param0.writeBoolean(false);
                }

            }

            public Optional<T> read(FriendlyByteBuf param0x) {
                return param0.readBoolean() ? Optional.of(param1.apply(param0)) : Optional.empty();
            }
        };
    }

    static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> param0) {
        return simple(FriendlyByteBuf::writeEnum, param1 -> param1.readEnum(param0));
    }

    static <T> EntityDataSerializer<T> simpleId(IdMap<T> param0) {
        return simple((param1, param2) -> param1.writeId(param0, param2), param1 -> param1.readById(param0));
    }

    public interface ForValueType<T> extends EntityDataSerializer<T> {
        @Override
        default T copy(T param0) {
            return param0;
        }
    }
}
