package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public record AdvancementRequirements(String[][] requirements) {
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(new String[0][]);

    public AdvancementRequirements(FriendlyByteBuf param0) {
        this(read(param0));
    }

    private static String[][] read(FriendlyByteBuf param0) {
        String[][] var0 = new String[param0.readVarInt()][];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var0[var1] = new String[param0.readVarInt()];

            for(int var2 = 0; var2 < var0[var1].length; ++var2) {
                var0[var1][var2] = param0.readUtf();
            }
        }

        return var0;
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.requirements.length);

        for(String[] var0 : this.requirements) {
            param0.writeVarInt(var0.length);

            for(String var1 : var0) {
                param0.writeUtf(var1);
            }
        }

    }

    public static AdvancementRequirements allOf(Collection<String> param0) {
        return new AdvancementRequirements(param0.stream().map(param0x -> new String[]{param0x}).toArray(param0x -> new String[param0x][]));
    }

    public static AdvancementRequirements anyOf(Collection<String> param0) {
        return new AdvancementRequirements(new String[][]{param0.toArray(param0x -> new String[param0x])});
    }

    public int size() {
        return this.requirements.length;
    }

    public boolean test(Predicate<String> param0) {
        if (this.requirements.length == 0) {
            return false;
        } else {
            for(String[] var0 : this.requirements) {
                if (!anyMatch(var0, param0)) {
                    return false;
                }
            }

            return true;
        }
    }

    public int count(Predicate<String> param0) {
        int var0 = 0;

        for(String[] var1 : this.requirements) {
            if (anyMatch(var1, param0)) {
                ++var0;
            }
        }

        return var0;
    }

    private static boolean anyMatch(String[] param0, Predicate<String> param1) {
        for(String var0 : param0) {
            if (param1.test(var0)) {
                return true;
            }
        }

        return false;
    }

    public static AdvancementRequirements fromJson(JsonArray param0, Set<String> param1) {
        String[][] var0 = new String[param0.size()][];
        Set<String> var1 = new ObjectOpenHashSet<>();

        for(int var2 = 0; var2 < param0.size(); ++var2) {
            JsonArray var3 = GsonHelper.convertToJsonArray(param0.get(var2), "requirements[" + var2 + "]");
            if (var3.isEmpty() && param1.isEmpty()) {
                throw new JsonSyntaxException("Requirement entry cannot be empty");
            }

            var0[var2] = new String[var3.size()];

            for(int var4 = 0; var4 < var3.size(); ++var4) {
                String var5 = GsonHelper.convertToString(var3.get(var4), "requirements[" + var2 + "][" + var4 + "]");
                var0[var2][var4] = var5;
                var1.add(var5);
            }
        }

        if (!param1.equals(var1)) {
            Set<String> var6 = Sets.difference(param1, var1);
            Set<String> var7 = Sets.difference(var1, param1);
            throw new JsonSyntaxException(
                "Advancement completion requirements did not exactly match specified criteria. Missing: " + var6 + ". Unknown: " + var7
            );
        } else {
            return new AdvancementRequirements(var0);
        }
    }

    public JsonArray toJson() {
        JsonArray var0 = new JsonArray();

        for(String[] var1 : this.requirements) {
            JsonArray var2 = new JsonArray();
            Arrays.stream(var1).forEach(var2::add);
            var0.add(var2);
        }

        return var0;
    }

    public boolean isEmpty() {
        return this.requirements.length == 0;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(this.requirements);
    }

    public Set<String> names() {
        Set<String> var0 = new ObjectOpenHashSet<>();

        for(String[] var1 : this.requirements) {
            Collections.addAll(var0, var1);
        }

        return var0;
    }

    public interface Strategy {
        AdvancementRequirements.Strategy AND = AdvancementRequirements::allOf;
        AdvancementRequirements.Strategy OR = AdvancementRequirements::anyOf;

        AdvancementRequirements create(Collection<String> var1);
    }
}
