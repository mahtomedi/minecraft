package net.minecraft.network.protocol.game;

import java.util.Random;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugVillagerNameGenerator {
    private static final String[] NAMES_FIRST_PART = new String[]{
        "Slim", "Far", "River", "Silly", "Fat", "Thin", "Fish", "Bat", "Dark", "Oak", "Sly", "Bush", "Zen", "Bark", "Cry", "Slack", "Soup", "Grim", "Hook"
    };
    private static final String[] NAMES_SECOND_PART = new String[]{
        "Fox",
        "Tail",
        "Jaw",
        "Whisper",
        "Twig",
        "Root",
        "Finder",
        "Nose",
        "Brow",
        "Blade",
        "Fry",
        "Seek",
        "Tooth",
        "Foot",
        "Leaf",
        "Stone",
        "Fall",
        "Face",
        "Tongue"
    };

    public static String getVillagerName(UUID param0) {
        Random var0 = getRandom(param0);
        return getRandomString(var0, NAMES_FIRST_PART) + getRandomString(var0, NAMES_SECOND_PART);
    }

    private static String getRandomString(Random param0, String[] param1) {
        return param1[param0.nextInt(param1.length)];
    }

    private static Random getRandom(UUID param0) {
        return new Random((long)(param0.hashCode() >> 2));
    }
}
