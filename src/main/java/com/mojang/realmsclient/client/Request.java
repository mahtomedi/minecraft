package com.mojang.realmsclient.client;

import com.mojang.realmsclient.exception.RealmsHttpException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Request<T extends Request<T>> {
    protected HttpURLConnection connection;
    private boolean connected;
    protected String url;
    private static final int DEFAULT_READ_TIMEOUT = 60000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final String IS_SNAPSHOT_KEY = "Is-Prerelease";
    private static final String COOKIE_KEY = "Cookie";

    public Request(String param0, int param1, int param2) {
        try {
            this.url = param0;
            Proxy var0 = RealmsClientConfig.getProxy();
            if (var0 != null) {
                this.connection = (HttpURLConnection)new URL(param0).openConnection(var0);
            } else {
                this.connection = (HttpURLConnection)new URL(param0).openConnection();
            }

            this.connection.setConnectTimeout(param1);
            this.connection.setReadTimeout(param2);
        } catch (MalformedURLException var5) {
            throw new RealmsHttpException(var5.getMessage(), var5);
        } catch (IOException var6) {
            throw new RealmsHttpException(var6.getMessage(), var6);
        }
    }

    public void cookie(String param0, String param1) {
        cookie(this.connection, param0, param1);
    }

    public static void cookie(HttpURLConnection param0, String param1, String param2) {
        String var0 = param0.getRequestProperty("Cookie");
        if (var0 == null) {
            param0.setRequestProperty("Cookie", param1 + "=" + param2);
        } else {
            param0.setRequestProperty("Cookie", var0 + ";" + param1 + "=" + param2);
        }

    }

    public void addSnapshotHeader(boolean param0) {
        this.connection.addRequestProperty("Is-Prerelease", String.valueOf(param0));
    }

    public int getRetryAfterHeader() {
        return getRetryAfterHeader(this.connection);
    }

    public static int getRetryAfterHeader(HttpURLConnection param0) {
        String var0 = param0.getHeaderField("Retry-After");

        try {
            return Integer.valueOf(var0);
        } catch (Exception var3) {
            return 5;
        }
    }

    public int responseCode() {
        try {
            this.connect();
            return this.connection.getResponseCode();
        } catch (Exception var2) {
            throw new RealmsHttpException(var2.getMessage(), var2);
        }
    }

    public String text() {
        try {
            this.connect();
            String var0;
            if (this.responseCode() >= 400) {
                var0 = this.read(this.connection.getErrorStream());
            } else {
                var0 = this.read(this.connection.getInputStream());
            }

            this.dispose();
            return var0;
        } catch (IOException var21) {
            throw new RealmsHttpException(var21.getMessage(), var21);
        }
    }

    private String read(@Nullable InputStream param0) throws IOException {
        if (param0 == null) {
            return "";
        } else {
            InputStreamReader var0 = new InputStreamReader(param0, StandardCharsets.UTF_8);
            StringBuilder var1 = new StringBuilder();

            for(int var2 = var0.read(); var2 != -1; var2 = var0.read()) {
                var1.append((char)var2);
            }

            return var1.toString();
        }
    }

    private void dispose() {
        byte[] var0 = new byte[1024];

        try {
            InputStream var1 = this.connection.getInputStream();

            while(var1.read(var0) > 0) {
            }

            var1.close();
            return;
        } catch (Exception var9) {
            try {
                InputStream var3 = this.connection.getErrorStream();
                if (var3 != null) {
                    while(var3.read(var0) > 0) {
                    }

                    var3.close();
                    return;
                }
            } catch (IOException var8) {
                return;
            }
        } finally {
            if (this.connection != null) {
                this.connection.disconnect();
            }

        }

    }

    protected T connect() {
        if (this.connected) {
            return (T)this;
        } else {
            T var0 = this.doConnect();
            this.connected = true;
            return var0;
        }
    }

    protected abstract T doConnect();

    public static Request<?> get(String param0) {
        return new Request.Get(param0, 5000, 60000);
    }

    public static Request<?> get(String param0, int param1, int param2) {
        return new Request.Get(param0, param1, param2);
    }

    public static Request<?> post(String param0, String param1) {
        return new Request.Post(param0, param1, 5000, 60000);
    }

    public static Request<?> post(String param0, String param1, int param2, int param3) {
        return new Request.Post(param0, param1, param2, param3);
    }

    public static Request<?> delete(String param0) {
        return new Request.Delete(param0, 5000, 60000);
    }

    public static Request<?> put(String param0, String param1) {
        return new Request.Put(param0, param1, 5000, 60000);
    }

    public static Request<?> put(String param0, String param1, int param2, int param3) {
        return new Request.Put(param0, param1, param2, param3);
    }

    public String getHeader(String param0) {
        return getHeader(this.connection, param0);
    }

    public static String getHeader(HttpURLConnection param0, String param1) {
        try {
            return param0.getHeaderField(param1);
        } catch (Exception var3) {
            return "";
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Delete extends Request<Request.Delete> {
        public Delete(String param0, int param1, int param2) {
            super(param0, param1, param2);
        }

        public Request.Delete doConnect() {
            try {
                this.connection.setDoOutput(true);
                this.connection.setRequestMethod("DELETE");
                this.connection.connect();
                return this;
            } catch (Exception var2) {
                throw new RealmsHttpException(var2.getMessage(), var2);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Get extends Request<Request.Get> {
        public Get(String param0, int param1, int param2) {
            super(param0, param1, param2);
        }

        public Request.Get doConnect() {
            try {
                this.connection.setDoInput(true);
                this.connection.setDoOutput(true);
                this.connection.setUseCaches(false);
                this.connection.setRequestMethod("GET");
                return this;
            } catch (Exception var2) {
                throw new RealmsHttpException(var2.getMessage(), var2);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Post extends Request<Request.Post> {
        private final String content;

        public Post(String param0, String param1, int param2, int param3) {
            super(param0, param2, param3);
            this.content = param1;
        }

        public Request.Post doConnect() {
            try {
                if (this.content != null) {
                    this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                }

                this.connection.setDoInput(true);
                this.connection.setDoOutput(true);
                this.connection.setUseCaches(false);
                this.connection.setRequestMethod("POST");
                OutputStream var0 = this.connection.getOutputStream();
                OutputStreamWriter var1 = new OutputStreamWriter(var0, "UTF-8");
                var1.write(this.content);
                var1.close();
                var0.flush();
                return this;
            } catch (Exception var3) {
                throw new RealmsHttpException(var3.getMessage(), var3);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Put extends Request<Request.Put> {
        private final String content;

        public Put(String param0, String param1, int param2, int param3) {
            super(param0, param2, param3);
            this.content = param1;
        }

        public Request.Put doConnect() {
            try {
                if (this.content != null) {
                    this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                }

                this.connection.setDoOutput(true);
                this.connection.setDoInput(true);
                this.connection.setRequestMethod("PUT");
                OutputStream var0 = this.connection.getOutputStream();
                OutputStreamWriter var1 = new OutputStreamWriter(var0, "UTF-8");
                var1.write(this.content);
                var1.close();
                var0.flush();
                return this;
            } catch (Exception var3) {
                throw new RealmsHttpException(var3.getMessage(), var3);
            }
        }
    }
}
