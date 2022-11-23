package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.util.StringRepresentable;

public class ObjectiveCriteria {
    private static final Map<String, ObjectiveCriteria> CUSTOM_CRITERIA = Maps.newHashMap();
    private static final Map<String, ObjectiveCriteria> CRITERIA_CACHE = Maps.newHashMap();
    public static final ObjectiveCriteria DUMMY = registerCustom("dummy");
    public static final ObjectiveCriteria TRIGGER = registerCustom("trigger");
    public static final ObjectiveCriteria DEATH_COUNT = registerCustom("deathCount");
    public static final ObjectiveCriteria KILL_COUNT_PLAYERS = registerCustom("playerKillCount");
    public static final ObjectiveCriteria KILL_COUNT_ALL = registerCustom("totalKillCount");
    public static final ObjectiveCriteria HEALTH = registerCustom("health", true, ObjectiveCriteria.RenderType.HEARTS);
    public static final ObjectiveCriteria FOOD = registerCustom("food", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria AIR = registerCustom("air", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria ARMOR = registerCustom("armor", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria EXPERIENCE = registerCustom("xp", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria LEVEL = registerCustom("level", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria[] TEAM_KILL = new ObjectiveCriteria[]{
        registerCustom("teamkill." + ChatFormatting.BLACK.getName()),
        registerCustom("teamkill." + ChatFormatting.DARK_BLUE.getName()),
        registerCustom("teamkill." + ChatFormatting.DARK_GREEN.getName()),
        registerCustom("teamkill." + ChatFormatting.DARK_AQUA.getName()),
        registerCustom("teamkill." + ChatFormatting.DARK_RED.getName()),
        registerCustom("teamkill." + ChatFormatting.DARK_PURPLE.getName()),
        registerCustom("teamkill." + ChatFormatting.GOLD.getName()),
        registerCustom("teamkill." + ChatFormatting.GRAY.getName()),
        registerCustom("teamkill." + ChatFormatting.DARK_GRAY.getName()),
        registerCustom("teamkill." + ChatFormatting.BLUE.getName()),
        registerCustom("teamkill." + ChatFormatting.GREEN.getName()),
        registerCustom("teamkill." + ChatFormatting.AQUA.getName()),
        registerCustom("teamkill." + ChatFormatting.RED.getName()),
        registerCustom("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()),
        registerCustom("teamkill." + ChatFormatting.YELLOW.getName()),
        registerCustom("teamkill." + ChatFormatting.WHITE.getName())
    };
    public static final ObjectiveCriteria[] KILLED_BY_TEAM = new ObjectiveCriteria[]{
        registerCustom("killedByTeam." + ChatFormatting.BLACK.getName()),
        registerCustom("killedByTeam." + ChatFormatting.DARK_BLUE.getName()),
        registerCustom("killedByTeam." + ChatFormatting.DARK_GREEN.getName()),
        registerCustom("killedByTeam." + ChatFormatting.DARK_AQUA.getName()),
        registerCustom("killedByTeam." + ChatFormatting.DARK_RED.getName()),
        registerCustom("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()),
        registerCustom("killedByTeam." + ChatFormatting.GOLD.getName()),
        registerCustom("killedByTeam." + ChatFormatting.GRAY.getName()),
        registerCustom("killedByTeam." + ChatFormatting.DARK_GRAY.getName()),
        registerCustom("killedByTeam." + ChatFormatting.BLUE.getName()),
        registerCustom("killedByTeam." + ChatFormatting.GREEN.getName()),
        registerCustom("killedByTeam." + ChatFormatting.AQUA.getName()),
        registerCustom("killedByTeam." + ChatFormatting.RED.getName()),
        registerCustom("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()),
        registerCustom("killedByTeam." + ChatFormatting.YELLOW.getName()),
        registerCustom("killedByTeam." + ChatFormatting.WHITE.getName())
    };
    private final String name;
    private final boolean readOnly;
    private final ObjectiveCriteria.RenderType renderType;

    private static ObjectiveCriteria registerCustom(String param0, boolean param1, ObjectiveCriteria.RenderType param2) {
        ObjectiveCriteria var0 = new ObjectiveCriteria(param0, param1, param2);
        CUSTOM_CRITERIA.put(param0, var0);
        return var0;
    }

    private static ObjectiveCriteria registerCustom(String param0) {
        return registerCustom(param0, false, ObjectiveCriteria.RenderType.INTEGER);
    }

    protected ObjectiveCriteria(String param0) {
        this(param0, false, ObjectiveCriteria.RenderType.INTEGER);
    }

    protected ObjectiveCriteria(String param0, boolean param1, ObjectiveCriteria.RenderType param2) {
        this.name = param0;
        this.readOnly = param1;
        this.renderType = param2;
        CRITERIA_CACHE.put(param0, this);
    }

    public static Set<String> getCustomCriteriaNames() {
        return ImmutableSet.copyOf(CUSTOM_CRITERIA.keySet());
    }

    public static Optional<ObjectiveCriteria> byName(String param0) {
        ObjectiveCriteria var0 = CRITERIA_CACHE.get(param0);
        if (var0 != null) {
            return Optional.of(var0);
        } else {
            int var1 = param0.indexOf(58);
            return var1 < 0
                ? Optional.empty()
                : BuiltInRegistries.STAT_TYPE
                    .getOptional(ResourceLocation.of(param0.substring(0, var1), '.'))
                    .flatMap(param2 -> getStat(param2, ResourceLocation.of(param0.substring(var1 + 1), '.')));
        }
    }

    private static <T> Optional<ObjectiveCriteria> getStat(StatType<T> param0, ResourceLocation param1) {
        return param0.getRegistry().getOptional(param1).map(param0::get);
    }

    public String getName() {
        return this.name;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public ObjectiveCriteria.RenderType getDefaultRenderType() {
        return this.renderType;
    }

    public static enum RenderType implements StringRepresentable {
        INTEGER("integer"),
        HEARTS("hearts");

        private final String id;
        public static final StringRepresentable.EnumCodec<ObjectiveCriteria.RenderType> CODEC = StringRepresentable.fromEnum(
            ObjectiveCriteria.RenderType::values
        );

        private RenderType(String param0) {
            this.id = param0;
        }

        public String getId() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public static ObjectiveCriteria.RenderType byId(String param0) {
            return CODEC.byName(param0, INTEGER);
        }
    }
}
