package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SerializableUUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.<ListTag>comparingInt(param0 -> param0.getInt(1))
        .thenComparingInt(param0 -> param0.getInt(0))
        .thenComparingInt(param0 -> param0.getInt(2));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.<ListTag>comparingDouble(param0 -> param0.getDouble(1))
        .thenComparingDouble(param0 -> param0.getDouble(0))
        .thenComparingDouble(param0 -> param0.getDouble(2));
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on(",");
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private NbtUtils() {
    }

    @Nullable
    public static GameProfile readGameProfile(CompoundTag param0) {
        String var0 = null;
        UUID var1 = null;
        if (param0.contains("Name", 8)) {
            var0 = param0.getString("Name");
        }

        if (param0.hasUUID("Id")) {
            var1 = param0.getUUID("Id");
        }

        try {
            GameProfile var2 = new GameProfile(var1, var0);
            if (param0.contains("Properties", 10)) {
                CompoundTag var3 = param0.getCompound("Properties");

                for(String var4 : var3.getAllKeys()) {
                    ListTag var5 = var3.getList(var4, 10);

                    for(int var6 = 0; var6 < var5.size(); ++var6) {
                        CompoundTag var7 = var5.getCompound(var6);
                        String var8 = var7.getString("Value");
                        if (var7.contains("Signature", 8)) {
                            var2.getProperties().put(var4, new com.mojang.authlib.properties.Property(var4, var8, var7.getString("Signature")));
                        } else {
                            var2.getProperties().put(var4, new com.mojang.authlib.properties.Property(var4, var8));
                        }
                    }
                }
            }

            return var2;
        } catch (Throwable var11) {
            return null;
        }
    }

    public static CompoundTag writeGameProfile(CompoundTag param0, GameProfile param1) {
        if (!StringUtil.isNullOrEmpty(param1.getName())) {
            param0.putString("Name", param1.getName());
        }

        if (param1.getId() != null) {
            param0.putUUID("Id", param1.getId());
        }

        if (!param1.getProperties().isEmpty()) {
            CompoundTag var0 = new CompoundTag();

            for(String var1 : param1.getProperties().keySet()) {
                ListTag var2 = new ListTag();

                for(com.mojang.authlib.properties.Property var3 : param1.getProperties().get(var1)) {
                    CompoundTag var4 = new CompoundTag();
                    var4.putString("Value", var3.getValue());
                    if (var3.hasSignature()) {
                        var4.putString("Signature", var3.getSignature());
                    }

                    var2.add(var4);
                }

                var0.put(var1, var2);
            }

            param0.put("Properties", var0);
        }

        return param0;
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag param0, @Nullable Tag param1, boolean param2) {
        if (param0 == param1) {
            return true;
        } else if (param0 == null) {
            return true;
        } else if (param1 == null) {
            return false;
        } else if (!param0.getClass().equals(param1.getClass())) {
            return false;
        } else if (param0 instanceof CompoundTag var0) {
            CompoundTag var1 = (CompoundTag)param1;

            for(String var2 : var0.getAllKeys()) {
                Tag var3 = var0.get(var2);
                if (!compareNbt(var3, var1.get(var2), param2)) {
                    return false;
                }
            }

            return true;
        } else if (param0 instanceof ListTag var4 && param2) {
            ListTag var5 = (ListTag)param1;
            if (var4.isEmpty()) {
                return var5.isEmpty();
            } else {
                for(int var6 = 0; var6 < var4.size(); ++var6) {
                    Tag var7 = var4.get(var6);
                    boolean var8 = false;

                    for(int var9 = 0; var9 < var5.size(); ++var9) {
                        if (compareNbt(var7, var5.get(var9), param2)) {
                            var8 = true;
                            break;
                        }
                    }

                    if (!var8) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return param0.equals(param1);
        }
    }

    public static IntArrayTag createUUID(UUID param0) {
        return new IntArrayTag(SerializableUUID.uuidToIntArray(param0));
    }

    public static UUID loadUUID(Tag param0) {
        if (param0.getType() != IntArrayTag.TYPE) {
            throw new IllegalArgumentException(
                "Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + param0.getType().getName() + "."
            );
        } else {
            int[] var0 = ((IntArrayTag)param0).getAsIntArray();
            if (var0.length != 4) {
                throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + var0.length + ".");
            } else {
                return SerializableUUID.uuidFromIntArray(var0);
            }
        }
    }

    public static BlockPos readBlockPos(CompoundTag param0) {
        return new BlockPos(param0.getInt("X"), param0.getInt("Y"), param0.getInt("Z"));
    }

    public static CompoundTag writeBlockPos(BlockPos param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putInt("X", param0.getX());
        var0.putInt("Y", param0.getY());
        var0.putInt("Z", param0.getZ());
        return var0;
    }

    public static BlockState readBlockState(CompoundTag param0) {
        if (!param0.contains("Name", 8)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            Block var0 = Registry.BLOCK.get(new ResourceLocation(param0.getString("Name")));
            BlockState var1 = var0.defaultBlockState();
            if (param0.contains("Properties", 10)) {
                CompoundTag var2 = param0.getCompound("Properties");
                StateDefinition<Block, BlockState> var3 = var0.getStateDefinition();

                for(String var4 : var2.getAllKeys()) {
                    Property<?> var5 = var3.getProperty(var4);
                    if (var5 != null) {
                        var1 = setValueHelper(var1, var5, var4, var2, param0);
                    }
                }
            }

            return var1;
        }
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(
        S param0, Property<T> param1, String param2, CompoundTag param3, CompoundTag param4
    ) {
        Optional<T> var0 = param1.getValue(param3.getString(param2));
        if (var0.isPresent()) {
            return param0.setValue(param1, var0.get());
        } else {
            LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", param2, param3.getString(param2), param4.toString());
            return param0;
        }
    }

    public static CompoundTag writeBlockState(BlockState param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", Registry.BLOCK.getKey(param0.getBlock()).toString());
        ImmutableMap<Property<?>, Comparable<?>> var1 = param0.getValues();
        if (!var1.isEmpty()) {
            CompoundTag var2 = new CompoundTag();

            for(Entry<Property<?>, Comparable<?>> var3 : var1.entrySet()) {
                Property<?> var4 = var3.getKey();
                var2.putString(var4.getName(), getName(var4, var3.getValue()));
            }

            var0.put("Properties", var2);
        }

        return var0;
    }

    public static CompoundTag writeFluidState(FluidState param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", Registry.FLUID.getKey(param0.getType()).toString());
        ImmutableMap<Property<?>, Comparable<?>> var1 = param0.getValues();
        if (!var1.isEmpty()) {
            CompoundTag var2 = new CompoundTag();

            for(Entry<Property<?>, Comparable<?>> var3 : var1.entrySet()) {
                Property<?> var4 = var3.getKey();
                var2.putString(var4.getName(), getName(var4, var3.getValue()));
            }

            var0.put("Properties", var2);
        }

        return var0;
    }

    private static <T extends Comparable<T>> String getName(Property<T> param0, Comparable<?> param1) {
        return param0.getName((T)param1);
    }

    public static String prettyPrint(Tag param0) {
        return prettyPrint(param0, false);
    }

    public static String prettyPrint(Tag param0, boolean param1) {
        return prettyPrint(new StringBuilder(), param0, 0, param1).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder param0, Tag param1, int param2, boolean param3) {
        switch(param1.getId()) {
            case 0:
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
                param0.append(param1);
                break;
            case 7:
                ByteArrayTag var0 = (ByteArrayTag)param1;
                byte[] var1 = var0.getAsByteArray();
                int var2 = var1.length;
                indent(param2, param0).append("byte[").append(var2).append("] {\n");
                if (!param3) {
                    indent(param2 + 1, param0).append(" // Skipped, supply withBinaryBlobs true");
                } else {
                    indent(param2 + 1, param0);

                    for(int var3 = 0; var3 < var1.length; ++var3) {
                        if (var3 != 0) {
                            param0.append(',');
                        }

                        if (var3 % 16 == 0 && var3 / 16 > 0) {
                            param0.append('\n');
                            if (var3 < var1.length) {
                                indent(param2 + 1, param0);
                            }
                        } else if (var3 != 0) {
                            param0.append(' ');
                        }

                        param0.append(String.format("0x%02X", var1[var3] & 255));
                    }
                }

                param0.append('\n');
                indent(param2, param0).append('}');
                break;
            case 9:
                ListTag var4 = (ListTag)param1;
                int var5 = var4.size();
                int var6 = var4.getElementType();
                String var7 = var6 == 0 ? "undefined" : TagTypes.getType(var6).getPrettyName();
                indent(param2, param0).append("list<").append(var7).append(">[").append(var5).append("] [");
                if (var5 != 0) {
                    param0.append('\n');
                }

                for(int var8 = 0; var8 < var5; ++var8) {
                    if (var8 != 0) {
                        param0.append(",\n");
                    }

                    indent(param2 + 1, param0);
                    prettyPrint(param0, var4.get(var8), param2 + 1, param3);
                }

                if (var5 != 0) {
                    param0.append('\n');
                }

                indent(param2, param0).append(']');
                break;
            case 10:
                CompoundTag var15 = (CompoundTag)param1;
                List<String> var16 = Lists.newArrayList(var15.getAllKeys());
                Collections.sort(var16);
                indent(param2, param0).append('{');
                if (param0.length() - param0.lastIndexOf("\n") > 2 * (param2 + 1)) {
                    param0.append('\n');
                    indent(param2 + 1, param0);
                }

                int var17 = var16.stream().mapToInt(String::length).max().orElse(0);
                String var18 = Strings.repeat(" ", var17);

                for(int var19 = 0; var19 < var16.size(); ++var19) {
                    if (var19 != 0) {
                        param0.append(",\n");
                    }

                    String var20 = var16.get(var19);
                    indent(param2 + 1, param0).append('"').append(var20).append('"').append(var18, 0, var18.length() - var20.length()).append(": ");
                    prettyPrint(param0, var15.get(var20), param2 + 1, param3);
                }

                if (!var16.isEmpty()) {
                    param0.append('\n');
                }

                indent(param2, param0).append('}');
                break;
            case 11:
                IntArrayTag var9 = (IntArrayTag)param1;
                int[] var10 = var9.getAsIntArray();
                int var11 = 0;

                for(int var12 : var10) {
                    var11 = Math.max(var11, String.format("%X", var12).length());
                }

                int var13 = var10.length;
                indent(param2, param0).append("int[").append(var13).append("] {\n");
                if (!param3) {
                    indent(param2 + 1, param0).append(" // Skipped, supply withBinaryBlobs true");
                } else {
                    indent(param2 + 1, param0);

                    for(int var14 = 0; var14 < var10.length; ++var14) {
                        if (var14 != 0) {
                            param0.append(',');
                        }

                        if (var14 % 16 == 0 && var14 / 16 > 0) {
                            param0.append('\n');
                            if (var14 < var10.length) {
                                indent(param2 + 1, param0);
                            }
                        } else if (var14 != 0) {
                            param0.append(' ');
                        }

                        param0.append(String.format("0x%0" + var11 + "X", var10[var14]));
                    }
                }

                param0.append('\n');
                indent(param2, param0).append('}');
                break;
            case 12:
                LongArrayTag var21 = (LongArrayTag)param1;
                long[] var22 = var21.getAsLongArray();
                long var23 = 0L;

                for(long var24 : var22) {
                    var23 = Math.max(var23, (long)String.format("%X", var24).length());
                }

                long var25 = (long)var22.length;
                indent(param2, param0).append("long[").append(var25).append("] {\n");
                if (!param3) {
                    indent(param2 + 1, param0).append(" // Skipped, supply withBinaryBlobs true");
                } else {
                    indent(param2 + 1, param0);

                    for(int var26 = 0; var26 < var22.length; ++var26) {
                        if (var26 != 0) {
                            param0.append(',');
                        }

                        if (var26 % 16 == 0 && var26 / 16 > 0) {
                            param0.append('\n');
                            if (var26 < var22.length) {
                                indent(param2 + 1, param0);
                            }
                        } else if (var26 != 0) {
                            param0.append(' ');
                        }

                        param0.append(String.format("0x%0" + var23 + "X", var22[var26]));
                    }
                }

                param0.append('\n');
                indent(param2, param0).append('}');
                break;
            default:
                param0.append("<UNKNOWN :(>");
        }

        return param0;
    }

    private static StringBuilder indent(int param0, StringBuilder param1) {
        int var0 = param1.lastIndexOf("\n") + 1;
        int var1 = param1.length() - var0;

        for(int var2 = 0; var2 < 2 * param0 - var1; ++var2) {
            param1.append(' ');
        }

        return param1;
    }

    public static CompoundTag update(DataFixer param0, DataFixTypes param1, CompoundTag param2, int param3) {
        return update(param0, param1, param2, param3, SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundTag update(DataFixer param0, DataFixTypes param1, CompoundTag param2, int param3, int param4) {
        return param0.update(param1.getType(), new Dynamic<>(NbtOps.INSTANCE, param2), param3, param4).getValue();
    }

    public static Component toPrettyComponent(Tag param0) {
        return new TextComponentTagVisitor("", 0).visit(param0);
    }

    public static String structureToSnbt(CompoundTag param0) {
        return new SnbtPrinterTagVisitor().visit(packStructureTemplate(param0));
    }

    public static CompoundTag snbtToStructure(String param0) throws CommandSyntaxException {
        return unpackStructureTemplate(TagParser.parseTag(param0));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag param0) {
        boolean var0 = param0.contains("palettes", 9);
        ListTag var1;
        if (var0) {
            var1 = param0.getList("palettes", 9).getList(0);
        } else {
            var1 = param0.getList("palette", 10);
        }

        ListTag var3 = var1.stream()
            .map(CompoundTag.class::cast)
            .map(NbtUtils::packBlockState)
            .map(StringTag::valueOf)
            .collect(Collectors.toCollection(ListTag::new));
        param0.put("palette", var3);
        if (var0) {
            ListTag var4 = new ListTag();
            ListTag var5 = param0.getList("palettes", 9);
            var5.stream().map(ListTag.class::cast).forEach(param2 -> {
                CompoundTag var0x = new CompoundTag();

                for(int var1x = 0; var1x < param2.size(); ++var1x) {
                    var0x.putString(var3.getString(var1x), packBlockState(param2.getCompound(var1x)));
                }

                var4.add(var0x);
            });
            param0.put("palettes", var4);
        }

        if (param0.contains("entities", 10)) {
            ListTag var6 = param0.getList("entities", 10);
            ListTag var7 = var6.stream()
                .map(CompoundTag.class::cast)
                .sorted(Comparator.comparing(param0x -> param0x.getList("pos", 6), YXZ_LISTTAG_DOUBLE_COMPARATOR))
                .collect(Collectors.toCollection(ListTag::new));
            param0.put("entities", var7);
        }

        ListTag var8 = param0.getList("blocks", 10)
            .stream()
            .map(CompoundTag.class::cast)
            .sorted(Comparator.comparing(param0x -> param0x.getList("pos", 3), YXZ_LISTTAG_INT_COMPARATOR))
            .peek(param1 -> param1.putString("state", var3.getString(param1.getInt("state"))))
            .collect(Collectors.toCollection(ListTag::new));
        param0.put("data", var8);
        param0.remove("blocks");
        return param0;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag param0) {
        ListTag var0 = param0.getList("palette", 8);
        Map<String, Tag> var1 = var0.stream()
            .map(StringTag.class::cast)
            .map(StringTag::getAsString)
            .collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
        if (param0.contains("palettes", 9)) {
            param0.put(
                "palettes",
                param0.getList("palettes", 10)
                    .stream()
                    .map(CompoundTag.class::cast)
                    .map(param1 -> var1.keySet().stream().map(param1::getString).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new)))
                    .collect(Collectors.toCollection(ListTag::new))
            );
            param0.remove("palette");
        } else {
            param0.put("palette", var1.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }

        if (param0.contains("data", 9)) {
            Object2IntMap<String> var2 = new Object2IntOpenHashMap<>();
            var2.defaultReturnValue(-1);

            for(int var3 = 0; var3 < var0.size(); ++var3) {
                var2.put(var0.getString(var3), var3);
            }

            ListTag var4 = param0.getList("data", 10);

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                CompoundTag var6 = var4.getCompound(var5);
                String var7 = var6.getString("state");
                int var8 = var2.getInt(var7);
                if (var8 == -1) {
                    throw new IllegalStateException("Entry " + var7 + " missing from palette");
                }

                var6.putInt("state", var8);
            }

            param0.put("blocks", var4);
            param0.remove("data");
        }

        return param0;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag param0x) {
        StringBuilder var0x = new StringBuilder(param0x.getString("Name"));
        if (param0x.contains("Properties", 10)) {
            CompoundTag var1x = param0x.getCompound("Properties");
            String var2x = var1x.getAllKeys().stream().sorted().map(param1 -> param1 + ":" + var1x.get(param1).getAsString()).collect(Collectors.joining(","));
            var0x.append('{').append(var2x).append('}');
        }

        return var0x.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String param0x) {
        CompoundTag var0x = new CompoundTag();
        int var1x = param0x.indexOf(123);
        String var2x;
        if (var1x >= 0) {
            var2x = param0x.substring(0, var1x);
            CompoundTag var3x = new CompoundTag();
            if (var1x + 2 <= param0x.length()) {
                String var4x = param0x.substring(var1x + 1, param0x.indexOf(125, var1x));
                COMMA_SPLITTER.split(var4x).forEach(param2 -> {
                    List<String> var0xx = COLON_SPLITTER.splitToList(param2);
                    if (var0xx.size() == 2) {
                        var3x.putString((String)var0xx.get(0), (String)var0xx.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", param0x);
                    }

                });
                var0x.put("Properties", var3x);
            }
        } else {
            var2x = param0x;
        }

        var0x.putString("Name", var2x);
        return var0x;
    }
}
