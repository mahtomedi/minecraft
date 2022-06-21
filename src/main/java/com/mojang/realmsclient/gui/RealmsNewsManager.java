package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.util.RealmsPersistence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsNewsManager {
    private final RealmsPersistence newsLocalStorage;
    private boolean hasUnreadNews;
    private String newsLink;

    public RealmsNewsManager(RealmsPersistence param0) {
        this.newsLocalStorage = param0;
        RealmsPersistence.RealmsPersistenceData var0 = param0.read();
        this.hasUnreadNews = var0.hasUnreadNews;
        this.newsLink = var0.newsLink;
    }

    public boolean hasUnreadNews() {
        return this.hasUnreadNews;
    }

    public String newsLink() {
        return this.newsLink;
    }

    public void updateUnreadNews(RealmsNews param0) {
        RealmsPersistence.RealmsPersistenceData var0 = this.updateNewsStorage(param0);
        this.hasUnreadNews = var0.hasUnreadNews;
        this.newsLink = var0.newsLink;
    }

    private RealmsPersistence.RealmsPersistenceData updateNewsStorage(RealmsNews param0) {
        RealmsPersistence.RealmsPersistenceData var0 = new RealmsPersistence.RealmsPersistenceData();
        var0.newsLink = param0.newsLink;
        RealmsPersistence.RealmsPersistenceData var1 = this.newsLocalStorage.read();
        boolean var2 = var0.newsLink == null || var0.newsLink.equals(var1.newsLink);
        if (var2) {
            return var1;
        } else {
            var0.hasUnreadNews = true;
            this.newsLocalStorage.save(var0);
            return var0;
        }
    }
}
