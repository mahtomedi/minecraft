package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CarverDebugSettings {
    public static final CarverDebugSettings DEFAULT = new CarverDebugSettings(false, Blocks.ACACIA_BUTTON.defaultBlockState());
    public static final Codec<CarverDebugSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.BOOL.optionalFieldOf("debug_mode", Boolean.valueOf(false)).forGetter(CarverDebugSettings::isDebugMode),
                    BlockState.CODEC.optionalFieldOf("air_state", DEFAULT.getAirState()).forGetter(CarverDebugSettings::getAirState)
                )
                .apply(param0, CarverDebugSettings::new)
    );
    private boolean debugMode;
    private final BlockState airState;

    public static CarverDebugSettings of(boolean param0, BlockState param1) {
        return new CarverDebugSettings(param0, param1);
    }

    private CarverDebugSettings(boolean param0, BlockState param1) {
        this.debugMode = param0;
        this.airState = param1;
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public BlockState getAirState() {
        return this.airState;
    }
}
