package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NbtUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    public static GameProfile readGameProfile(CompoundTag param0) {
        String var0 = null;
        String var1 = null;
        if (param0.contains("Name", 8)) {
            var0 = param0.getString("Name");
        }

        if (param0.contains("Id", 8)) {
            var1 = param0.getString("Id");
        }

        try {
            UUID var2;
            try {
                var2 = UUID.fromString(var1);
            } catch (Throwable var12) {
                var2 = null;
            }

            GameProfile var5 = new GameProfile(var2, var0);
            if (param0.contains("Properties", 10)) {
                CompoundTag var6 = param0.getCompound("Properties");

                for(String var7 : var6.getAllKeys()) {
                    ListTag var8 = var6.getList(var7, 10);

                    for(int var9 = 0; var9 < var8.size(); ++var9) {
                        CompoundTag var10 = var8.getCompound(var9);
                        String var11 = var10.getString("Value");
                        if (var10.contains("Signature", 8)) {
                            var5.getProperties().put(var7, new com.mojang.authlib.properties.Property(var7, var11, var10.getString("Signature")));
                        } else {
                            var5.getProperties().put(var7, new com.mojang.authlib.properties.Property(var7, var11));
                        }
                    }
                }
            }

            return var5;
        } catch (Throwable var13) {
            return null;
        }
    }

    public static CompoundTag writeGameProfile(CompoundTag param0, GameProfile param1) {
        if (!StringUtil.isNullOrEmpty(param1.getName())) {
            param0.putString("Name", param1.getName());
        }

        if (param1.getId() != null) {
            param0.putString("Id", param1.getId().toString());
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
        } else if (param0 instanceof CompoundTag) {
            CompoundTag var0 = (CompoundTag)param0;
            CompoundTag var1 = (CompoundTag)param1;

            for(String var2 : var0.getAllKeys()) {
                Tag var3 = var0.get(var2);
                if (!compareNbt(var3, var1.get(var2), param2)) {
                    return false;
                }
            }

            return true;
        } else if (param0 instanceof ListTag && param2) {
            ListTag var4 = (ListTag)param0;
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

    public static CompoundTag createUUIDTag(UUID param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putLong("M", param0.getMostSignificantBits());
        var0.putLong("L", param0.getLeastSignificantBits());
        return var0;
    }

    public static UUID loadUUIDTag(CompoundTag param0) {
        return new UUID(param0.getLong("M"), param0.getLong("L"));
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

    private static <S extends StateHolder<S>, T extends Comparable<T>> S setValueHelper(
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

    private static <T extends Comparable<T>> String getName(Property<T> param0, Comparable<?> param1) {
        return param0.getName((T)param1);
    }

    public static CompoundTag update(DataFixer param0, DataFixTypes param1, CompoundTag param2, int param3) {
        return update(param0, param1, param2, param3, SharedConstants.getCurrentVersion().getWorldVersion());
    }

    public static CompoundTag update(DataFixer param0, DataFixTypes param1, CompoundTag param2, int param3, int param4) {
        return param0.update(param1.getType(), new Dynamic<>(NbtOps.INSTANCE, param2), param3, param4).getValue();
    }
}
