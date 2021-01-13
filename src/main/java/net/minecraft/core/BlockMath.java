package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BlockMath {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final EnumMap<Direction, Transformation> vanillaUvTransformLocalToGlobal = Util.make(Maps.newEnumMap(Direction.class), param0 -> {
        param0.put(Direction.SOUTH, Transformation.identity());
        param0.put(Direction.EAST, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 90.0F, true), null, null));
        param0.put(Direction.WEST, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), -90.0F, true), null, null));
        param0.put(Direction.NORTH, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 180.0F, true), null, null));
        param0.put(Direction.UP, new Transformation(null, new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), -90.0F, true), null, null));
        param0.put(Direction.DOWN, new Transformation(null, new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), 90.0F, true), null, null));
    });
    public static final EnumMap<Direction, Transformation> vanillaUvTransformGlobalToLocal = Util.make(Maps.newEnumMap(Direction.class), param0 -> {
        for(Direction var0 : Direction.values()) {
            param0.put(var0, vanillaUvTransformLocalToGlobal.get(var0).inverse());
        }

    });

    public static Transformation blockCenterToCorner(Transformation param0) {
        Matrix4f var0 = Matrix4f.createTranslateMatrix(0.5F, 0.5F, 0.5F);
        var0.multiply(param0.getMatrix());
        var0.multiply(Matrix4f.createTranslateMatrix(-0.5F, -0.5F, -0.5F));
        return new Transformation(var0);
    }

    public static Transformation getUVLockTransform(Transformation param0, Direction param1, Supplier<String> param2) {
        Direction var0 = Direction.rotate(param0.getMatrix(), param1);
        Transformation var1 = param0.inverse();
        if (var1 == null) {
            LOGGER.warn(param2.get());
            return new Transformation(null, null, new Vector3f(0.0F, 0.0F, 0.0F), null);
        } else {
            Transformation var2 = vanillaUvTransformGlobalToLocal.get(param1).compose(var1).compose(vanillaUvTransformLocalToGlobal.get(var0));
            return blockCenterToCorner(var2);
        }
    }
}
