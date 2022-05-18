package net.minecraft.server.network;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public record FilteredText<T>(T raw, @Nullable T filtered) {
    public static final FilteredText<String> EMPTY_STRING = passThrough("");

    public static <T> FilteredText<T> passThrough(T param0) {
        return new FilteredText<>(param0, param0);
    }

    public static <T> FilteredText<T> fullyFiltered(T param0) {
        return new FilteredText<>(param0, (T)null);
    }

    public <U> FilteredText<U> map(Function<T, U> param0) {
        return new FilteredText<>(param0.apply(this.raw), Util.mapNullable(this.filtered, param0));
    }

    public boolean isFiltered() {
        return !this.raw.equals(this.filtered);
    }

    public boolean isFullyFiltered() {
        return this.filtered == null;
    }

    public T filteredOrElse(T param0) {
        return (T)(this.filtered != null ? this.filtered : param0);
    }

    @Nullable
    public T filter(ServerPlayer param0, ServerPlayer param1) {
        return (T)(param0.shouldFilterMessageTo(param1) ? this.filtered : this.raw);
    }

    @Nullable
    public T filter(CommandSourceStack param0, ServerPlayer param1) {
        ServerPlayer var0 = param0.getPlayer();
        return (T)(var0 != null ? this.filter(var0, param1) : this.raw);
    }
}
