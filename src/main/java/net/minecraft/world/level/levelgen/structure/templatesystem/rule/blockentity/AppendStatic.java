package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

public class AppendStatic implements RuleBlockEntityModifier {
    public static final Codec<AppendStatic> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(CompoundTag.CODEC.fieldOf("data").forGetter(param0x -> param0x.tag)).apply(param0, AppendStatic::new)
    );
    private final CompoundTag tag;

    public AppendStatic(CompoundTag param0) {
        this.tag = param0;
    }

    @Override
    public CompoundTag apply(RandomSource param0, @Nullable CompoundTag param1) {
        return param1 == null ? this.tag.copy() : param1.merge(this.tag);
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_STATIC;
    }
}
