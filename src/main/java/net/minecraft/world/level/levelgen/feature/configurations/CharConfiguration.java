package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.math.OctahedralGroup;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class CharConfiguration implements FeatureConfiguration {
    public final BlockStateProvider material;
    public final char ch;
    public final OctahedralGroup orientation;
    private static final Char2ObjectMap<byte[]> CONTENTS = createContents();
    private static final byte[] REPLACEMENT = CONTENTS.get('\ufffd');

    public static Char2ObjectMap<byte[]> createContents() {
        Char2ObjectOpenHashMap<byte[]> var0 = new Char2ObjectOpenHashMap<>();

        try (InputStream var1 = CharConfiguration.class.getResourceAsStream("/chars.bin")) {
            DataInputStream var2 = new DataInputStream(var1);

            while(true) {
                char var3 = var2.readChar();
                byte[] var4 = new byte[8];
                if (var2.read(var4) != 8) {
                    break;
                }

                var0.put(var3, var4);
            }
        } catch (EOFException var17) {
        } catch (IOException var18) {
        }

        return var0;
    }

    @Nullable
    public byte[] getBytes() {
        return CONTENTS.getOrDefault(this.ch, REPLACEMENT);
    }

    public CharConfiguration(BlockStateProvider param0, char param1, OctahedralGroup param2) {
        this.material = param0;
        this.ch = param1;
        this.orientation = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("material"),
                    this.material.serialize(param0),
                    param0.createString("char"),
                    param0.createInt(this.ch),
                    param0.createString("orientation"),
                    param0.createString(this.orientation.getSerializedName())
                )
            )
        );
    }

    public static <T> CharConfiguration deserialize(Dynamic<T> param0) {
        BlockStateProvider var0 = param0.get("material")
            .map(
                param0x -> {
                    ResourceLocation var0x = param0x.get("type").asString().map(ResourceLocation::new).get();
                    BlockStateProviderType<?> var1x = Registry.BLOCKSTATE_PROVIDER_TYPES
                        .getOptional(var0x)
                        .orElseThrow(() -> new IllegalStateException(var0x.toString()));
                    return var1x.deserialize(param0x);
                }
            )
            .orElseThrow(IllegalStateException::new);
        char var1 = (char)param0.asInt(66);
        OctahedralGroup var2 = param0.get("orientation").asString().flatMap(OctahedralGroup::byName).orElse(OctahedralGroup.IDENTITY);
        return new CharConfiguration(var0, var1, var2);
    }

    public static CharConfiguration random(Random param0) {
        BlockStateProvider var0 = BlockStateProvider.random(param0);
        char var1 = Util.randomObject(param0, ImmutableList.copyOf(CONTENTS.keySet()));
        OctahedralGroup var2 = Util.randomObject(param0, OctahedralGroup.values());
        return new CharConfiguration(var0, var1, var2);
    }
}
