package net.minecraft.util.thread;

import com.mojang.datafixers.util.Either;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ProcessorHandle<Msg> extends AutoCloseable {
    String name();

    void tell(Msg var1);

    @Override
    default void close() {
    }

    default <Source> CompletableFuture<Source> ask(Function<? super ProcessorHandle<Source>, ? extends Msg> param0) {
        CompletableFuture<Source> var0 = new CompletableFuture<>();
        Msg var1 = param0.apply(of("ask future procesor handle", var0::complete));
        this.tell(var1);
        return var0;
    }

    default <Source> CompletableFuture<Source> askEither(Function<? super ProcessorHandle<Either<Source, Exception>>, ? extends Msg> param0) {
        CompletableFuture<Source> var0 = new CompletableFuture<>();
        Msg var1 = param0.apply(of("ask future procesor handle", param1 -> {
            param1.ifLeft(var0::complete);
            param1.ifRight(var0::completeExceptionally);
        }));
        this.tell(var1);
        return var0;
    }

    static <Msg> ProcessorHandle<Msg> of(final String param0, final Consumer<Msg> param1) {
        return new ProcessorHandle<Msg>() {
            @Override
            public String name() {
                return param0;
            }

            @Override
            public void tell(Msg param0x) {
                param1.accept(param0);
            }

            @Override
            public String toString() {
                return param0;
            }
        };
    }
}
