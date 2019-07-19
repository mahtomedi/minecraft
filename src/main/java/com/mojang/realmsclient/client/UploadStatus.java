package com.mojang.realmsclient.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UploadStatus {
    public volatile Long bytesWritten = 0L;
    public volatile Long totalBytes = 0L;
}
