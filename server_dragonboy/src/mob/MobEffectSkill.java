package mob;

import network.Message;
import player.Player;
import services.Service;
import player.skill.Skill;
import utils.Util;

public class MobEffectSkill {

    private final Mob mob;
    public long lastTimeStun;
    public int timeStun;
    public boolean isStun;
    public boolean isThoiMien;
    public long lastTimeThoiMien;
    public int timeThoiMien;
    public boolean isBlindDCTT;
    public long lastTimeBlindDCTT;
    public int timeBlindDCTT;
    public boolean isAnTroi;
    public long lastTimeAnTroi;
    public int timeAnTroi;
    public boolean isSocola;
    public boolean isBinh;
    private long lastTimeSocola;
    private int timeSocola;
    private long lastTimeBinh;
    private int timeBinh;
    private Player playerUseMafuba;
    private long lastTimeMaPhongBa;
    private int typeBinh;

    public MobEffectSkill(Mob mob) {
        this.mob = mob;
    }

    public void update() {
        if (isStun && (Util.canDoWithTime(lastTimeStun, timeStun) || mob.isDie())) {
            removeStun();
        }
        if (isThoiMien && (Util.canDoWithTime(lastTimeThoiMien, timeThoiMien) || mob.isDie())) {
            removeThoiMien();
        }
        if (isBlindDCTT && (Util.canDoWithTime(lastTimeBlindDCTT, timeBlindDCTT)) || mob.isDie()) {
            removeBlindDCTT();
        }
        if (isSocola && (Util.canDoWithTime(lastTimeSocola, timeSocola) || mob.isDie())) {
            removeSocola();
        }
        if (isAnTroi && (Util.canDoWithTime(lastTimeAnTroi, timeAnTroi) || mob.isDie())) {
            removeAnTroi();
        }
        if (this.isBinh) {
            if (Util.canDoWithTime(lastTimeBinh, timeBinh) || mob.isDie()) {
                removeBinh();
            }
            if (Util.canDoWithTime(lastTimeMaPhongBa, 500) && !mob.isDie()) {
                if (playerUseMafuba != null && playerUseMafuba.playerSkill != null) {
                    double param = playerUseMafuba.playerSkill.getSkillbyId(Skill.MA_PHONG_BA).point;
                    int subHp = (int) ((long) playerUseMafuba.nPoint.hpMax * param * (playerUseMafuba.effectSkill.typeBinh == 0 ? 1 : 2) / 100);
                    if (subHp >= this.mob.point.hp) {
                        subHp = this.mob.point.hp - 1;
                    }
                    this.mob.injured(null, subHp, false);
                }
                this.lastTimeMaPhongBa = System.currentTimeMillis();
            }
        }
    }

    public boolean isHaveEffectSkill() {
        return isAnTroi || isBlindDCTT || isStun || isThoiMien;
    }

    public void startStun(long lastTimeStartBlind, int timeBlind) {
        this.lastTimeStun = lastTimeStartBlind;
        this.timeStun = timeBlind;
        isStun = true;
    }

    private void removeStun() {
        isStun = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0);
            msg.writer().writeByte(1);
            msg.writer().writeByte(40);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void setThoiMien(long lastTimeThoiMien, int timeThoiMien) {
        this.isThoiMien = true;
        this.lastTimeThoiMien = lastTimeThoiMien;
        this.timeThoiMien = timeThoiMien;
    }

    public void removeThoiMien() {
        this.isThoiMien = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0); //b5
            msg.writer().writeByte(1); //b6
            msg.writer().writeByte(41); //num6
            msg.writer().writeByte(mob.id); //b7
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void setStartBlindDCTT(long lastTimeBlindDCTT, int timeBlindDCTT) {
        this.isBlindDCTT = true;
        this.lastTimeBlindDCTT = lastTimeBlindDCTT;
        this.timeBlindDCTT = timeBlindDCTT;
    }

    public void removeBlindDCTT() {
        this.isBlindDCTT = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0);
            msg.writer().writeByte(1);
            msg.writer().writeByte(40);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void setTroi(long lastTimeAnTroi, int timeAnTroi) {
        this.lastTimeAnTroi = lastTimeAnTroi;
        this.timeAnTroi = timeAnTroi;
        this.isAnTroi = true;
    }

    public void removeAnTroi() {
        isAnTroi = false;
        Message msg;
        try {
            msg = new Message(-124);
            msg.writer().writeByte(0); //b4
            msg.writer().writeByte(1);//b5
            msg.writer().writeByte(32);//num8
            msg.writer().writeByte(mob.id);//b6
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void removeSocola() {
        Message msg;
        this.isSocola = false;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(0);
            msg.writer().writeByte(mob.id);
            Service.gI().sendMessAllPlayerInMap(mob.zone, msg);
            msg.cleanup();
        } catch (Exception e) {

        }
    }

    public void setSocola(long lastTimeSocola, int timeSocola) {
        this.lastTimeSocola = lastTimeSocola;
        this.timeSocola = timeSocola;
        this.isSocola = true;
    }

    public void removeBinh() {
        this.isBinh = false;
        Service.gI().Send_Body_Mob(mob, 0, -1);
    }

    public void setBinh(Player plAtt, long lastTimeBinh, int timeBinh) {
        int typeBinh = plAtt.newSkill.typeItem;
        this.lastTimeBinh = lastTimeBinh;
        this.timeBinh = timeBinh;
        this.isBinh = true;
        this.typeBinh = typeBinh;
        this.playerUseMafuba = plAtt;
        Service.gI().Send_Body_Mob(mob, 1, typeBinh == 0 ? 11175 : 11166);
    }

}
