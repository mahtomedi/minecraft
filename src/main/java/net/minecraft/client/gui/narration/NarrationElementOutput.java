package net.minecraft.client.gui.narration;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface NarrationElementOutput {
    default void add(NarratedElementType param0, Component param1) {
        this.add(param0, NarrationThunk.from(param1.getString()));
    }

    default void add(NarratedElementType param0, String param1) {
        this.add(param0, NarrationThunk.from(param1));
    }

    default void add(NarratedElementType param0, Component... param1) {
        this.add(param0, NarrationThunk.from(ImmutableList.copyOf(param1)));
    }

    void add(NarratedElementType var1, NarrationThunk<?> var2);

    NarrationElementOutput nest();
}
