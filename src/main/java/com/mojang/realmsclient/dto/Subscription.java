package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Subscription extends ValueObject {
    private static final Logger LOGGER = LogManager.getLogger();
    public long startDate;
    public int daysLeft;
    public Subscription.SubscriptionType type = Subscription.SubscriptionType.NORMAL;

    public static Subscription parse(String param0) {
        Subscription var0 = new Subscription();

        try {
            JsonParser var1 = new JsonParser();
            JsonObject var2 = var1.parse(param0).getAsJsonObject();
            var0.startDate = JsonUtils.getLongOr("startDate", var2, 0L);
            var0.daysLeft = JsonUtils.getIntOr("daysLeft", var2, 0);
            var0.type = typeFrom(JsonUtils.getStringOr("subscriptionType", var2, Subscription.SubscriptionType.NORMAL.name()));
        } catch (Exception var4) {
            LOGGER.error("Could not parse Subscription: " + var4.getMessage());
        }

        return var0;
    }

    private static Subscription.SubscriptionType typeFrom(String param0) {
        try {
            return Subscription.SubscriptionType.valueOf(param0);
        } catch (Exception var2) {
            return Subscription.SubscriptionType.NORMAL;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum SubscriptionType {
        NORMAL,
        RECURRING;
    }
}
