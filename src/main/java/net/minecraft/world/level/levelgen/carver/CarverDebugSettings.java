package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
    public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(
        false,
        Blocks.ACACIA_BUTTON.defaultBlockState(),
        Blocks.CANDLE.defaultBlockState(),
        Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
        Blocks.GLASS.defaultBlockState()
    );
    public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.BOOL.optionalFieldOf("debug_mode", Boolean.valueOf(false)).forGetter(CarverDebugSettings::isDebugMode),
                    BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState),
                    BlockState.CODEC.optionalFieldOf("water_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getWaterState),
                    BlockState.CODEC.optionalFieldOf("lava_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getLavaState),
                    BlockState.CODEC.optionalFieldOf("barrier_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getBarrierState)
                )
                .apply(param0, CarverDebugSettings::new)
    );
    private boolean debugMode;
    private final BlockState airState;
    private final BlockState waterState;
    private final BlockState lavaState;
    private final BlockState barrierState;

    public static CarverDebugSettings of(boolean param0, BlockState param1, BlockState param2, BlockState param3, BlockState param4) {
        return new CarverDebugSettings(param0, param1, param2, param3, param4);
    }

    public static CarverDebugSettings of(BlockState param0, BlockState param1, BlockState param2, BlockState param3) {
        return new CarverDebugSettings(false, param0, param1, param2, param3);
    }

    public static CarverDebugSettings of(boolean param0, BlockState param1) {
        return new CarverDebugSettings(param0, param1, DEFAULT.getWaterState(), DEFAULT.getLavaState(), DEFAULT.getBarrierState());
    }

    private CarverDebugSettings(boolean param0, BlockState param1, BlockState param2, BlockState param3, BlockState param4) {
        this.debugMode = param0;
        this.airState = param1;
        this.waterState = param2;
        this.lavaState = param3;
        this.barrierState = param4;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public BlockState getAirState() {
        return this.airState;
    }

    public BlockState getWaterState() {
        return this.waterState;
    }

    public BlockState getLavaState() {
        return this.lavaState;
    }

    public BlockState getBarrierState() {
        return this.barrierState;
    }
}
