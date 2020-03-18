package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState extends BlockBehaviour.BlockStateBase {
    public BlockState(Block param0, ImmutableMap<Property<?>, Comparable<?>> param1) {
        super(param0, param1);
    }

    @Override
    protected BlockState asState() {
        return this;
    }

    public static <T> Dynamic<T> serialize(DynamicOps<T> param0, BlockState param1) {
        ImmutableMap<Property<?>, Comparable<?>> var0 = param1.getValues();
        T var1;
        if (var0.isEmpty()) {
            var1 = param0.createMap(ImmutableMap.of(param0.createString("Name"), param0.createString(Registry.BLOCK.getKey(param1.getBlock()).toString())));
        } else {
            var1 = param0.createMap(
                ImmutableMap.of(
                    param0.createString("Name"),
                    param0.createString(Registry.BLOCK.getKey(param1.getBlock()).toString()),
                    param0.createString("Properties"),
                    param0.createMap(
                        var0.entrySet()
                            .stream()
                            .map(
                                param1x -> Pair.of(
                                        param0.createString(param1x.getKey().getName()),
                                        param0.createString(StateHolder.getName(param1x.getKey(), param1x.getValue()))
                                    )
                            )
                            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                    )
                )
            );
        }

        return new Dynamic<>(param0, var1);
    }

    public static <T> BlockState deserialize(Dynamic<T> param0) {
        Block var0 = Registry.BLOCK.get(new ResourceLocation(param0.getElement("Name").flatMap(param0.getOps()::getStringValue).orElse("minecraft:air")));
        Map<String, String> var1 = param0.get("Properties").asMap(param0x -> param0x.asString(""), param0x -> param0x.asString(""));
        BlockState var2 = var0.defaultBlockState();
        StateDefinition<Block, BlockState> var3 = var0.getStateDefinition();

        for(Entry<String, String> var4 : var1.entrySet()) {
            String var5 = var4.getKey();
            Property<?> var6 = var3.getProperty(var5);
            if (var6 != null) {
                var2 = StateHolder.setValueHelper(var2, var6, var5, param0.toString(), var4.getValue());
            }
        }

        return var2;
    }
}
