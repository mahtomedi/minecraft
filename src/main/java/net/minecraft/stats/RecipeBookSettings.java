package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
    private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = ImmutableMap.of(
        RecipeBookType.CRAFTING,
        Pair.of("isGuiOpen", "isFilteringCraftable"),
        RecipeBookType.FURNACE,
        Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"),
        RecipeBookType.BLAST_FURNACE,
        Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"),
        RecipeBookType.SMOKER,
        Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable")
    );
    private final Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

    private RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.TypeSettings> param0) {
        this.states = param0;
    }

    public RecipeBookSettings() {
        this(Util.make(Maps.newEnumMap(RecipeBookType.class), param0 -> {
            for(RecipeBookType var0 : RecipeBookType.values()) {
                param0.put(var0, new RecipeBookSettings.TypeSettings(false, false));
            }

        }));
    }

    public boolean isOpen(RecipeBookType param0) {
        return this.states.get(param0).open;
    }

    public void setOpen(RecipeBookType param0, boolean param1) {
        this.states.get(param0).open = param1;
    }

    public boolean isFiltering(RecipeBookType param0) {
        return this.states.get(param0).filtering;
    }

    public void setFiltering(RecipeBookType param0, boolean param1) {
        this.states.get(param0).filtering = param1;
    }

    public static RecipeBookSettings read(FriendlyByteBuf param0) {
        Map<RecipeBookType, RecipeBookSettings.TypeSettings> var0 = Maps.newEnumMap(RecipeBookType.class);

        for(RecipeBookType var1 : RecipeBookType.values()) {
            boolean var2 = param0.readBoolean();
            boolean var3 = param0.readBoolean();
            var0.put(var1, new RecipeBookSettings.TypeSettings(var2, var3));
        }

        return new RecipeBookSettings(var0);
    }

    public void write(FriendlyByteBuf param0) {
        for(RecipeBookType var0 : RecipeBookType.values()) {
            RecipeBookSettings.TypeSettings var1 = this.states.get(var0);
            if (var1 == null) {
                param0.writeBoolean(false);
                param0.writeBoolean(false);
            } else {
                param0.writeBoolean(var1.open);
                param0.writeBoolean(var1.filtering);
            }
        }

    }

    public static RecipeBookSettings read(CompoundTag param0) {
        Map<RecipeBookType, RecipeBookSettings.TypeSettings> var0 = Maps.newEnumMap(RecipeBookType.class);
        TAG_FIELDS.forEach((param2, param3) -> {
            boolean var0x = param0.getBoolean(param3.getFirst());
            boolean var1x = param0.getBoolean(param3.getSecond());
            var0.put(param2, new RecipeBookSettings.TypeSettings(var0x, var1x));
        });
        return new RecipeBookSettings(var0);
    }

    public void write(CompoundTag param0) {
        TAG_FIELDS.forEach((param1, param2) -> {
            RecipeBookSettings.TypeSettings var0 = this.states.get(param1);
            param0.putBoolean(param2.getFirst(), var0.open);
            param0.putBoolean(param2.getSecond(), var0.filtering);
        });
    }

    public RecipeBookSettings copy() {
        Map<RecipeBookType, RecipeBookSettings.TypeSettings> var0 = Maps.newEnumMap(RecipeBookType.class);

        for(RecipeBookType var1 : RecipeBookType.values()) {
            RecipeBookSettings.TypeSettings var2 = this.states.get(var1);
            var0.put(var1, var2.copy());
        }

        return new RecipeBookSettings(var0);
    }

    public void replaceFrom(RecipeBookSettings param0) {
        this.states.clear();

        for(RecipeBookType var0 : RecipeBookType.values()) {
            RecipeBookSettings.TypeSettings var1 = param0.states.get(var0);
            this.states.put(var0, var1.copy());
        }

    }

    @Override
    public boolean equals(Object param0) {
        return this == param0 || param0 instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings)param0).states);
    }

    @Override
    public int hashCode() {
        return this.states.hashCode();
    }

    static final class TypeSettings {
        boolean open;
        boolean filtering;

        public TypeSettings(boolean param0, boolean param1) {
            this.open = param0;
            this.filtering = param1;
        }

        public RecipeBookSettings.TypeSettings copy() {
            return new RecipeBookSettings.TypeSettings(this.open, this.filtering);
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (!(param0 instanceof RecipeBookSettings.TypeSettings)) {
                return false;
            } else {
                RecipeBookSettings.TypeSettings var0 = (RecipeBookSettings.TypeSettings)param0;
                return this.open == var0.open && this.filtering == var0.filtering;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.open ? 1 : 0;
            return 31 * var0 + (this.filtering ? 1 : 0);
        }

        @Override
        public String toString() {
            return "[open=" + this.open + ", filtering=" + this.filtering + "]";
        }
    }
}
