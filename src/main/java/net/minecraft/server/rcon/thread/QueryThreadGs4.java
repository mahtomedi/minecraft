package net.minecraft.server.rcon.thread;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.rcon.NetworkDataOutputStream;
import net.minecraft.server.rcon.PktUtils;

public class QueryThreadGs4 extends GenericThread {
    private long lastChallengeCheck;
    private final int port;
    private final int serverPort;
    private final int maxPlayers;
    private final String serverName;
    private final String worldName;
    private DatagramSocket socket;
    private final byte[] buffer = new byte[1460];
    private DatagramPacket request;
    private final Map<SocketAddress, String> idents;
    private String hostIp;
    private String serverIp;
    private final Map<SocketAddress, QueryThreadGs4.RequestChallenge> validChallenges;
    private final long lastChallengeClean;
    private final NetworkDataOutputStream rulesResponse;
    private long lastRulesResponse;

    public QueryThreadGs4(ServerInterface param0) {
        super(param0, "Query Listener");
        this.port = param0.getProperties().queryPort;
        this.serverIp = param0.getServerIp();
        this.serverPort = param0.getServerPort();
        this.serverName = param0.getServerName();
        this.maxPlayers = param0.getMaxPlayers();
        this.worldName = param0.getLevelIdName();
        this.lastRulesResponse = 0L;
        this.hostIp = "0.0.0.0";
        if (!this.serverIp.isEmpty() && !this.hostIp.equals(this.serverIp)) {
            this.hostIp = this.serverIp;
        } else {
            this.serverIp = "0.0.0.0";

            try {
                InetAddress var0 = InetAddress.getLocalHost();
                this.hostIp = var0.getHostAddress();
            } catch (UnknownHostException var3) {
                this.warn("Unable to determine local host IP, please set server-ip in server.properties: " + var3.getMessage());
            }
        }

        this.idents = Maps.newHashMap();
        this.rulesResponse = new NetworkDataOutputStream(1460);
        this.validChallenges = Maps.newHashMap();
        this.lastChallengeClean = new Date().getTime();
    }

    private void sendTo(byte[] param0, DatagramPacket param1) throws IOException {
        this.socket.send(new DatagramPacket(param0, param0.length, param1.getSocketAddress()));
    }

    private boolean processPacket(DatagramPacket param0) throws IOException {
        byte[] var0 = param0.getData();
        int var1 = param0.getLength();
        SocketAddress var2 = param0.getSocketAddress();
        this.debug("Packet len " + var1 + " [" + var2 + "]");
        if (3 <= var1 && -2 == var0[0] && -3 == var0[1]) {
            this.debug("Packet '" + PktUtils.toHexString(var0[2]) + "' [" + var2 + "]");
            switch(var0[2]) {
                case 0:
                    if (!this.validChallenge(param0)) {
                        this.debug("Invalid challenge [" + var2 + "]");
                        return false;
                    } else if (15 == var1) {
                        this.sendTo(this.buildRuleResponse(param0), param0);
                        this.debug("Rules [" + var2 + "]");
                    } else {
                        NetworkDataOutputStream var3 = new NetworkDataOutputStream(1460);
                        var3.write(0);
                        var3.writeBytes(this.getIdentBytes(param0.getSocketAddress()));
                        var3.writeString(this.serverName);
                        var3.writeString("SMP");
                        var3.writeString(this.worldName);
                        var3.writeString(Integer.toString(this.currentPlayerCount()));
                        var3.writeString(Integer.toString(this.maxPlayers));
                        var3.writeShort((short)this.serverPort);
                        var3.writeString(this.hostIp);
                        this.sendTo(var3.toByteArray(), param0);
                        this.debug("Status [" + var2 + "]");
                    }
                default:
                    return true;
                case 9:
                    this.sendChallenge(param0);
                    this.debug("Challenge [" + var2 + "]");
                    return true;
            }
        } else {
            this.debug("Invalid packet [" + var2 + "]");
            return false;
        }
    }

    private byte[] buildRuleResponse(DatagramPacket param0) throws IOException {
        long var0 = Util.getMillis();
        if (var0 < this.lastRulesResponse + 5000L) {
            byte[] var1 = this.rulesResponse.toByteArray();
            byte[] var2 = this.getIdentBytes(param0.getSocketAddress());
            var1[1] = var2[0];
            var1[2] = var2[1];
            var1[3] = var2[2];
            var1[4] = var2[3];
            return var1;
        } else {
            this.lastRulesResponse = var0;
            this.rulesResponse.reset();
            this.rulesResponse.write(0);
            this.rulesResponse.writeBytes(this.getIdentBytes(param0.getSocketAddress()));
            this.rulesResponse.writeString("splitnum");
            this.rulesResponse.write(128);
            this.rulesResponse.write(0);
            this.rulesResponse.writeString("hostname");
            this.rulesResponse.writeString(this.serverName);
            this.rulesResponse.writeString("gametype");
            this.rulesResponse.writeString("SMP");
            this.rulesResponse.writeString("game_id");
            this.rulesResponse.writeString("MINECRAFT");
            this.rulesResponse.writeString("version");
            this.rulesResponse.writeString(this.serverInterface.getServerVersion());
            this.rulesResponse.writeString("plugins");
            this.rulesResponse.writeString(this.serverInterface.getPluginNames());
            this.rulesResponse.writeString("map");
            this.rulesResponse.writeString(this.worldName);
            this.rulesResponse.writeString("numplayers");
            this.rulesResponse.writeString("" + this.currentPlayerCount());
            this.rulesResponse.writeString("maxplayers");
            this.rulesResponse.writeString("" + this.maxPlayers);
            this.rulesResponse.writeString("hostport");
            this.rulesResponse.writeString("" + this.serverPort);
            this.rulesResponse.writeString("hostip");
            this.rulesResponse.writeString(this.hostIp);
            this.rulesResponse.write(0);
            this.rulesResponse.write(1);
            this.rulesResponse.writeString("player_");
            this.rulesResponse.write(0);
            String[] var3 = this.serverInterface.getPlayerNames();

            for(String var4 : var3) {
                this.rulesResponse.writeString(var4);
            }

            this.rulesResponse.write(0);
            return this.rulesResponse.toByteArray();
        }
    }

    private byte[] getIdentBytes(SocketAddress param0) {
        return this.validChallenges.get(param0).getIdentBytes();
    }

    private Boolean validChallenge(DatagramPacket param0) {
        SocketAddress var0 = param0.getSocketAddress();
        if (!this.validChallenges.containsKey(var0)) {
            return false;
        } else {
            byte[] var1 = param0.getData();
            return this.validChallenges.get(var0).getChallenge() != PktUtils.intFromNetworkByteArray(var1, 7, param0.getLength()) ? false : true;
        }
    }

    private void sendChallenge(DatagramPacket param0) throws IOException {
        QueryThreadGs4.RequestChallenge var0 = new QueryThreadGs4.RequestChallenge(param0);
        this.validChallenges.put(param0.getSocketAddress(), var0);
        this.sendTo(var0.getChallengeBytes(), param0);
    }

    private void pruneChallenges() {
        if (this.running) {
            long var0 = Util.getMillis();
            if (var0 >= this.lastChallengeCheck + 30000L) {
                this.lastChallengeCheck = var0;
                Iterator<Entry<SocketAddress, QueryThreadGs4.RequestChallenge>> var1 = this.validChallenges.entrySet().iterator();

                while(var1.hasNext()) {
                    Entry<SocketAddress, QueryThreadGs4.RequestChallenge> var2 = var1.next();
                    if (var2.getValue().before(var0)) {
                        var1.remove();
                    }
                }

            }
        }
    }

    @Override
    public void run() {
        this.info("Query running on " + this.serverIp + ":" + this.port);
        this.lastChallengeCheck = Util.getMillis();
        this.request = new DatagramPacket(this.buffer, this.buffer.length);

        try {
            while(this.running) {
                try {
                    this.socket.receive(this.request);
                    this.pruneChallenges();
                    this.processPacket(this.request);
                } catch (SocketTimeoutException var7) {
                    this.pruneChallenges();
                } catch (PortUnreachableException var8) {
                } catch (IOException var9) {
                    this.recoverSocketError(var9);
                }
            }
        } finally {
            this.closeSockets();
        }

    }

    @Override
    public void start() {
        if (!this.running) {
            if (0 < this.port && 65535 >= this.port) {
                if (this.initSocket()) {
                    super.start();
                }

            } else {
                this.warn("Invalid query port " + this.port + " found in server.properties (queries disabled)");
            }
        }
    }

    private void recoverSocketError(Exception param0) {
        if (this.running) {
            this.warn("Unexpected exception, buggy JRE? (" + param0 + ")");
            if (!this.initSocket()) {
                this.error("Failed to recover from buggy JRE, shutting down!");
                this.running = false;
            }

        }
    }

    private boolean initSocket() {
        try {
            this.socket = new DatagramSocket(this.port, InetAddress.getByName(this.serverIp));
            this.registerSocket(this.socket);
            this.socket.setSoTimeout(500);
            return true;
        } catch (SocketException var2) {
            this.warn("Unable to initialise query system on " + this.serverIp + ":" + this.port + " (Socket): " + var2.getMessage());
        } catch (UnknownHostException var3) {
            this.warn("Unable to initialise query system on " + this.serverIp + ":" + this.port + " (Unknown Host): " + var3.getMessage());
        } catch (Exception var4) {
            this.warn("Unable to initialise query system on " + this.serverIp + ":" + this.port + " (E): " + var4.getMessage());
        }

        return false;
    }

    class RequestChallenge {
        private final long time = new Date().getTime();
        private final int challenge;
        private final byte[] identBytes;
        private final byte[] challengeBytes;
        private final String ident;

        public RequestChallenge(DatagramPacket param0) {
            byte[] param1 = param0.getData();
            this.identBytes = new byte[4];
            this.identBytes[0] = param1[3];
            this.identBytes[1] = param1[4];
            this.identBytes[2] = param1[5];
            this.identBytes[3] = param1[6];
            this.ident = new String(this.identBytes, StandardCharsets.UTF_8);
            this.challenge = new Random().nextInt(16777216);
            this.challengeBytes = String.format("\t%s%d\u0000", this.ident, this.challenge).getBytes(StandardCharsets.UTF_8);
        }

        public Boolean before(long param0) {
            return this.time < param0;
        }

        public int getChallenge() {
            return this.challenge;
        }

        public byte[] getChallengeBytes() {
            return this.challengeBytes;
        }

        public byte[] getIdentBytes() {
            return this.identBytes;
        }
    }
}
