package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AttributeInstance {
    private final Attribute attribute;
    private final Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<UUID, AttributeModifier> modifierById = new Object2ObjectArrayMap<>();
    private final Set<AttributeModifier> permanentModifiers = new ObjectArraySet<>();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;
    private final Consumer<AttributeInstance> onDirty;

    public AttributeInstance(Attribute param0, Consumer<AttributeInstance> param1) {
        this.attribute = param0;
        this.onDirty = param1;
        this.baseValue = param0.getDefaultValue();
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double param0) {
        if (param0 != this.baseValue) {
            this.baseValue = param0;
            this.setDirty();
        }
    }

    public Set<AttributeModifier> getModifiers(AttributeModifier.Operation param0) {
        return this.modifiersByOperation.computeIfAbsent(param0, param0x -> Sets.newHashSet());
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    @Nullable
    public AttributeModifier getModifier(UUID param0) {
        return this.modifierById.get(param0);
    }

    public boolean hasModifier(AttributeModifier param0) {
        return this.modifierById.get(param0.getId()) != null;
    }

    private void addModifier(AttributeModifier param0) {
        AttributeModifier var0 = this.modifierById.putIfAbsent(param0.getId(), param0);
        if (var0 != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            this.getModifiers(param0.getOperation()).add(param0);
            this.setDirty();
        }
    }

    public void addTransientModifier(AttributeModifier param0) {
        this.addModifier(param0);
    }

    public void addPermanentModifier(AttributeModifier param0) {
        this.addModifier(param0);
        this.permanentModifiers.add(param0);
    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier param0) {
        this.getModifiers(param0.getOperation()).remove(param0);
        this.modifierById.remove(param0.getId());
        this.permanentModifiers.remove(param0);
        this.setDirty();
    }

    public void removeModifier(UUID param0) {
        AttributeModifier var0 = this.getModifier(param0);
        if (var0 != null) {
            this.removeModifier(var0);
        }

    }

    public boolean removePermanentModifier(UUID param0) {
        AttributeModifier var0 = this.getModifier(param0);
        if (var0 != null && this.permanentModifiers.contains(var0)) {
            this.removeModifier(var0);
            return true;
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void removeModifiers() {
        for(AttributeModifier var0 : this.getModifiers()) {
            this.removeModifier(var0);
        }

    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }

        return this.cachedValue;
    }

    private double calculateValue() {
        double var0 = this.getBaseValue();

        for(AttributeModifier var1 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADDITION)) {
            var0 += var1.getAmount();
        }

        double var2 = var0;

        for(AttributeModifier var3 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
            var2 += var0 * var3.getAmount();
        }

        for(AttributeModifier var4 : this.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            var2 *= 1.0 + var4.getAmount();
        }

        return this.attribute.sanitizeValue(var2);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation param0) {
        return this.modifiersByOperation.getOrDefault(param0, Collections.emptySet());
    }

    public void replaceFrom(AttributeInstance param0) {
        this.baseValue = param0.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll(param0.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.addAll(param0.permanentModifiers);
        this.modifiersByOperation.clear();
        param0.modifiersByOperation.forEach((param0x, param1) -> this.getModifiers(param0x).addAll(param1));
        this.setDirty();
    }

    public CompoundTag save() {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", Registry.ATTRIBUTES.getKey(this.attribute).toString());
        var0.putDouble("Base", this.baseValue);
        if (!this.permanentModifiers.isEmpty()) {
            ListTag var1 = new ListTag();

            for(AttributeModifier var2 : this.permanentModifiers) {
                var1.add(var2.save());
            }

            var0.put("Modifiers", var1);
        }

        return var0;
    }

    public void load(CompoundTag param0) {
        this.baseValue = param0.getDouble("Base");
        if (param0.contains("Modifiers", 9)) {
            ListTag var0 = param0.getList("Modifiers", 10);

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                AttributeModifier var2 = AttributeModifier.load(var0.getCompound(var1));
                if (var2 != null) {
                    this.modifierById.put(var2.getId(), var2);
                    this.getModifiers(var2.getOperation()).add(var2);
                    this.permanentModifiers.add(var2);
                }
            }
        }

        this.setDirty();
    }
}
