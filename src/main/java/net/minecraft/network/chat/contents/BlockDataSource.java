package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource {
    public static final MapCodec<BlockDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply(param0, BlockDataSource::new)
    );
    public static final DataSource.Type<BlockDataSource> TYPE = new DataSource.Type<>(SUB_CODEC, "block");

    public BlockDataSource(String param0) {
        this(param0, compilePos(param0));
    }

    @Nullable
    private static Coordinates compilePos(String param0) {
        try {
            return BlockPosArgument.blockPos().parse(new StringReader(param0));
        } catch (CommandSyntaxException var2) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack param0) {
        if (this.compiledPos != null) {
            ServerLevel var0 = param0.getLevel();
            BlockPos var1 = this.compiledPos.getBlockPos(param0);
            if (var0.isLoaded(var1)) {
                BlockEntity var2 = var0.getBlockEntity(var1);
                if (var2 != null) {
                    return Stream.of(var2.saveWithFullMetadata());
                }
            }
        }

        return Stream.empty();
    }

    @Override
    public DataSource.Type<?> type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "block=" + this.posPattern;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof BlockDataSource var0 && this.posPattern.equals(var0.posPattern)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.posPattern.hashCode();
    }
}
