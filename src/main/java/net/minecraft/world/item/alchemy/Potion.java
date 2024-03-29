package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class Potion {
    @Nullable
    private final String name;
    private final ImmutableList<MobEffectInstance> effects;
    private final Holder.Reference<Potion> builtInRegistryHolder = BuiltInRegistries.POTION.createIntrusiveHolder(this);

    public static Potion byName(String param0) {
        return BuiltInRegistries.POTION.get(ResourceLocation.tryParse(param0));
    }

    public Potion(MobEffectInstance... param0) {
        this(null, param0);
    }

    public Potion(@Nullable String param0, MobEffectInstance... param1) {
        this.name = param0;
        this.effects = ImmutableList.copyOf(param1);
    }

    public String getName(String param0) {
        return param0 + (this.name == null ? BuiltInRegistries.POTION.getKey(this).getPath() : this.name);
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            for(MobEffectInstance var0 : this.effects) {
                if (var0.getEffect().isInstantenous()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Deprecated
    public Holder.Reference<Potion> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }
}
