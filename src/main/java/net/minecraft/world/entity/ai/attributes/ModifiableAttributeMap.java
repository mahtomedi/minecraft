package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.InsensitiveStringMap;

public class ModifiableAttributeMap extends BaseAttributeMap {
    private final Set<AttributeInstance> dirtyAttributes = Sets.newHashSet();
    protected final Map<String, AttributeInstance> attributesByLegacy = new InsensitiveStringMap<>();

    public ModifiableAttributeInstance getInstance(Attribute param0) {
        return (ModifiableAttributeInstance)super.getInstance(param0);
    }

    public ModifiableAttributeInstance getInstance(String param0) {
        AttributeInstance var0 = super.getInstance(param0);
        if (var0 == null) {
            var0 = this.attributesByLegacy.get(param0);
        }

        return (ModifiableAttributeInstance)var0;
    }

    @Override
    public AttributeInstance registerAttribute(Attribute param0) {
        AttributeInstance var0 = super.registerAttribute(param0);
        if (param0 instanceof RangedAttribute && ((RangedAttribute)param0).getImportLegacyName() != null) {
            this.attributesByLegacy.put(((RangedAttribute)param0).getImportLegacyName(), var0);
        }

        return var0;
    }

    @Override
    protected AttributeInstance createAttributeInstance(Attribute param0) {
        return new ModifiableAttributeInstance(this, param0);
    }

    @Override
    public void onAttributeModified(AttributeInstance param0) {
        if (param0.getAttribute().isClientSyncable()) {
            this.dirtyAttributes.add(param0);
        }

        for(Attribute var0 : this.descendantsByParent.get(param0.getAttribute())) {
            ModifiableAttributeInstance var1 = this.getInstance(var0);
            if (var1 != null) {
                var1.setDirty();
            }
        }

    }

    public Set<AttributeInstance> getDirtyAttributes() {
        return this.dirtyAttributes;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        Set<AttributeInstance> var0 = Sets.newHashSet();

        for(AttributeInstance var1 : this.getAttributes()) {
            if (var1.getAttribute().isClientSyncable()) {
                var0.add(var1);
            }
        }

        return var0;
    }
}
