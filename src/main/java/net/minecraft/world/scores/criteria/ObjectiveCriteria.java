package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;

public class ObjectiveCriteria {
    public static final Map<String, ObjectiveCriteria> CRITERIA_BY_NAME = Maps.newHashMap();
    public static final ObjectiveCriteria DUMMY = new ObjectiveCriteria("dummy");
    public static final ObjectiveCriteria TRIGGER = new ObjectiveCriteria("trigger");
    public static final ObjectiveCriteria DEATH_COUNT = new ObjectiveCriteria("deathCount");
    public static final ObjectiveCriteria KILL_COUNT_PLAYERS = new ObjectiveCriteria("playerKillCount");
    public static final ObjectiveCriteria KILL_COUNT_ALL = new ObjectiveCriteria("totalKillCount");
    public static final ObjectiveCriteria HEALTH = new ObjectiveCriteria("health", true, ObjectiveCriteria.RenderType.HEARTS);
    public static final ObjectiveCriteria FOOD = new ObjectiveCriteria("food", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria AIR = new ObjectiveCriteria("air", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria ARMOR = new ObjectiveCriteria("armor", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria EXPERIENCE = new ObjectiveCriteria("xp", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria LEVEL = new ObjectiveCriteria("level", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria[] TEAM_KILL = new ObjectiveCriteria[]{
        new ObjectiveCriteria("teamkill." + ChatFormatting.BLACK.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_BLUE.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_GREEN.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_AQUA.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_RED.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_PURPLE.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.GOLD.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.GRAY.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.DARK_GRAY.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.BLUE.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.GREEN.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.AQUA.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.RED.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.YELLOW.getName()),
        new ObjectiveCriteria("teamkill." + ChatFormatting.WHITE.getName())
    };
    public static final ObjectiveCriteria[] KILLED_BY_TEAM = new ObjectiveCriteria[]{
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.BLACK.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_BLUE.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_GREEN.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_AQUA.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_RED.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.GOLD.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.GRAY.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.DARK_GRAY.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.BLUE.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.GREEN.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.AQUA.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.RED.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.YELLOW.getName()),
        new ObjectiveCriteria("killedByTeam." + ChatFormatting.WHITE.getName())
    };
    private final String name;
    private final boolean readOnly;
    private final ObjectiveCriteria.RenderType renderType;

    public ObjectiveCriteria(String param0) {
        this(param0, false, ObjectiveCriteria.RenderType.INTEGER);
    }

    protected ObjectiveCriteria(String param0, boolean param1, ObjectiveCriteria.RenderType param2) {
        this.name = param0;
        this.readOnly = param1;
        this.renderType = param2;
        CRITERIA_BY_NAME.put(param0, this);
    }

    public static Optional<ObjectiveCriteria> byName(String param0) {
        if (CRITERIA_BY_NAME.containsKey(param0)) {
            return Optional.of(CRITERIA_BY_NAME.get(param0));
        } else {
            int var0 = param0.indexOf(58);
            return var0 < 0
                ? Optional.empty()
                : Registry.STAT_TYPE
                    .getOptional(ResourceLocation.of(param0.substring(0, var0), '.'))
                    .flatMap(param2 -> getStat(param2, ResourceLocation.of(param0.substring(var0 + 1), '.')));
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

    public static enum RenderType {
        INTEGER("integer"),
        HEARTS("hearts");

        private final String id;
        private static final Map<String, ObjectiveCriteria.RenderType> BY_ID;

        private RenderType(String param0) {
            this.id = param0;
        }

        public String getId() {
            return this.id;
        }

        public static ObjectiveCriteria.RenderType byId(String param0) {
            return BY_ID.getOrDefault(param0, INTEGER);
        }

        static {
            Builder<String, ObjectiveCriteria.RenderType> var0 = ImmutableMap.builder();

            for(ObjectiveCriteria.RenderType var1 : values()) {
                var0.put(var1.id, var1);
            }

            BY_ID = var0.build();
        }
    }
}
