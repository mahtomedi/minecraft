package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
    public static final Codec<AdvancementRequirements> CODEC = Codec.STRING
        .listOf()
        .listOf()
        .xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

    public AdvancementRequirements(FriendlyByteBuf param0) {
        this(param0.readList(param0x -> param0x.readList(FriendlyByteBuf::readUtf)));
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.requirements, (param0x, param1) -> param0x.writeCollection(param1, FriendlyByteBuf::writeUtf));
    }

    public static AdvancementRequirements allOf(Collection<String> param0) {
        return new AdvancementRequirements(param0.stream().map(List::of).toList());
    }

    public static AdvancementRequirements anyOf(Collection<String> param0) {
        return new AdvancementRequirements(List.of(List.copyOf(param0)));
    }

    public int size() {
        return this.requirements.size();
    }

    public boolean test(Predicate<String> param0) {
        if (this.requirements.isEmpty()) {
            return false;
        } else {
            for(List<String> var0 : this.requirements) {
                if (!anyMatch(var0, param0)) {
                    return false;
                }
            }

            return true;
        }
    }

    public int count(Predicate<String> param0) {
        int var0 = 0;

        for(List<String> var1 : this.requirements) {
            if (anyMatch(var1, param0)) {
                ++var0;
            }
        }

        return var0;
    }

    private static boolean anyMatch(List<String> param0, Predicate<String> param1) {
        for(String var0 : param0) {
            if (param1.test(var0)) {
                return true;
            }
        }

        return false;
    }

    public DataResult<AdvancementRequirements> validate(Set<String> param0) {
        Set<String> var0 = new ObjectOpenHashSet<>();

        for(List<String> var1 : this.requirements) {
            if (var1.isEmpty() && param0.isEmpty()) {
                return DataResult.error(() -> "Requirement entry cannot be empty");
            }

            var0.addAll(var1);
        }

        if (!param0.equals(var0)) {
            Set<String> var2 = Sets.difference(param0, var0);
            Set<String> var3 = Sets.difference(var0, param0);
            return DataResult.error(
                () -> "Advancement completion requirements did not exactly match specified criteria. Missing: " + var2 + ". Unknown: " + var3
            );
        } else {
            return DataResult.success(this);
        }
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    @Override
    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        Set<String> var0 = new ObjectOpenHashSet<>();

        for(List<String> var1 : this.requirements) {
            var0.addAll(var1);
        }

        return var0;
    }

    public interface Strategy {
        AdvancementRequirements.Strategy AND = AdvancementRequirements::allOf;
        AdvancementRequirements.Strategy OR = AdvancementRequirements::anyOf;

        AdvancementRequirements create(Collection<String> var1);
    }
}
