package net.minecraft.world.level.material;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface FluidState extends StateHolder<FluidState> {
    Fluid getType();

    default boolean isSource() {
        return this.getType().isSource(this);
    }

    default boolean isEmpty() {
        return this.getType().isEmpty();
    }

    default float getHeight(BlockGetter param0, BlockPos param1) {
        return this.getType().getHeight(this, param0, param1);
    }

    default float getOwnHeight() {
        return this.getType().getOwnHeight(this);
    }

    default int getAmount() {
        return this.getType().getAmount(this);
    }

    @OnlyIn(Dist.CLIENT)
    default boolean shouldRenderBackwardUpFace(BlockGetter param0, BlockPos param1) {
        for(int var0 = -1; var0 <= 1; ++var0) {
            for(int var1 = -1; var1 <= 1; ++var1) {
                BlockPos var2 = param1.offset(var0, 0, var1);
                FluidState var3 = param0.getFluidState(var2);
                if (!var3.getType().isSame(this.getType()) && !param0.getBlockState(var2).isSolidRender(param0, var2)) {
                    return true;
                }
            }
        }

        return false;
    }

    default void tick(Level param0, BlockPos param1) {
        this.getType().tick(param0, param1, this);
    }

    @OnlyIn(Dist.CLIENT)
    default void animateTick(Level param0, BlockPos param1, Random param2) {
        this.getType().animateTick(param0, param1, this, param2);
    }

    default boolean isRandomlyTicking() {
        return this.getType().isRandomlyTicking();
    }

    default void randomTick(Level param0, BlockPos param1, Random param2) {
        this.getType().randomTick(param0, param1, this, param2);
    }

    default Vec3 getFlow(BlockGetter param0, BlockPos param1) {
        return this.getType().getFlow(param0, param1, this);
    }

    default BlockState createLegacyBlock() {
        return this.getType().createLegacyBlock(this);
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    default ParticleOptions getDripParticle() {
        return this.getType().getDripParticle();
    }

    default boolean is(Tag<Fluid> param0) {
        return this.getType().is(param0);
    }

    default float getExplosionResistance() {
        return this.getType().getExplosionResistance();
    }

    default boolean canBeReplacedWith(BlockGetter param0, BlockPos param1, Fluid param2, Direction param3) {
        return this.getType().canBeReplacedWith(this, param0, param1, param2, param3);
    }

    static <T> Dynamic<T> serialize(DynamicOps<T> param0, FluidState param1) {
        ImmutableMap<Property<?>, Comparable<?>> var0 = param1.getValues();
        T var1;
        if (var0.isEmpty()) {
            var1 = param0.createMap(ImmutableMap.of(param0.createString("Name"), param0.createString(Registry.FLUID.getKey(param1.getType()).toString())));
        } else {
            var1 = param0.createMap(
                ImmutableMap.of(
                    param0.createString("Name"),
                    param0.createString(Registry.FLUID.getKey(param1.getType()).toString()),
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

    static <T> FluidState deserialize(Dynamic<T> param0) {
        Fluid var0 = Registry.FLUID.get(new ResourceLocation(param0.getElement("Name").flatMap(param0.getOps()::getStringValue).orElse("minecraft:empty")));
        Map<String, String> var1 = param0.get("Properties").asMap(param0x -> param0x.asString(""), param0x -> param0x.asString(""));
        FluidState var2 = var0.defaultFluidState();
        StateDefinition<Fluid, FluidState> var3 = var0.getStateDefinition();

        for(Entry<String, String> var4 : var1.entrySet()) {
            String var5 = var4.getKey();
            Property<?> var6 = var3.getProperty(var5);
            if (var6 != null) {
                var2 = StateHolder.setValueHelper(var2, var6, var5, param0.toString(), var4.getValue());
            }
        }

        return var2;
    }

    default VoxelShape getShape(BlockGetter param0, BlockPos param1) {
        return this.getType().getShape(this, param0, param1);
    }
}
