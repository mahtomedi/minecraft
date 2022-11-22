package net.minecraft.server.network;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FilterMask;

public record FilteredText(String raw, FilterMask mask) {
    public static final FilteredText EMPTY = passThrough("");

    public static FilteredText passThrough(String param0) {
        return new FilteredText(param0, FilterMask.PASS_THROUGH);
    }

    public static FilteredText fullyFiltered(String param0) {
        return new FilteredText(param0, FilterMask.FULLY_FILTERED);
    }

    @Nullable
    public String filtered() {
        return this.mask.apply(this.raw);
    }

    public String filteredOrEmpty() {
        return (String)Objects.requireNonNullElse(this.filtered(), "");
    }

    public boolean isFiltered() {
        return !this.mask.isEmpty();
    }
}
