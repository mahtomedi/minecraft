package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface AttributeInstance {
    Attribute getAttribute();

    double getBaseValue();

    void setBaseValue(double var1);

    Set<AttributeModifier> getModifiers(AttributeModifier.Operation var1);

    Set<AttributeModifier> getModifiers();

    boolean hasModifier(AttributeModifier var1);

    @Nullable
    AttributeModifier getModifier(UUID var1);

    void addModifier(AttributeModifier var1);

    void removeModifier(AttributeModifier var1);

    void removeModifier(UUID var1);

    @OnlyIn(Dist.CLIENT)
    void removeModifiers();

    double getValue();

    @OnlyIn(Dist.CLIENT)
    default void copyFrom(AttributeInstance param0) {
        this.setBaseValue(param0.getBaseValue());
        Set<AttributeModifier> var0 = param0.getModifiers();
        Set<AttributeModifier> var1 = this.getModifiers();
        ImmutableSet<AttributeModifier> var2 = ImmutableSet.copyOf(Sets.difference(var0, var1));
        ImmutableSet<AttributeModifier> var3 = ImmutableSet.copyOf(Sets.difference(var1, var0));
        var2.forEach(this::addModifier);
        var3.forEach(this::removeModifier);
    }
}
