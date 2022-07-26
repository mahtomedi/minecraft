package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

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

    public static String getEntityName(Entity param0) {
        if (param0 instanceof Player) {
            return param0.getName().getString();
        } else {
            Component var0 = param0.getCustomName();
            return var0 != null ? var0.getString() : getEntityName(param0.getUUID());
        }
    }

    public static String getEntityName(UUID param0) {
        RandomSource var0 = getRandom(param0);
        return getRandomString(var0, NAMES_FIRST_PART) + getRandomString(var0, NAMES_SECOND_PART);
    }

    private static String getRandomString(RandomSource param0, String[] param1) {
        return Util.getRandom(param1, param0);
    }

    private static RandomSource getRandom(UUID param0) {
        return RandomSource.create((long)(param0.hashCode() >> 2));
    }
}
