package net.minecraft.client.resources.model;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public enum BlockModelRotation implements ModelState {
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private static final int DEGREES = 360;
    private static final Map<Integer, BlockModelRotation> BY_INDEX = Arrays.stream(values())
        .collect(Collectors.toMap(param0 -> param0.index, param0 -> param0));
    private final Transformation transformation;
    private final OctahedralGroup actualRotation;
    private final int index;

    private static int getIndex(int param0, int param1) {
        return param0 * 360 + param1;
    }

    private BlockModelRotation(int param0, int param1) {
        this.index = getIndex(param0, param1);
        Quaternionf param2 = new Quaternionf().rotateYXZ((float)(-param1) * (float) (Math.PI / 180.0), (float)(-param0) * (float) (Math.PI / 180.0), 0.0F);
        OctahedralGroup param3 = OctahedralGroup.IDENTITY;

        for(int var0 = 0; var0 < param1; var0 += 90) {
            param3 = param3.compose(OctahedralGroup.ROT_90_Y_NEG);
        }

        for(int var1 = 0; var1 < param0; var1 += 90) {
            param3 = param3.compose(OctahedralGroup.ROT_90_X_NEG);
        }

        this.transformation = new Transformation(null, param2, null, null);
        this.actualRotation = param3;
    }

    @Override
    public Transformation getRotation() {
        return this.transformation;
    }

    public static BlockModelRotation by(int param0, int param1) {
        return BY_INDEX.get(getIndex(Mth.positiveModulo(param0, 360), Mth.positiveModulo(param1, 360)));
    }

    public OctahedralGroup actualRotation() {
        return this.actualRotation;
    }
}
