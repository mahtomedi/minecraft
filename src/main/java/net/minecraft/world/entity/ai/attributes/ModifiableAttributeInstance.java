package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModifiableAttributeInstance implements AttributeInstance {
    private final BaseAttributeMap attributeMap;
    private final Attribute attribute;
    private final Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiers = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<String, Set<AttributeModifier>> modifiersByName = Maps.newHashMap();
    private final Map<UUID, AttributeModifier> modifierById = Maps.newHashMap();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;

    public ModifiableAttributeInstance(BaseAttributeMap param0, Attribute param1) {
        this.attributeMap = param0;
        this.attribute = param1;
        this.baseValue = param1.getDefaultValue();

        for(AttributeModifier.Operation var0 : AttributeModifier.Operation.values()) {
            this.modifiers.put(var0, Sets.newHashSet());
        }

    }

    @Override
    public Attribute getAttribute() {
        return this.attribute;
    }

    @Override
    public double getBaseValue() {
        return this.baseValue;
    }

    @Override
    public void setBaseValue(double param0) {
        if (param0 != this.getBaseValue()) {
            this.baseValue = param0;
            this.setDirty();
        }
    }

    @Override
    public Set<AttributeModifier> getModifiers(AttributeModifier.Operation param0) {
        return this.modifiers.get(param0);
    }

    @Override
    public Set<AttributeModifier> getModifiers() {
        Set<AttributeModifier> var0 = Sets.newHashSet();

        for(AttributeModifier.Operation var1 : AttributeModifier.Operation.values()) {
            var0.addAll(this.getModifiers(var1));
        }

        return var0;
    }

    @Nullable
    @Override
    public AttributeModifier getModifier(UUID param0) {
        return this.modifierById.get(param0);
    }

    @Override
    public boolean hasModifier(AttributeModifier param0) {
        return this.modifierById.get(param0.getId()) != null;
    }

    @Override
    public void addModifier(AttributeModifier param0) {
        if (this.getModifier(param0.getId()) != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            Set<AttributeModifier> var0 = this.modifiersByName.computeIfAbsent(param0.getName(), param0x -> Sets.newHashSet());
            this.modifiers.get(param0.getOperation()).add(param0);
            var0.add(param0);
            this.modifierById.put(param0.getId(), param0);
            this.setDirty();
        }
    }

    protected void setDirty() {
        this.dirty = true;
        this.attributeMap.onAttributeModified(this);
    }

    @Override
    public void removeModifier(AttributeModifier param0) {
        for(AttributeModifier.Operation var0 : AttributeModifier.Operation.values()) {
            this.modifiers.get(var0).remove(param0);
        }

        Set<AttributeModifier> var1 = this.modifiersByName.get(param0.getName());
        if (var1 != null) {
            var1.remove(param0);
            if (var1.isEmpty()) {
                this.modifiersByName.remove(param0.getName());
            }
        }

        this.modifierById.remove(param0.getId());
        this.setDirty();
    }

    @Override
    public void removeModifier(UUID param0) {
        AttributeModifier var0 = this.getModifier(param0);
        if (var0 != null) {
            this.removeModifier(var0);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void removeModifiers() {
        Collection<AttributeModifier> var0 = this.getModifiers();
        if (var0 != null) {
            for(AttributeModifier var1 : Lists.newArrayList(var0)) {
                this.removeModifier(var1);
            }

        }
    }

    @Override
    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }

        return this.cachedValue;
    }

    private double calculateValue() {
        double var0 = this.getBaseValue();

        for(AttributeModifier var1 : this.getAppliedModifiers(AttributeModifier.Operation.ADDITION)) {
            var0 += var1.getAmount();
        }

        double var2 = var0;

        for(AttributeModifier var3 : this.getAppliedModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            var2 += var0 * var3.getAmount();
        }

        for(AttributeModifier var4 : this.getAppliedModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            var2 *= 1.0 + var4.getAmount();
        }

        return this.attribute.sanitizeValue(var2);
    }

    private Collection<AttributeModifier> getAppliedModifiers(AttributeModifier.Operation param0) {
        Set<AttributeModifier> var0 = Sets.newHashSet(this.getModifiers(param0));

        for(Attribute var1 = this.attribute.getParentAttribute(); var1 != null; var1 = var1.getParentAttribute()) {
            AttributeInstance var2 = this.attributeMap.getInstance(var1);
            if (var2 != null) {
                var0.addAll(var2.getModifiers(param0));
            }
        }

        return var0;
    }
}
