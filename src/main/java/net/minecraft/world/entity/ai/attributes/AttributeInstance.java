package net.minecraft.world.entity.ai.attributes;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface AttributeInstance {
    Attribute getAttribute();

    double getBaseValue();

    void setBaseValue(double var1);

    Collection<AttributeModifier> getModifiers(AttributeModifier.Operation var1);

    Collection<AttributeModifier> getModifiers();

    boolean hasModifier(AttributeModifier var1);

    @Nullable
    AttributeModifier getModifier(UUID var1);

    void addModifier(AttributeModifier var1);

    void removeModifier(AttributeModifier var1);

    void removeModifier(UUID var1);

    @OnlyIn(Dist.CLIENT)
    void removeModifiers();

    double getValue();
}
