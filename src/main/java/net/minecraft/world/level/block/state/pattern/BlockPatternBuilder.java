package net.minecraft.world.level.block.state.pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BlockPatternBuilder {
    private static final Joiner COMMA_JOINED = Joiner.on(",");
    private final List<String[]> pattern = Lists.newArrayList();
    private final Map<Character, Predicate<BlockInWorld>> lookup = Maps.newHashMap();
    private int height;
    private int width;

    private BlockPatternBuilder() {
        this.lookup.put(' ', Predicates.alwaysTrue());
    }

    public BlockPatternBuilder aisle(String... param0) {
        if (!ArrayUtils.isEmpty((Object[])param0) && !StringUtils.isEmpty(param0[0])) {
            if (this.pattern.isEmpty()) {
                this.height = param0.length;
                this.width = param0[0].length();
            }

            if (param0.length != this.height) {
                throw new IllegalArgumentException(
                    "Expected aisle with height of " + this.height + ", but was given one with a height of " + param0.length + ")"
                );
            } else {
                for(String var0 : param0) {
                    if (var0.length() != this.width) {
                        throw new IllegalArgumentException(
                            "Not all rows in the given aisle are the correct width (expected " + this.width + ", found one with " + var0.length() + ")"
                        );
                    }

                    for(char var1 : var0.toCharArray()) {
                        if (!this.lookup.containsKey(var1)) {
                            this.lookup.put(var1, null);
                        }
                    }
                }

                this.pattern.add(param0);
                return this;
            }
        } else {
            throw new IllegalArgumentException("Empty pattern for aisle");
        }
    }

    public static BlockPatternBuilder start() {
        return new BlockPatternBuilder();
    }

    public BlockPatternBuilder where(char param0, Predicate<BlockInWorld> param1) {
        this.lookup.put(param0, param1);
        return this;
    }

    public BlockPattern build() {
        return new BlockPattern(this.createPattern());
    }

    private Predicate<BlockInWorld>[][][] createPattern() {
        this.ensureAllCharactersMatched();
        Predicate<BlockInWorld>[][][] var0 = (Predicate[][][])Array.newInstance(Predicate.class, this.pattern.size(), this.height, this.width);

        for(int var1 = 0; var1 < this.pattern.size(); ++var1) {
            for(int var2 = 0; var2 < this.height; ++var2) {
                for(int var3 = 0; var3 < this.width; ++var3) {
                    var0[var1][var2][var3] = this.lookup.get(this.pattern.get(var1)[var2].charAt(var3));
                }
            }
        }

        return var0;
    }

    private void ensureAllCharactersMatched() {
        List<Character> var0 = Lists.newArrayList();

        for(Entry<Character, Predicate<BlockInWorld>> var1 : this.lookup.entrySet()) {
            if (var1.getValue() == null) {
                var0.add(var1.getKey());
            }
        }

        if (!var0.isEmpty()) {
            throw new IllegalStateException("Predicates for character(s) " + COMMA_JOINED.join(var0) + " are missing");
        }
    }
}
