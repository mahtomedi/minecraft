package net.minecraft.network.protocol.game;

import java.util.Random;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DebugEntityNameGenerator {
    private static final String[] NAMES_FIRST_PART = new String[]{
        "Slim",
        "Far",
        "River",
        "Silly",
        "Fat",
        "Thin",
        "Fish",
        "Bat",
        "Dark",
        "Oak",
        "Sly",
        "Bush",
        "Zen",
        "Bark",
        "Cry",
        "Slack",
        "Soup",
        "Grim",
        "Hook",
        "Dirt",
        "Mud",
        "Sad",
        "Hard",
        "Crook",
        "Sneak",
        "Stink",
        "Weird",
        "Fire",
        "Soot",
        "Soft",
        "Rough",
        "Cling",
        "Scar"
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
        "Wart",
        "Tooth",
        "Foot",
        "Leaf",
        "Stone",
        "Fall",
        "Face",
        "Tongue",
        "Voice",
        "Lip",
        "Mouth",
        "Snail",
        "Toe",
        "Ear",
        "Hair",
        "Beard",
        "Shirt",
        "Fist"
    };

    public static String getEntityName(UUID param0) {
        Random var0 = getRandom(param0);
        return getRandomString(var0, NAMES_FIRST_PART) + getRandomString(var0, NAMES_SECOND_PART);
    }

    private static String getRandomString(Random param0, String[] param1) {
        return Util.getRandom(param1, param0);
    }

    private static Random getRandom(UUID param0) {
        return new Random((long)(param0.hashCode() >> 2));
    }
}
