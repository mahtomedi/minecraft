package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemTransforms {
    public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms();
    public final ItemTransform thirdPersonLeftHand;
    public final ItemTransform thirdPersonRightHand;
    public final ItemTransform firstPersonLeftHand;
    public final ItemTransform firstPersonRightHand;
    public final ItemTransform head;
    public final ItemTransform gui;
    public final ItemTransform ground;
    public final ItemTransform fixed;

    private ItemTransforms() {
        this(
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM,
            ItemTransform.NO_TRANSFORM
        );
    }

    public ItemTransforms(ItemTransforms param0) {
        this.thirdPersonLeftHand = param0.thirdPersonLeftHand;
        this.thirdPersonRightHand = param0.thirdPersonRightHand;
        this.firstPersonLeftHand = param0.firstPersonLeftHand;
        this.firstPersonRightHand = param0.firstPersonRightHand;
        this.head = param0.head;
        this.gui = param0.gui;
        this.ground = param0.ground;
        this.fixed = param0.fixed;
    }

    public ItemTransforms(
        ItemTransform param0,
        ItemTransform param1,
        ItemTransform param2,
        ItemTransform param3,
        ItemTransform param4,
        ItemTransform param5,
        ItemTransform param6,
        ItemTransform param7
    ) {
        this.thirdPersonLeftHand = param0;
        this.thirdPersonRightHand = param1;
        this.firstPersonLeftHand = param2;
        this.firstPersonRightHand = param3;
        this.head = param4;
        this.gui = param5;
        this.ground = param6;
        this.fixed = param7;
    }

    public ItemTransform getTransform(ItemDisplayContext param0) {
        return switch(param0) {
            case THIRD_PERSON_LEFT_HAND -> this.thirdPersonLeftHand;
            case THIRD_PERSON_RIGHT_HAND -> this.thirdPersonRightHand;
            case FIRST_PERSON_LEFT_HAND -> this.firstPersonLeftHand;
            case FIRST_PERSON_RIGHT_HAND -> this.firstPersonRightHand;
            case HEAD -> this.head;
            case GUI -> this.gui;
            case GROUND -> this.ground;
            case FIXED -> this.fixed;
            default -> ItemTransform.NO_TRANSFORM;
        };
    }

    public boolean hasTransform(ItemDisplayContext param0) {
        return this.getTransform(param0) != ItemTransform.NO_TRANSFORM;
    }

    @OnlyIn(Dist.CLIENT)
    protected static class Deserializer implements JsonDeserializer<ItemTransforms> {
        public ItemTransforms deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            ItemTransform var1 = this.getTransform(param2, var0, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            ItemTransform var2 = this.getTransform(param2, var0, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
            if (var2 == ItemTransform.NO_TRANSFORM) {
                var2 = var1;
            }

            ItemTransform var3 = this.getTransform(param2, var0, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
            ItemTransform var4 = this.getTransform(param2, var0, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
            if (var4 == ItemTransform.NO_TRANSFORM) {
                var4 = var3;
            }

            ItemTransform var5 = this.getTransform(param2, var0, ItemDisplayContext.HEAD);
            ItemTransform var6 = this.getTransform(param2, var0, ItemDisplayContext.GUI);
            ItemTransform var7 = this.getTransform(param2, var0, ItemDisplayContext.GROUND);
            ItemTransform var8 = this.getTransform(param2, var0, ItemDisplayContext.FIXED);
            return new ItemTransforms(var2, var1, var4, var3, var5, var6, var7, var8);
        }

        private ItemTransform getTransform(JsonDeserializationContext param0, JsonObject param1, ItemDisplayContext param2) {
            String var0 = param2.getSerializedName();
            return param1.has(var0) ? param0.deserialize(param1.get(var0), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
        }
    }
}
