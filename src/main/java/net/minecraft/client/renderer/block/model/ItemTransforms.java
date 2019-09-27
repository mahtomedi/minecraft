package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
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

    public ItemTransform getTransform(ItemTransforms.TransformType param0) {
        switch(param0) {
            case THIRD_PERSON_LEFT_HAND:
                return this.thirdPersonLeftHand;
            case THIRD_PERSON_RIGHT_HAND:
                return this.thirdPersonRightHand;
            case FIRST_PERSON_LEFT_HAND:
                return this.firstPersonLeftHand;
            case FIRST_PERSON_RIGHT_HAND:
                return this.firstPersonRightHand;
            case HEAD:
                return this.head;
            case GUI:
                return this.gui;
            case GROUND:
                return this.ground;
            case FIXED:
                return this.fixed;
            default:
                return ItemTransform.NO_TRANSFORM;
        }
    }

    public boolean hasTransform(ItemTransforms.TransformType param0) {
        return this.getTransform(param0) != ItemTransform.NO_TRANSFORM;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<ItemTransforms> {
        protected Deserializer() {
        }

        public ItemTransforms deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = param0.getAsJsonObject();
            ItemTransform var1 = this.getTransform(param2, var0, "thirdperson_righthand");
            ItemTransform var2 = this.getTransform(param2, var0, "thirdperson_lefthand");
            if (var2 == ItemTransform.NO_TRANSFORM) {
                var2 = var1;
            }

            ItemTransform var3 = this.getTransform(param2, var0, "firstperson_righthand");
            ItemTransform var4 = this.getTransform(param2, var0, "firstperson_lefthand");
            if (var4 == ItemTransform.NO_TRANSFORM) {
                var4 = var3;
            }

            ItemTransform var5 = this.getTransform(param2, var0, "head");
            ItemTransform var6 = this.getTransform(param2, var0, "gui");
            ItemTransform var7 = this.getTransform(param2, var0, "ground");
            ItemTransform var8 = this.getTransform(param2, var0, "fixed");
            return new ItemTransforms(var2, var1, var4, var3, var5, var6, var7, var8);
        }

        private ItemTransform getTransform(JsonDeserializationContext param0, JsonObject param1, String param2) {
            return param1.has(param2) ? param0.deserialize(param1.get(param2), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum TransformType {
        NONE,
        THIRD_PERSON_LEFT_HAND,
        THIRD_PERSON_RIGHT_HAND,
        FIRST_PERSON_LEFT_HAND,
        FIRST_PERSON_RIGHT_HAND,
        HEAD,
        GUI,
        GROUND,
        FIXED;
    }
}
