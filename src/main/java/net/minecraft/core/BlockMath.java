package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import org.slf4j.Logger;

public class BlockMath {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL = Util.make(Maps.newEnumMap(Direction.class), param0 -> {
        param0.put(Direction.SOUTH, Transformation.identity());
        param0.put(Direction.EAST, new Transformation(null, Vector3f.YP.rotationDegrees(90.0F), null, null));
        param0.put(Direction.WEST, new Transformation(null, Vector3f.YP.rotationDegrees(-90.0F), null, null));
        param0.put(Direction.NORTH, new Transformation(null, Vector3f.YP.rotationDegrees(180.0F), null, null));
        param0.put(Direction.UP, new Transformation(null, Vector3f.XP.rotationDegrees(-90.0F), null, null));
        param0.put(Direction.DOWN, new Transformation(null, Vector3f.XP.rotationDegrees(90.0F), null, null));
    });
    public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL = Util.make(Maps.newEnumMap(Direction.class), param0 -> {
        for(Direction var0 : Direction.values()) {
            param0.put(var0, VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(var0).inverse());
        }

    });

    public static Transformation blockCenterToCorner(Transformation param0) {
        Matrix4f var0 = Matrix4f.createTranslateMatrix(0.5F, 0.5F, 0.5F);
        var0.multiply(param0.getMatrix());
        var0.multiply(Matrix4f.createTranslateMatrix(-0.5F, -0.5F, -0.5F));
        return new Transformation(var0);
    }

    public static Transformation blockCornerToCenter(Transformation param0) {
        Matrix4f var0 = Matrix4f.createTranslateMatrix(-0.5F, -0.5F, -0.5F);
        var0.multiply(param0.getMatrix());
        var0.multiply(Matrix4f.createTranslateMatrix(0.5F, 0.5F, 0.5F));
        return new Transformation(var0);
    }

    public static Transformation getUVLockTransform(Transformation param0, Direction param1, Supplier<String> param2) {
        Direction var0 = Direction.rotate(param0.getMatrix(), param1);
        Transformation var1 = param0.inverse();
        if (var1 == null) {
            LOGGER.warn(param2.get());
            return new Transformation(null, null, new Vector3f(0.0F, 0.0F, 0.0F), null);
        } else {
            Transformation var2 = VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL.get(param1).compose(var1).compose(VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(var0));
            return blockCenterToCorner(var2);
        }
    }
}
