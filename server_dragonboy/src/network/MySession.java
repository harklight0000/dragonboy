package network;

import player.PlayerDAO;
import item.Item;
import player.Player;
import server.*;
import services.Service;
import logger.NLogger;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySession extends Session {

    public static final byte[] KEYS = {0};
    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    public Player player;
    public byte timeWait = 100;
    public boolean sentKey;
    public byte curR, curW;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel;

    public long lastTimeLogout;
    public boolean joinedGame;

    public long lastTimeReadMessage;

    public boolean actived;

    public boolean check;

    public int goldBar;
    public long gold;
    public int eventPoint;
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int vnd;
    public int tongnap;
    public int vip;
    public int luotquay;

    public boolean finishUpdate;

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    public void login(String username, String password) {
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        if (GameData.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRunning) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= GameData.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player == null) {
            Player pl = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;

                pl = PlayerDAO.login(this, al);
                if (pl != null) {
                    SessionService.sendSmallVersion(this);
                    SessionService.sendBgItemVersion(this);

                    this.timeWait = 0;
                    this.joinedGame = true;
                    pl.nPoint.calPoint();
                    pl.nPoint.setHp(pl.nPoint.hp);
                    pl.nPoint.setMp(pl.nPoint.mp);
                    pl.zone.addPlayer(pl);
                    if (pl.pet != null) {
                        pl.pet.nPoint.calPoint();
                        pl.pet.nPoint.setHp(pl.pet.nPoint.hp);
                        pl.pet.nPoint.setMp(pl.pet.nPoint.mp);
                    }

                    pl.setSession(this);
                    Client.gI().put(pl);
                    this.player = pl;
                    SessionService.sendVersionGame(this);
                    SessionService.sendDataItemBG(this);
                    Controller.gI().sendInfo(this);
                    if (this.player.notify != null && !this.player.notify.equals("null") && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
                        Service.gI().sendThongBao(this.player, this.player.notify);
                        this.player.notify = null;
                    }
                    NLogger.logInformation(player.name + "[/" + this.ipAddress + "] joined the game");
                }
            } catch (Exception e) {
                if (pl != null) {
                    pl.dispose();
                }
            }
        }
    }
}
