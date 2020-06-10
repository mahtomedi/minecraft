package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttributeMap {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<Attribute, AttributeInstance> attributes = Maps.newHashMap();
    private final Set<AttributeInstance> dirtyAttributes = Sets.newHashSet();
    private final AttributeSupplier supplier;

    public AttributeMap(AttributeSupplier param0) {
        this.supplier = param0;
    }

    private void onAttributeModified(AttributeInstance param0) {
        if (param0.getAttribute().isClientSyncable()) {
            this.dirtyAttributes.add(param0);
        }

    }

    public Set<AttributeInstance> getDirtyAttributes() {
        return this.dirtyAttributes;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        return this.attributes.values().stream().filter(param0 -> param0.getAttribute().isClientSyncable()).collect(Collectors.toList());
    }

    @Nullable
    public AttributeInstance getInstance(Attribute param0) {
        return this.attributes.computeIfAbsent(param0, param0x -> this.supplier.createInstance(this::onAttributeModified, param0x));
    }

    public boolean hasAttribute(Attribute param0) {
        return this.attributes.get(param0) != null || this.supplier.hasAttribute(param0);
    }

    public boolean hasModifier(Attribute param0, UUID param1) {
        AttributeInstance var0 = this.attributes.get(param0);
        return var0 != null ? var0.getModifier(param1) != null : this.supplier.hasModifier(param0, param1);
    }

    public double getValue(Attribute param0) {
        AttributeInstance var0 = this.attributes.get(param0);
        return var0 != null ? var0.getValue() : this.supplier.getValue(param0);
    }

    public double getBaseValue(Attribute param0) {
        AttributeInstance var0 = this.attributes.get(param0);
        return var0 != null ? var0.getBaseValue() : this.supplier.getBaseValue(param0);
    }

    public double getModifierValue(Attribute param0, UUID param1) {
        AttributeInstance var0 = this.attributes.get(param0);
        return var0 != null ? var0.getModifier(param1).getAmount() : this.supplier.getModifierValue(param0, param1);
    }

    public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> param0) {
        param0.asMap().forEach((param0x, param1) -> {
            AttributeInstance var0 = this.attributes.get(param0x);
            if (var0 != null) {
                param1.forEach(var0::removeModifier);
            }

        });
    }

    public void addTransientAttributeModifiers(Multimap<Attribute, AttributeModifier> param0) {
        param0.forEach((param0x, param1) -> {
            AttributeInstance var0 = this.getInstance(param0x);
            if (var0 != null) {
                var0.removeModifier(param1);
                var0.addTransientModifier(param1);
            }

        });
    }

    @OnlyIn(Dist.CLIENT)
    public void assignValues(AttributeMap param0) {
        param0.attributes.values().forEach(param0x -> {
            AttributeInstance var0 = this.getInstance(param0x.getAttribute());
            if (var0 != null) {
                var0.replaceFrom(param0x);
            }

        });
    }

    public ListTag save() {
        ListTag var0 = new ListTag();

        for(AttributeInstance var1 : this.attributes.values()) {
            var0.add(var1.save());
        }

        return var0;
    }

    public void load(ListTag param0) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            CompoundTag var1 = param0.getCompound(var0);
            String var2 = var1.getString("Name");
            Util.ifElse(Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(var2)), param1 -> {
                AttributeInstance var0x = this.getInstance(param1);
                if (var0x != null) {
                    var0x.load(var1);
                }

            }, () -> LOGGER.warn("Ignoring unknown attribute '{}'", var2));
        }

    }
}
