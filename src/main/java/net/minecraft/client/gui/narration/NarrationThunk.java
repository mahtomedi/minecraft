package net.minecraft.client.gui.narration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NarrationThunk<T> {
    private final T contents;
    private final BiConsumer<Consumer<String>, T> converter;
    public static final NarrationThunk<?> EMPTY = new NarrationThunk<>(Unit.INSTANCE, (param0, param1) -> {
    });

    private NarrationThunk(T param0, BiConsumer<Consumer<String>, T> param1) {
        this.contents = param0;
        this.converter = param1;
    }

    public static NarrationThunk<?> from(String param0) {
        return new NarrationThunk<>(param0, Consumer::accept);
    }

    public static NarrationThunk<?> from(Component param0) {
        return new NarrationThunk<>(param0, (param0x, param1) -> param0x.accept(param1.getContents()));
    }

    public static NarrationThunk<?> from(List<Component> param0) {
        return new NarrationThunk<>(param0, (param1, param2) -> param0.stream().map(Component::getString).forEach(param1));
    }

    public void getText(Consumer<String> param0) {
        this.converter.accept(param0, this.contents);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof NarrationThunk)) {
            return false;
        } else {
            NarrationThunk<?> var0 = (NarrationThunk)param0;
            return var0.converter == this.converter && var0.contents.equals(this.contents);
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.contents.hashCode();
        return 31 * var0 + this.converter.hashCode();
    }
}
