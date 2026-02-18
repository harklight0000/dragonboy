package clan;

/*
 * YouTube Channel: Nguyễn Đức Vũ Entertainment
 * Zalo: 0838.494.182
 * Contacts: https://contacts.TanTaivip.pro
 */

import dungeon.DestronGas;
import dungeon.RedRibbonHQ;
import dungeon.SnakeWay;
import dungeon.TreasureUnderSea;
import player.PlayerDAO;
import network.Message;
import player.Player;
import server.Client;
import services.ClanService;
import services.Service;
import utils.TimeUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class Clan {

    public static final byte LEADER = 0;
    public static final byte DEPUTY = 1;
    public static final byte MEMBER = 2;
    public static int NEXT_ID = 0;
    public final List<ClanMember> members;
    public final List<Player> membersInGame;
    private final List<ClanMessage> clanMessages;
    public int clanMessageId = 0;
    public int id;
    public int imgId;
    public String name;
    public String name2;
    public String slogan;
    public int createTime;
    public long powerPoint;
    public byte maxMember;
    public int level;
    public boolean active;
    public int capsuleClan;
    public long lastTimeOpenDoanhTrai;
    public boolean haveGoneDoanhTrai;
    public RedRibbonHQ doanhTrai;
    public Player playerOpenDoanhTrai;
    public TreasureUnderSea BanDoKhoBau;
    public long lastTimeOpenBanDoKhoBau;
    public Player playerOpenBanDoKhoBau;

    public SnakeWay ConDuongRanDoc;
    public long lastTimeOpenConDuongRanDoc;
    public Player playerOpenConDuongRanDoc;

    public DestronGas KhiGasHuyDiet;
    public long lastTimeOpenKhiGasHuyDiet;
    public Player playerOpenKhiGasHuyDiet;
    public int timesPerDayKGHD;

    public long timeUpdateClan;

    public Clan() {
        this.id = NEXT_ID++;
        this.name = "";
        this.name2 = "";
        this.slogan = "";
        this.maxMember = 10;
        this.level = 1;
        this.createTime = (int) (System.currentTimeMillis() / 1000);
        this.members = new ArrayList<>();
        this.membersInGame = new ArrayList<>();
        this.clanMessages = new ArrayList<>();
    }

    public boolean canUpdateClan(Player player) {
        if (Util.canDoWithTime(timeUpdateClan, 60000)) {
            timeUpdateClan = System.currentTimeMillis();
            return true;
        }
        Service.gI().sendThongBao(player, "Vui lòng đợi " + TimeUtil.getTimeLeft(timeUpdateClan, 60) + " nữa");
        return false;
    }

    public ClanMember getLeader() {
        for (ClanMember cm : members) {
            if (cm.role == LEADER) {
                return cm;
            }
        }
        ClanMember cm = new ClanMember();
        cm.name = "Bang chủ";
        return cm;
    }

    public byte getRole(Player player) {
        for (ClanMember cm : members) {
            if (cm.id == player.id) {
                return cm.role;
            }
        }
        return -1;
    }

    public boolean isLeader(Player player) {
        for (ClanMember cm : members) {
            if (cm.id == player.id && cm.role == LEADER) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeputy(Player player) {
        for (ClanMember cm : members) {
            if (cm.id == player.id && cm.role == DEPUTY) {
                return true;
            }
        }
        return false;
    }

    public void addSMTNClan(Player plOri, long param) {
        for (int i = this.membersInGame.size() - 1; i >= 0; i--) {
            Player pl = this.membersInGame.get(i);

            if (pl != null && !plOri.equals(pl) && pl.zone != null && plOri.zone.equals(pl.zone)) {
                int levelOri = Service.gI().getCurrLevel(plOri); // Cấp độ người khởi xướng
                int levelTarget = Service.gI().getCurrLevel(pl); // Cấp độ mục tiêu
                int levelDiff = Math.abs(levelTarget - levelOri); // Chênh lệch cấp độ
                long tnsm = param / (levelDiff == 0 ? 1 : levelDiff);
                Service.gI().addSMTN(pl, (byte) 1, tnsm, false);
            }
        }
    }

    public void sendMessageClan(ClanMessage cmg) {
        Message msg;
        try {
            msg = new Message(-51);
            msg.writer().writeByte(cmg.type);
            msg.writer().writeInt(cmg.id);
            msg.writer().writeInt(cmg.playerId);
            if (cmg.type == 2) {
                msg.writer().writeUTF(cmg.playerName + " (" + Util.numberToMoney(cmg.playerPower) + ")");
            } else {
                msg.writer().writeUTF(cmg.playerName);
            }
            msg.writer().writeByte(cmg.role);
            msg.writer().writeInt(cmg.time);
            if (cmg.type == 0) {
                msg.writer().writeUTF(cmg.text);
                msg.writer().writeByte(cmg.color);
            } else if (cmg.type == 1) {
                msg.writer().writeByte(cmg.receiveDonate);
                msg.writer().writeByte(cmg.maxDonate);
                msg.writer().writeByte(cmg.isNewMessage);
            }
            for (Player pl : this.membersInGame) {
                pl.sendMessage(msg);
            }
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void addClanMessage(ClanMessage cmg) {
        this.clanMessages.add(0, cmg);
        if (clanMessages.size() > 20) {
            for (int i = clanMessages.size() - 1; i >= 20; i--) {
                clanMessages.remove(i).dispose();
            }
        }
    }

    public ClanMessage getClanMessage(int clanMessageId) {
        for (ClanMessage cmg : this.clanMessages) {
            if (cmg.id == clanMessageId) {
                return cmg;
            }
        }
        return null;
    }

    public List<ClanMessage> getCurrClanMessages() {
        List<ClanMessage> list = new ArrayList();
        if (this.clanMessages.size() <= 20) {
            list.addAll(this.clanMessages);
        } else {
            for (int i = 0; i < 20; i++) {
                list.add(this.clanMessages.get(i));
            }
        }
        return list;
    }

    public void sendMyClanForAllMember() {
        for (Player pl : this.membersInGame) {
            if (pl != null) {
                ClanService.gI().sendMyClan(pl);
            }
        }
    }

    public void sendFlagBagForAllMember() {
        for (Player pl : this.membersInGame) {
            if (pl != null) {
                Service.gI().sendFlagBag(pl);
            }
        }
    }

    public void addMemberOnline(Player player) {
        this.membersInGame.add(player);
    }

    public void removeMemberOnline(ClanMember cm, Player player) {
        if (player != null) {
            this.membersInGame.remove(player);
        }
        if (cm != null) {
            for (int i = this.membersInGame.size() - 1; i >= 0; i--) {
                if (this.membersInGame.get(i).id == cm.id) {
                    this.membersInGame.remove(i);
                    break;
                }
            }
        }
    }

    public Player getPlayerOnline(int playerId) {
        for (Player player : this.membersInGame) {
            if (player.id == playerId) {
                return player;
            }
        }

        Player playeroffline = PlayerDAO.loadById(playerId);
        if (playeroffline != null) {
            return playeroffline;
        }

        return null;
    }

    public void addClanMember(ClanMember cm) {
        this.members.add(cm);
    }

    public void addClanMember(Player player, byte role) {
        ClanMember cm = new ClanMember(player, this, role);
        this.members.add(cm);
        player.clanMember = cm;
    }

    public void removeClanMember(ClanMember cm) {
        this.members.remove(cm);
        cm.dispose();
    }

    public byte getCurrMembers() {
        return (byte) this.members.size();
    }

    public List<ClanMember> getMembers() {
        return this.members;
    }

    public ClanMember getClanMember(int memberId) {
        for (ClanMember cm : members) {
            if (cm.id == memberId) {
                return cm;
            }
        }
        return null;
    }

    public void reloadClanMember() {
        for (ClanMember cm : this.members) {
            Player pl = Client.gI().getPlayer(cm.id);
            if (pl != null) {
                cm.powerPoint = pl.nPoint.power;
            }
        }
    }

    public void insert() {
        ClanDAO.insert(this);

    }

    public void update() {
        ClanDAO.update(this);

    }

    public void delete(int id) {
        ClanDAO.delete(id);
    }

}
