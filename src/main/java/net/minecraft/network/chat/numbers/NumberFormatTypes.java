package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class NumberFormatTypes {
    public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE
        .byNameCodec()
        .dispatchMap(NumberFormat::type, param0 -> param0.mapCodec().codec());
    public static final Codec<NumberFormat> CODEC = MAP_CODEC.codec();

    public static NumberFormatType<?> bootstrap(Registry<NumberFormatType<?>> param0) {
        NumberFormatType<?> var0 = Registry.register(param0, "blank", BlankFormat.TYPE);
        Registry.register(param0, "result", StyledFormat.TYPE);
        Registry.register(param0, "fixed", FixedFormat.TYPE);
        return var0;
    }

    public static <T extends NumberFormat> void writeToStream(FriendlyByteBuf param0, T param1) {
        NumberFormatType<T> var0 = param1.type();
        param0.writeId(BuiltInRegistries.NUMBER_FORMAT_TYPE, var0);
        var0.writeToStream(param0, param1);
    }

    public static NumberFormat readFromStream(FriendlyByteBuf param0) {
        NumberFormatType<?> var0 = param0.readById(BuiltInRegistries.NUMBER_FORMAT_TYPE);
        return var0.readFromStream(param0);
    }
}
