package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;

public abstract class BanListEntry<T> extends StoredUserEntry<T> {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
    public static final String EXPIRES_NEVER = "forever";
    protected final Date created;
    protected final String source;
    @Nullable
    protected final Date expires;
    protected final String reason;

    public BanListEntry(@Nullable T param0, @Nullable Date param1, @Nullable String param2, @Nullable Date param3, @Nullable String param4) {
        super(param0);
        this.created = param1 == null ? new Date() : param1;
        this.source = param2 == null ? "(Unknown)" : param2;
        this.expires = param3;
        this.reason = param4 == null ? "Banned by an operator." : param4;
    }

    protected BanListEntry(@Nullable T param0, JsonObject param1) {
        super(param0);

        Date var0;
        try {
            var0 = param1.has("created") ? DATE_FORMAT.parse(param1.get("created").getAsString()) : new Date();
        } catch (ParseException var7) {
            var0 = new Date();
        }

        this.created = var0;
        this.source = param1.has("source") ? param1.get("source").getAsString() : "(Unknown)";

        Date var3;
        try {
            var3 = param1.has("expires") ? DATE_FORMAT.parse(param1.get("expires").getAsString()) : null;
        } catch (ParseException var6) {
            var3 = null;
        }

        this.expires = var3;
        this.reason = param1.has("reason") ? param1.get("reason").getAsString() : "Banned by an operator.";
    }

    public Date getCreated() {
        return this.created;
    }

    public String getSource() {
        return this.source;
    }

    @Nullable
    public Date getExpires() {
        return this.expires;
    }

    public String getReason() {
        return this.reason;
    }

    public abstract Component getDisplayName();

    @Override
    boolean hasExpired() {
        return this.expires == null ? false : this.expires.before(new Date());
    }

    @Override
    protected void serialize(JsonObject param0) {
        param0.addProperty("created", DATE_FORMAT.format(this.created));
        param0.addProperty("source", this.source);
        param0.addProperty("expires", this.expires == null ? "forever" : DATE_FORMAT.format(this.expires));
        param0.addProperty("reason", this.reason);
    }
}
