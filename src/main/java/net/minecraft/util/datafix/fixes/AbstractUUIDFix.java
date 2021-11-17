package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractUUIDFix extends DataFix {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected TypeReference typeReference;

    public AbstractUUIDFix(Schema param0, TypeReference param1) {
        super(param0, false);
        this.typeReference = param1;
    }

    protected Typed<?> updateNamedChoice(Typed<?> param0, String param1, Function<Dynamic<?>, Dynamic<?>> param2) {
        Type<?> var0 = this.getInputSchema().getChoiceType(this.typeReference, param1);
        Type<?> var1 = this.getOutputSchema().getChoiceType(this.typeReference, param1);
        return param0.updateTyped(DSL.namedChoice(param1, var0), var1, param1x -> param1x.update(DSL.remainderFinder(), param2));
    }

    protected static Optional<Dynamic<?>> replaceUUIDString(Dynamic<?> param0, String param1, String param2) {
        return createUUIDFromString(param0, param1).map(param3 -> param0.remove(param1).set(param2, param3));
    }

    protected static Optional<Dynamic<?>> replaceUUIDMLTag(Dynamic<?> param0, String param1, String param2) {
        return param0.get(param1).result().flatMap(AbstractUUIDFix::createUUIDFromML).map(param3 -> param0.remove(param1).set(param2, param3));
    }

    protected static Optional<Dynamic<?>> replaceUUIDLeastMost(Dynamic<?> param0, String param1, String param2) {
        String var0 = param1 + "Most";
        String var1 = param1 + "Least";
        return createUUIDFromLongs(param0, var0, var1).map(param4 -> param0.remove(var0).remove(var1).set(param2, param4));
    }

    protected static Optional<Dynamic<?>> createUUIDFromString(Dynamic<?> param0, String param1) {
        return param0.get(param1).result().flatMap(param1x -> {
            String var0x = param1x.asString(null);
            if (var0x != null) {
                try {
                    UUID var1x = UUID.fromString(var0x);
                    return createUUIDTag(param0, var1x.getMostSignificantBits(), var1x.getLeastSignificantBits());
                } catch (IllegalArgumentException var4) {
                }
            }

            return Optional.empty();
        });
    }

    protected static Optional<Dynamic<?>> createUUIDFromML(Dynamic<?> param0x) {
        return createUUIDFromLongs(param0x, "M", "L");
    }

    protected static Optional<Dynamic<?>> createUUIDFromLongs(Dynamic<?> param0, String param1, String param2) {
        long var0 = param0.get(param1).asLong(0L);
        long var1 = param0.get(param2).asLong(0L);
        return var0 != 0L && var1 != 0L ? createUUIDTag(param0, var0, var1) : Optional.empty();
    }

    protected static Optional<Dynamic<?>> createUUIDTag(Dynamic<?> param0, long param1, long param2) {
        return Optional.of(param0.createIntList(Arrays.stream(new int[]{(int)(param1 >> 32), (int)param1, (int)(param2 >> 32), (int)param2})));
    }
}
