package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.InsensitiveStringMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseAttributeMap {
    protected final Map<Attribute, AttributeInstance> attributesByObject = Maps.newHashMap();
    protected final Map<String, AttributeInstance> attributesByName = new InsensitiveStringMap<>();
    protected final Multimap<Attribute, Attribute> descendantsByParent = HashMultimap.create();

    @Nullable
    public AttributeInstance getInstance(Attribute param0) {
        return this.attributesByObject.get(param0);
    }

    @Nullable
    public AttributeInstance getInstance(String param0) {
        return this.attributesByName.get(param0);
    }

    public AttributeInstance registerAttribute(Attribute param0) {
        if (this.attributesByName.containsKey(param0.getName())) {
            throw new IllegalArgumentException("Attribute is already registered!");
        } else {
            AttributeInstance var0 = this.createAttributeInstance(param0);
            this.attributesByName.put(param0.getName(), var0);
            this.attributesByObject.put(param0, var0);

            for(Attribute var1 = param0.getParentAttribute(); var1 != null; var1 = var1.getParentAttribute()) {
                this.descendantsByParent.put(var1, param0);
            }

            return var0;
        }
    }

    protected abstract AttributeInstance createAttributeInstance(Attribute var1);

    public Collection<AttributeInstance> getAttributes() {
        return this.attributesByName.values();
    }

    public void onAttributeModified(AttributeInstance param0) {
    }

    public void removeAttributeModifiers(Multimap<String, AttributeModifier> param0) {
        for(Entry<String, AttributeModifier> var0 : param0.entries()) {
            AttributeInstance var1 = this.getInstance(var0.getKey());
            if (var1 != null) {
                var1.removeModifier(var0.getValue());
            }
        }

    }

    public void addAttributeModifiers(Multimap<String, AttributeModifier> param0) {
        for(Entry<String, AttributeModifier> var0 : param0.entries()) {
            AttributeInstance var1 = this.getInstance(var0.getKey());
            if (var1 != null) {
                var1.removeModifier(var0.getValue());
                var1.addModifier(var0.getValue());
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void assignValues(BaseAttributeMap param0) {
        this.getAttributes().forEach(param1 -> {
            AttributeInstance var0 = param0.getInstance(param1.getAttribute());
            if (var0 != null) {
                param1.copyFrom(var0);
            }

        });
    }
}
