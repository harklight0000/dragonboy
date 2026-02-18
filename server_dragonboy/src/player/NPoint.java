package player;

import consts.ConstItem;
import consts.ConstPlayer;
import consts.ConstRatio;
import dungeon.RewardBlackBall;
import player.intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import lombok.Setter;
import services.map.MapService;
import mob.Mob;
import services.PlayerService;
import radar.Card;
import radar.OptionCard;
import server.GameData;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import services.TaskService;
import player.skill.Skill;
import logger.NLogger;
import utils.SkillUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class NPoint {

    public static final byte MAX_LIMIT = 8;
    public boolean isCrit;
    public boolean isCrit100;
    public boolean isCritTele;
    public int dameAfter;
    /*-----------------------Chỉ số cơ bản------------------------------------*/
    public byte numAttack;
    public short stamina, maxStamina;
    public byte limitPower;
    public long power;
    public long tiemNang;
    public int hp, hpMax, hpg;
    public int mp, mpMax, mpg;
    public int dame, dameg;
    public int def, defg;
    public int crit, critg, critdragon;
    public byte speed = 8;
    public boolean teleport;
    public boolean khangTDHS;
    /**
     * Chỉ số cộng thêm
     */
    public int hpAdd, mpAdd, dameAdd, defAdd, critAdd, hpHoiAdd, mpHoiAdd;
    /**
     * //+#% sức đánh chí mạng
     */
    public List<Integer> tlDameCrit;
    public int tlSDCM;
    /**
     * Tỉ lệ hp, mp cộng thêm
     */
    public List<Integer> tlHp, tlMp;
    /**
     * Tỉ lệ giáp cộng thêm
     */
    public List<Integer> tlDef;
    /**
     * Tỉ lệ sức đánh/ sức đánh khi đánh quái
     */
    public List<Integer> tlDame, tlDameAttMob;
    /**
     * Lượng hp, mp hồi mỗi 30s, mp hồi cho người khác
     */
    public int hpHoi, mpHoi, mpHoiCute;
    /**
     * Tỉ lệ hp, mp hồi cộng thêm
     */
    public short tlHpHoi, tlMpHoi;
    /**
     * Tỉ lệ hp, mp hồi bản thân và đồng đội cộng thêm
     */
    public short tlHpHoiBanThanVaDongDoi, tlMpHoiBanThanVaDongDoi;
    /**
     * Tỉ lệ hút hp, mp khi đánh, hp khi đánh quái
     */
    public short tlHutHp, tlHutMp, tlHutHpMob;
    /**
     * Tỉ lệ hút hp, mp xung quanh mỗi 5s
     */
    public short tlHutHpMpXQ;
    /**
     * Tỉ lệ phản sát thương
     */
    public short tlPST;
    /**
     * Tỉ lệ tiềm năng sức mạnh
     */
    public List<Integer> tlTNSM;
    /**
     * Tỉ lệ vàng cộng thêm
     */
    public short tlGold;
    /**
     * Tỉ lệ né đòn
     */
    public short tlNeDon;
    public short tlBom;
    public short tlGiap;
    public short tlxgcc;
    public short tlxgc;
    public short tlchinhxac;
    public short tlTNSMPet;
    public short xChuong;
    public short setltdb;
    public short setTinhAn;
    public short setNhatAn;
    public short setNguyetAn;
    /**
     * Tỉ lệ sức đánh đẹp cộng thêm cho bản thân và người xung quanh
     */
    public int tlSexyDame;
    /**
     * Tỉ lệ giảm sức đánh
     */
    public short tlSubSD;
    public int voHieuChuong;
    /*------------------------Effect skin-------------------------------------*/
    public Item trainArmor;
    public boolean wearingTrainArmor;
    public boolean wearingVoHinh;
    public boolean isKhongLanh;
    public boolean islinhthuydanhbac;
    public boolean isTinhAn;
    public boolean isNhatAn;
    public boolean isNguyetAn;
    public boolean isTanHinh;
    public boolean isHoaDa;
    public boolean isLamCham;
    public boolean isDoSPL;
    public boolean isThoBulma;
    public short tlHpGiamODo;
    public boolean isGogeta_SSJ_4;
    public boolean isGogeta_SSJ_Blue;
    public int tlSpeed;
    public int levelBT;


    // % hiện tại (0..punchDmgStackMax)


    public boolean lowMpDefBoost;
    public int lowMpDefPercent;

    public boolean isPunchStackEnabled = false; // bật/tắt
    public int  punchDmgStack= 0;// % giảm dame

    public boolean isFusionOnly;

    private boolean isPunchSkill(Skill skill) {
        if (skill == null || skill.template == null) return false;
        switch (skill.template.id) {
            case Skill.GALICK:
            case Skill.DRAGON:
            case Skill.DEMON:
            case Skill.KAIOKEN:
            case Skill.LIEN_HOAN:
                return true;
            default:
                return false;
        }
    }
    @Setter
    private Player player;
    private Intrinsic intrinsic;
    private int percentDameIntrinsic;
    private long lastTimeHoiPhuc;

    /*-------------------------------------------------------------------------*/
    private long lastTimeHoiStamina;

    public NPoint(Player player) {
        this.player = player;
        this.tlHp = new ArrayList<>();
        this.tlMp = new ArrayList<>();
        this.tlDef = new ArrayList<>();
        this.tlDame = new ArrayList<>();
        this.tlDameAttMob = new ArrayList<>();
        this.tlTNSM = new ArrayList<>();
        this.tlDameCrit = new ArrayList<>();
    }

    /**
     * Tính toán mọi chỉ số sau khi có thay đổi
     */
    public void calPoint() {
        if (this.player.pet != null) {
            this.player.pet.nPoint.setPointWhenWearClothes();
        }
        this.setPointWhenWearClothes();
    }

    // NPoint.java
// thêm field (và nhớ reset về false trong resetPoint())
    public boolean fusionSkinOnly;

// ... trong resetPoint():
// this.fusionSkinOnly = false;

    private boolean hasOption(Item item, int optId) {
        if (item == null || item.itemOptions == null) return false;
        for (Item.ItemOption io : item.itemOptions) {
            if (io != null && io.optionTemplate != null && io.optionTemplate.id == optId) {
                return true;
            }
        }
        return false;
    }

    /** Master hoặc bản thân đang fusion? (Pet nhìn theo master) */
    private boolean isFusedContext() {
        if (this.player == null) return false;
        // master (player thường)
        if (this.player.fusion != null && this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) return true;
        // pet: nhìn theo master
        if (this.player.isPet) {
            Pet p = (Pet) this.player;
            return p.master != null
                    && p.master.fusion != null
                    && p.master.fusion.typeFusion != ConstPlayer.NON_FUSION;
        }
        return false;
    }


    private void setPointWhenWearClothes() {
        resetPoint();

        // --- rewardBlackBall như cũ ---
        if (this.player.rewardBlackBall.timeOutOfDateReward[2] > System.currentTimeMillis()) tlHutHp += RewardBlackBall.R3S_1;
        if (this.player.rewardBlackBall.timeOutOfDateReward[3] > System.currentTimeMillis()) tlPST   += RewardBlackBall.R4S_2;
        if (this.player.rewardBlackBall.timeOutOfDateReward[4] > System.currentTimeMillis()) {
            tlDameCrit.add(RewardBlackBall.R5S_1);
            tlSDCM += RewardBlackBall.R5S_1;
        }
        if (this.player.rewardBlackBall.timeOutOfDateReward[6] > System.currentTimeMillis()) tlNeDon += RewardBlackBall.R7S_1;

        // --- Options từ thẻ radar ---
        Card card = player.Cards.stream().filter(r -> r != null && r.Used == 1).findFirst().orElse(null);
        if (card != null) {
            for (OptionCard io : card.Options) {
                if (io.active == card.Level || (card.Level == -1 && io.active == 0)) {
                    addOption(io);
                }
            }
        }




        if (this.player.fusion != null && this.player.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2) {
            this.player.inventory.itemsBag.stream()
                    .filter(it -> it.isNotNullItem() && it.template.id == 921)
                    .findFirst()
                    .ifPresent(btc2 -> {
                        for (ItemOption io : btc2.itemOptions) {
                            addOption(io);
                            if (io.optionTemplate.id == 72) this.levelBT = io.param;
                        }
                    });
        }


        this.player.setClothes.worldcup = 0;

        boolean fused = isFusedContext();

        for (int i = 0; i < this.player.inventory.itemsBody.size(); i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (!item.isNotNullItem()) continue;

            // phụ: worldcup & teleport như cũ
            switch (item.template.id) {
                case 966, 982, 983, 883, 904 -> this.player.setClothes.worldcup++;
            }
            if (item.template.id >= 592 && item.template.id <= 594) {
                teleport = true;
            }

            //if skin dang deo isFusiôn
            if (i == 5 && hasOption(item, 38) && !fused) {
                continue;
            }

            // addOption như bình thường
            for (Item.ItemOption io : item.itemOptions) {
                addOption(io);
            }
        }

        // --- Tính lại các chỉ số phụ thuộc ---
        setDameTrainArmor();
        setBasePoint();
        setOutfitFusion();
        setSpeed();
    }

  

    private void addOption(int id, int param) {
        switch (id) {
            case 0: //Tấn công +#
                this.dameAdd += param;
                break;
            case 2: //HP, KI+#000
                this.hpAdd += param * 1000;
                this.mpAdd += param * 1000;
                break;
            case 3:// vô hiệu chưởng
                this.voHieuChuong += param;
                break;
            case 5: //+#% sức đánh chí mạng
                this.tlDameCrit.add(param);
                this.tlSDCM += param;
                break;
            case 6: //HP+#
                this.hpAdd += param;
                break;
            case 7: //KI+#
                this.mpAdd += param;
                break;
            case 8: //Hút #% HP, KI xung quanh mỗi 5 giây
                this.tlHutHpMpXQ += param;
                break;
            case 14: //Chí mạng+#%
                this.critAdd += param;
                break;
            case 16: // Speed
            case 114:
            case 148: //+#% tốc độ chạy
                this.tlSpeed += param;
                break;
            case 18: //Chinh xac
                this.tlchinhxac += param;
                break;
            case 19: //Tấn công+#% khi đánh quái
                this.tlDameAttMob.add(param);
                break;
            case 22: //HP+#K
                this.hpAdd += param * 1000;
                break;
            case 23: //MP+#K
                this.mpAdd += param * 1000;
                break;
            case 24: //Làm chậm
                this.isLamCham = true;
                break;
            case 25: //Tàn hình
                this.isTanHinh = true;
                break;
            case 26: //Hóa đá
                this.isHoaDa = true;
                break;
            case 27: //+# HP/30s
                this.hpHoiAdd += param;
                break;
            case 28: //+# KI/30s
                this.mpHoiAdd += param;
                break;
            case 33: //dịch chuyển tức thời
                this.teleport = true;
                break;
            case 34:
                this.setTinhAn += 1;
                break;
            case 35:
                this.setNguyetAn += 1;
                break;
            case 36:
                this.setNhatAn += 1;
                break;
            case 38: //chi co tac dung khi hop the
                this.isFusionOnly = true;
                break;
            case 47: //Giáp+#
                this.defAdd += param;
                break;
            case 48: //HP/KI+#
                this.hpAdd += param;
                this.mpAdd += param;
                break;
            case 49: //Tấn công+#%
            case 50: //Sức đánh+#%
                this.tlDame.add(param);
                break;
            case 77: //HP+#%
                this.tlHp.add(param);
                break;
            case 80: //HP+#%/30s
                this.tlHpHoi += param;
                break;
            case 81: //MP+#%/30s
                this.tlMpHoi += param;
                break;
            case 88: //Cộng #% exp khi đánh quái
                this.tlTNSM.add(param);
                break;
            case 94: //Giáp #%
                this.tlGiap += param;
                break;
            case 95: //Biến #% tấn công thành HP
                this.tlHutHp += param;
                break;
            case 96: //Biến #% tấn công thành MP
                this.tlHutMp += param;
                break;
            case 97: //Phản #% sát thương
                this.tlPST += param;
                break;
            case 98: //Xuyen giap chuong
                this.tlxgc += param;
                break;
            case 99: //Xuyen giap can chien
                this.tlxgcc += param;
                break;
            case 100: //+#% vàng từ quái
                this.tlGold += param;
                break;
            case 101: //+#% TN,SM
                this.tlTNSM.add(param);
                break;
            case 103: //KI +#%
                this.tlMp.add(param);
                break;
            case 104: //Biến #% tấn công quái thành HP
                this.tlHutHpMob += param;
                break;
            case 105: //Vô hình khi không đánh quái và boss
                this.wearingVoHinh = true;
                break;
            case 106: //Không ảnh hưởng bởi cái lạnh
                this.isKhongLanh = true;
                break;
            case 108: //#% Né đòn
                this.tlNeDon += param;
                break;
            case 109: //Hôi, giảm #% HP
                this.tlHpGiamODo += param;
                break;
            case 110: //Do spl
                this.isDoSPL = true;
                break;
            case 116: //Kháng thái dương hạ san
                this.khangTDHS = true;
                break;
            case 117: //Đẹp +#% SĐ cho mình và người xung quanh
            case 226: //Đẹp +#% SĐ cho mình và người xung quanh
                if (param > this.tlSexyDame) {
                    this.tlSexyDame = param;
                }
                break;
            case 147: //+#% sức đánh
                this.tlDame.add(param);
                break;
            case 153: //% phát nổ sau khi chết
                this.tlBom += param;
                break;
            case 155: //Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái
                this.tlSubSD += 50;
                this.tlTNSM.add(param);
                this.tlGold += param;
                break;

                //tăng dame khi chỉ đấm, max 50pt
            case 156:
                this.isPunchStackEnabled = true;
                break;

            case 157:
                this.lowMpDefBoost = true;
                this.lowMpDefPercent = param;
                break;
            case 159: // x chưởng
                this.xChuong = (short) param;
                break;
            case 160: // TNSM PET;
                this.tlTNSMPet += param;
                break;
            case 162: //Cute hồi #% KI/s bản thân và xung quanh
                this.mpHoiCute += param;
                break;
            case 173: //Phục hồi #% HP và KI cho đồng đội
                this.tlHpHoiBanThanVaDongDoi += param;
                this.tlMpHoiBanThanVaDongDoi += param;
                break;
            case 211:
                this.setltdb += 1;
                break;
        }
    }

    // Overload cho ItemOption
    private void addOption(ItemOption io) {
        addOption(io.optionTemplate.id, io.param);
    }

    // Overload cho OptionCard
    private void addOption(OptionCard io) {
        addOption(io.id, io.param);
    }


    private void setSpeed() {
        if (player.isPl()) {
            speed = (byte) (8 + 8 * (tlSpeed / 100));
        }
    }

    
    //1693 1553 ssj 4
    //1590 1746 ssj blue
    private void setOutfitFusion() {
        if (this.player.inventory.itemsBody.size() < 6 || this.player.pet == null || this.player.pet.inventory.itemsBody.size() < 6) {
            return;
        }
        Item skin = this.player.inventory.itemsBody.get(5);
        Item pskin = this.player.pet.inventory.itemsBody.get(5);
        if (skin.isNotNullItem() && pskin.isNotNullItem()) {
            this.isGogeta_SSJ_4 = skin.template.id == ConstItem.CT_CADIC_SSJ_4  && pskin.template.id == ConstItem.CT_GOKU_SSJ_4 
                    || skin.template.id == ConstItem.CT_GOKU_SSJ_4 && pskin.template.id == ConstItem.CT_CADIC_SSJ_4;
            this.isGogeta_SSJ_Blue = skin.template.id == ConstItem.CT_CADIC_SSJ_BLUE  && pskin.template.id == ConstItem.CT_GOKU_SSJ_BLUE 
                    || skin.template.id == ConstItem.CT_GOKU_SSJ_BLUE && pskin.template.id == ConstItem.CT_CADIC_SSJ_BLUE;
        } else {
            this.isGogeta_SSJ_Blue = false;
            this.isGogeta_SSJ_4 = false;
        }
    }

    private void setDameTrainArmor() {
        if (!this.player.isPet && !this.player.isBoss) {
            if (this.player.inventory.itemsBody.size() < 7) {
                return;
            }
            try {
                Item gtl = this.player.inventory.itemsBody.get(6);
                if (gtl.isNotNullItem()) {
                    this.wearingTrainArmor = true;
                    this.player.inventory.trainArmor = gtl;
                    this.tlSubSD += ItemService.gI().getPercentTrainArmor(gtl);
                } else {
                    if (this.player.inventory.trainArmor == null) {
                        gtl = this.player.inventory.itemsBag.stream().filter(item -> item.isNotNullItem() && item.template.type == 32 && item.itemOptions != null
                                && item.itemOptions.stream().filter(io -> io.optionTemplate.id == 9 && io.param > 0).findFirst().orElse(null) != null).findFirst().orElse(null);
                        if (gtl == null) {
                            return;
                        }
                        this.player.inventory.trainArmor = gtl;
                    }
                    this.wearingTrainArmor = false;
                    for (Item.ItemOption io : this.player.inventory.trainArmor.itemOptions) {
                        if (io.optionTemplate.id == 9 && io.param > 0) {
                            this.tlDame.add(ItemService.gI().getPercentTrainArmor(this.player.inventory.trainArmor));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                NLogger.logWarning("Lỗi get giáp tập luyện " + this.player.name );
            }
        }
    }

    public void setBasePoint() {
        setHpMax();
        setHp();
        setMpMax();
        setMp();
        setDame();
        setDef();
        setCrit();
        setHpHoi();
        setMpHoi();
        setLtdb();
        setThoBulma();
        setTinhNhatNguyetAn();
    }



    private void setLtdb() {
        this.islinhthuydanhbac = this.setltdb >= 5;
    }

    private void setThoBulma() {
        this.isThoBulma = (this.player.inventory != null && this.player.inventory.itemsBody != null
                && this.player.inventory.itemsBody.size() >= 5 && this.player.inventory.itemsBody.get(5).isNotNullItem()
                && this.player.inventory.itemsBody.get(5).template.id == 584);
    }

    private void setTinhNhatNguyetAn() {
        this.isTinhAn = this.setTinhAn >= 5;
        this.isNhatAn = this.setNhatAn >= 5;
        this.isNguyetAn = this.setNguyetAn >= 5;
    }

    private void setHpHoi() {
        this.hpHoi = this.hpMax / 100;
        this.hpHoi += this.hpHoiAdd;

        if (this.tlHpHoi > 100) {
            this.tlHpHoi = 100;
        } else if (this.tlHpHoi < 0) {
            this.tlHpHoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoi / 100);

        if (this.tlHpHoiBanThanVaDongDoi > 100) {
            this.tlHpHoiBanThanVaDongDoi = 100;
        } else if (this.tlHpHoiBanThanVaDongDoi < 0) {
            this.tlHpHoiBanThanVaDongDoi = 0;
        }

        this.hpHoi += ((long) this.hpMax * this.tlHpHoiBanThanVaDongDoi / 100);
    }

    private void setMpHoi() {
        this.mpHoi = this.mpMax / 100;
        this.mpHoi += this.mpHoiAdd;

        if (this.tlMpHoi > 100) {
            this.tlMpHoi = 100;
        } else if (this.tlMpHoi < 0) {
            this.tlMpHoi = 0;
        }

        this.mpHoi += ((long) this.mpMax * this.tlMpHoi / 100);

        if (this.tlMpHoiBanThanVaDongDoi > 100) {
            this.tlMpHoiBanThanVaDongDoi = 100;
        } else if (this.tlMpHoiBanThanVaDongDoi < 0) {
            this.tlMpHoiBanThanVaDongDoi = 0;
        }

        this.mpHoi += (((long) this.mpMax * this.tlMpHoiBanThanVaDongDoi) / 100);
    }

    private void setHpMax() {
        long hpMax = this.hpg + this.hpAdd;

        for (Integer tl : this.tlHp) {
            hpMax += (hpMax * tl / 100L);
        }

        if (this.player.setClothes.nappa == 5) {
            hpMax += (hpMax * 80L / 100L);
        }

        if (this.player.setClothes.cadicM >= 2) {
            hpMax += (hpMax * 20L / 100L);

        }
        if (this.player.setClothes.worldcup == 2) {
            hpMax += (hpMax * 10 / 100L);
        }

        if (player.itemTime != null && player.itemTime.isUseRX) {
            hpMax += (hpMax * 10L / 100L);
        }

        if (this.isNhatAn) {
            hpMax += (hpMax * 15L / 100L);
        }

        if (this.player.rewardBlackBall.timeOutOfDateReward[1] > System.currentTimeMillis()) {
            hpMax += (hpMax * RewardBlackBall.R2S_1 / 100L);
        }

        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentHpMonkey(player.effectSkill.levelMonkey);
                hpMax += (hpMax * percent / 100L);
            }
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 1 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            hpMax += (hpMax * 5 / 100L);
        }

        if (this.player.isPet && ((Pet) this.player).typePet == 2 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            hpMax += (hpMax * 30 / 100L);
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 3 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            hpMax += (hpMax * 40 / 100L);
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 4 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            hpMax += (hpMax * 60 / 100L);
        }

        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            hpMax *= this.player.effectSkin.xHPKI;
        }

        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8062) {
            hpMax += (hpMax * 5 / 100L);
        }

        if (this.isGogeta_SSJ_Blue) {
            hpMax += (hpMax * 10 / 100L);
        }

        if (this.player.isPhuHoMapMabu) {
            hpMax += 1_000_000;
        }

        if (this.player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            hpMax += this.player.pet.nPoint.hpMax;
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet && !this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2;
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBoHuyet2) {
            hpMax *= 2.2;
        }

        if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
            if (this.player.effectSkill.tiLeHPHuytSao != 0) {
                hpMax += (hpMax * this.player.effectSkill.tiLeHPHuytSao / 100L);
            }
        }

        if (this.player.effectSkill != null && this.player.effectSkill.isChibi && this.player.typeChibi == 3) {
            hpMax *= 2;
        }

        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            hpMax /= 2;
        }


        if (hpMax > 2_000_000_000) {
            hpMax = 2_000_000_000;
        }

        this.hpMax = (int) hpMax;
    }

    private void setHp() {
        this.hp = Math.min(this.hp, this.hpMax);
    }

    private void setMpMax() {
        long mpMax = this.mpg + this.mpAdd;

        for (Integer tl : this.tlMp) {
            mpMax += (mpMax * tl / 100L);
        }

        if (this.isNguyetAn) {
            mpMax += (mpMax * 15L / 100L);
        }

        if (this.player.rewardBlackBall.timeOutOfDateReward[5] > System.currentTimeMillis()) {
            mpMax += (mpMax * RewardBlackBall.R6S_1 / 100L);
        }

        if (this.player.setClothes.worldcup == 2) {
            mpMax += (this.mpMax * 10 / 100L);
            if (this.player.isPet && ((Pet) this.player).typePet == 1
                    && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
                mpMax += (this.mpMax * 5 / 100L);
            }
        }

        if (this.player.isPet && ((Pet) this.player).typePet == 2
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            mpMax += (this.mpMax * 30 / 100L);// MP berus
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 3
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            mpMax += (this.mpMax * 40 / 100L);// MP black
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 4
                && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            mpMax += (this.mpMax * 60 / 100L);// MP black goku Rose
        }

        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            mpMax *= this.player.effectSkin.xHPKI;
        }

        if (this.isGogeta_SSJ_Blue) {
            mpMax += (mpMax * 10 / 100L);
        }

        if (this.player.isPhuHoMapMabu) {
            mpMax += 1_000_000;
        }

        if (player.itemTime != null && player.itemTime.isUseRX) {
            mpMax += (mpMax * 10L / 100L);
        }

        if (this.player.fusion.typeFusion != 0) {
            mpMax += this.player.pet.nPoint.mpMax;
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi && !this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2;
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseBoKhi2) {
            mpMax *= 2.2;
        }


        if (mpMax > 2_000_000_000) {
            mpMax = 2_000_000_000;
        }

        this.mpMax = (int) mpMax;
    }

    private void setMp() {
        this.mp = Math.min(this.mp, this.mpMax);
    }

    public int getHP() {
        return Math.min(this.hp, this.hpMax);
    }

    public void setHP(long hp) {
        if (hp > 0) {
            this.hp = (int) (hp <= this.hpMax ? hp : this.hpMax);
        } else {
            player.setDie();
        }
    }

    public int getMP() {
        return Math.min(this.mp, this.mpMax);
    }

    public void setMP(long mp) {
        if (mp > 0) {
            this.mp = (int) (mp <= this.mpMax ? mp : this.mpMax);
        } else {
            this.mp = 0;
        }
    }

    private void setDame() {
        long dame = this.dameg + this.dameAdd;

        for (Integer tl : this.tlDame) {
            dame += (dame * tl / 100L);
        }

        if (this.player.isPet && ((Pet) this.player).typePet == 3 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            dame += (dame * 20 / 100L);
        }

        if (this.player.isPet && ((Pet) this.player).typePet == 1 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            dame += (dame * 5 / 100L);
        }

        if (this.player.isPet && ((Pet) this.player).typePet == 2 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            dame += (dame * 30 / 100L);
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 3 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            dame += (dame * 40 / 100L);
        }
        if (this.player.isPet && ((Pet) this.player).typePet == 4 && (((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA || ((Pet) this.player).master.fusion.typeFusion == ConstPlayer.HOP_THE_PORATA_C2)) {
            dame += (dame * 60 / 100L);
        }

        if (this.isTinhAn) {
            dame += (dame * 15L / 100L);
        }

        if (!this.player.isPet && this.player.itemTime != null && this.player.itemTime.isEatMeal || this.player.isPet && this.player.itemTime != null && ((Pet) this.player).master.itemTime.isEatMeal) {
            dame += (dame * 10 / 100L);
        }

        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8060) {
            dame += (dame * 5 / 100L);
        }

        if (this.player.setClothes.nail >= 2) {
            this.tlDameCrit.add(10);
        }
        if (this.player.itemTime != null && this.player.itemTime.isEatMeal2 && this.player.itemTime.iconMeal2 == 8061) {
            this.tlDameCrit.add(5);
            this.tlSDCM += 5;
        }

        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo && !this.player.itemTime.isUseCuongNo2) {
            dame *= 2;
        }
        if (this.player.itemTime != null && this.player.itemTime.isUseCuongNo2) {
            dame *= 2.2;
        }

        if (this.player.rewardBlackBall.timeOutOfDateReward[0] > System.currentTimeMillis()) {
            dame += (dame * RewardBlackBall.R1S_2 / 100L);
        }

        if (this.player.setClothes.worldcup == 2) {
            dame += (dame * 10 / 100L);
        }

        if (this.isGogeta_SSJ_Blue) {
            dame += (dame * 10 / 100L);
        }

        if (this.player.isPhuHoMapMabu) {
            dame += 10_000;
        }

        if (player.itemTime != null && player.itemTime.isUseRX) {
            dame += (dame * 10L / 100L);
        }

        if (this.player.zone != null && MapService.gI().isMapBlackBallWar(this.player.zone.map.mapId)) {
            dame *= this.player.effectSkin.xDame;
        }

        if (this.player.fusion.typeFusion != 0) {
            dame += this.player.pet.nPoint.dame;
        }

        if (this.player.effectSkill.isMonkey) {
            if (!this.player.isPet || (this.player.isPet && ((Pet) this.player).status != Pet.FUSION)) {
                int percent = SkillUtil.getPercentDameMonkey(player.effectSkill.levelMonkey);
                dame += (dame * percent / 100L);
            }
        }

        dame += (dame * tlSexyDame / 100L);

        dame -= (dame * tlSubSD / 100L);

        if (this.player.zone != null && MapService.gI().isMapCold(this.player.zone.map) && !this.isKhongLanh) {
            dame /= 2;
        }

        if (dame > 2_000_000_000) {
            dame = 2_000_000_000;
        }

        this.dame = (int) dame;
    }

    private void setDef() {
        this.def = this.defg * 4;
        this.def += this.defAdd;
    }

    private void setCrit() {
        this.crit = this.critg;
        this.crit += this.critAdd;
        this.crit += this.critdragon;
        if (this.player.effectSkill.isMonkey) {
            this.crit = 110;
        }
        if (player.setClothes.thanVuTruKaio >= 1) {
            this.crit += 10;
        }
    }

    private void resetPoint() {
        this.voHieuChuong = 0;
        this.hpAdd = 0;
        this.mpAdd = 0;
        this.dameAdd = 0;
        this.defAdd = 0;
        this.critAdd = 0;
        this.tlHp.clear();
        this.tlMp.clear();
        this.tlDef.clear();
        this.tlDame.clear();
        this.tlDameCrit.clear();
        this.tlDameAttMob.clear();
        this.tlSDCM = 0;
        this.tlHpHoiBanThanVaDongDoi = 0;
        this.tlMpHoiBanThanVaDongDoi = 0;
        this.hpHoi = 0;
        this.mpHoi = 0;
        this.mpHoiCute = 0;
        this.tlHpHoi = 0;
        this.tlMpHoi = 0;
        this.tlHutHp = 0;
        this.tlHutMp = 0;
        this.tlHutHpMob = 0;
        this.tlHutHpMpXQ = 0;
        this.tlPST = 0;
        this.tlTNSM.clear();
        this.tlDameAttMob.clear();
        this.tlGold = 0;
        this.tlNeDon = 0;
        this.tlBom = 0;
        this.tlGiap = 0;
        this.tlxgcc = 0;
        this.tlxgc = 0;
        this.tlchinhxac = 0;
        this.tlTNSMPet = 0;
        this.xChuong = 0;
        this.setltdb = 0;
        this.setTinhAn = 0;
        this.setNhatAn = 0;
        this.setNguyetAn = 0;
        this.tlSexyDame = 0;
        this.tlSubSD = 0;
        this.tlHpGiamODo = 0;
        this.tlSpeed = 0;
        this.teleport = false;

        this.wearingVoHinh = false;
        this.isKhongLanh = false;
        this.khangTDHS = false;
        this.isTanHinh = false;
        this.isHoaDa = false;
        this.isLamCham = false;
        this.isDoSPL = false;
        this.isThoBulma = false;

        this.lowMpDefBoost = false;
        this.lowMpDefPercent = 0;

        this.isPunchStackEnabled = false;
        this.isFusionOnly = false;
    }

    public void addHp(long hp) {
        if (hp > 0) {
            long potentialHp = (long) this.hp + hp;
            if (potentialHp > this.hpMax) {
                this.hp = this.hpMax;
            } else {
                this.hp = (int) Math.min(potentialHp, 2000000000);
            }
        }
    }

    public void addMp(long mp) {
        long potentialMp = this.mp + mp;

        if (potentialMp > this.mpMax) {
            this.mp = this.mpMax;
        } else if (potentialMp < 0) {
            this.mp = 0;
        } else {
            this.mp = (int) potentialMp;
        }
    }

    public void setHp(long hp) {
        if (hp < 0) {
            this.hp = 0;
        } else {
            this.hp = (int) Math.min(hp, 2_000_000_000);
        }
    }

    public void setMp(long mp) {
        if (mp < 0) {
            this.mp = 0;
        } else {
            this.mp = (int) Math.min(mp, 2_000_000_000);
        }
    }

    private void setIsCrit() {
        if (intrinsic != null && intrinsic.id == 25
                && this.getCurrPercentHP() <= intrinsic.param1) {
            isCrit = true;
        } else if (isCrit100) {
            isCrit100 = false;
            isCrit = true;
        } else {
            isCrit = Util.isTrue(this.crit, ConstRatio.PER100);
        }
    }
    public int getDameAttack(boolean isAttackMob) {
        setIsCrit();

        long dameAttack = this.dame;
        Intrinsic intrinsic = (this.player != null && this.player.playerIntrinsic != null)
                ? this.player.playerIntrinsic.intrinsic
                : null;

        // các hệ số phần trăm
        this.percentDameIntrinsic = 0;
        int  percentDameSkill = 0;
        int  percentXDame      = 0; // dùng int để tránh tràn khi cộng lớn (100, 80, ...)

        Skill skillSelect = (player != null && player.playerSkill != null) ? player.playerSkill.skillSelect : null;

        // Nếu không có skill thì trả về dame cơ bản
        if (skillSelect == null || skillSelect.template == null) {
            return (int) Math.min(dameAttack, 2_000_000_000L);
        }

        // [PUNCH STACK] reset về 0 nếu chiêu hiện tại không phải "đấm"
        boolean isPunch = this.isPunchStackEnabled && isPunchSkill(skillSelect);
        if (!isPunch) {
            this.punchDmgStack = 0;
        }

        // xử lý "crit dịch chuyển" còn treo từ trước
        if (skillSelect.template.id != Skill.DICH_CHUYEN_TUC_THOI && isCritTele) {
            isCrit = true;
            isCritTele = false;
        }

        // --- skill cụ thể ---
        switch (skillSelect.template.id) {
            case Skill.DRAGON:
                if (intrinsic != null && intrinsic.id == 1) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;

            case Skill.KAMEJOKO:
                if (intrinsic != null && intrinsic.id == 2) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.songoku == 5) {
                    percentXDame += 100;
                }
                break;

            case Skill.GALICK:
                if (intrinsic != null && intrinsic.id == 16) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.kakarot == 5) {
                    percentXDame += 100;
                }
                break;

            case Skill.ANTOMIC:
                if (intrinsic != null && intrinsic.id == 17) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;

            case Skill.TU_SAT:
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.cadicM == 4) {
                    percentXDame += 20;
                } else if (this.player.setClothes.cadicM == 5) {
                    percentXDame += 50;
                }
                break;

            case Skill.DEMON:
                if (intrinsic != null && intrinsic.id == 8) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                break;

            case Skill.MASENKO:
                if (intrinsic != null && intrinsic.id == 9) {
                    percentXDame += intrinsic.param1; // nội tại cộng vào % xDame
                }
                if (this.player.setClothes.nail == 5) {
                    percentXDame += 80;
                }
                // thiết kế cũ: cộng % skill bằng (damage + %xDame-đang-tính)
                percentDameSkill = skillSelect.damage + percentXDame;
                break;

            case Skill.LIEN_HOAN:
                if (intrinsic != null && intrinsic.id == 13) {
                    percentDameIntrinsic = intrinsic.param1;
                }
                percentDameSkill = skillSelect.damage;
                if (this.player.setClothes.ocTieu == 5) {
                    percentXDame += 100;
                }
                break;

            case Skill.KAIOKEN:
                percentDameSkill = skillSelect.damage;
                if (player.setClothes.thanVuTruKaio == 5) {
                    percentXDame += 30;
                }
                break;

            case Skill.DICH_CHUYEN_TUC_THOI:
                isCrit = true;
                isCritTele = true;
                dameAttack = Util.nextInt(
                        (int) Math.min(2_000_000_000L, dameAttack - (dameAttack / 100 * 5)),
                        (int) Math.min(2_000_000_000L, dameAttack + (dameAttack / 100 * 5))
                );
                break;

            case Skill.MAKANKOSAPPO: {
                percentDameSkill = skillSelect.damage;
                int dameSkill = (int) Math.min(2_000_000_000L, (long) this.mpMax * percentDameSkill / 100);
                if (this.player.setClothes.picolo == 5) {
                    dameSkill = (int) Math.min((long) dameSkill * 3 / 2, 2_000_000_000L);
                }
                return dameSkill;
            }

            case Skill.QUA_CAU_KENH_KHI: {
                long hpmob = 0, hppl = 0;
                for (Mob mob : this.player.zone.mobs) {
                    if (!mob.isDie()
                            && Util.getDistance(this.player, mob) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hpmob += mob.point.hp;
                    }
                }
                for (Player pl : this.player.zone.getHumanoids()) {
                    if (!pl.isDie() && this.player.id != pl.id
                            && Util.getDistance(this.player, pl) <= SkillUtil.getRangeQCKK(this.player.playerSkill.skillSelect.point)) {
                        hppl += pl.nPoint.hp;
                    }
                }
                long dameqckk = (hpmob * 10 / 100) + (hppl * 10 / 100) + (long) this.dame * 10;
                if (this.player.setClothes.kirin == 5) {
                    dameqckk *= 2;
                }
                dameqckk = dameqckk + (Util.nextInt(-5, 5) * dameqckk / 100);
                if (dameqckk > 2_000_000_000L) dameqckk = 2_000_000_000L;
                return (int) dameqckk;
            }

            case Skill.DE_TRUNG:
                if (player.setClothes.pikkoroDaimao == 5) {
                    dameAttack = Math.min(dameAttack * 4, 2_000_000_000L);
                }
                return (int) dameAttack;
        }

        // nội tại 18 khi đang khỉ
        if (intrinsic != null && intrinsic.id == 18 && this.player.effectSkill.isMonkey) {
            percentDameIntrinsic = intrinsic.param1;
        }

        // áp % damage của skill
        if (percentDameSkill != 0) {
            dameAttack = (dameAttack * percentDameSkill) / 100;
        }

        // áp % từ nội tại, các hiệu ứng sau skill
        dameAttack += (dameAttack * percentDameIntrinsic) / 100L;
        dameAttack += (dameAttack * dameAfter) / 100L;

        if (this.player.effectSkill != null && this.player.effectSkill.isDameBuff && tlSexyDame == 0) {
            int tiLeDame = this.player.effectSkill.tileDameBuff;
            dameAttack += (dameAttack * tiLeDame) / 100L;
        }

        // % khi đánh quái + bùa đệ tử
        if (isAttackMob) {
            for (Integer tl : this.tlDameAttMob) {
                dameAttack += (dameAttack * tl) / 100L;
            }
            if (this.player.isPet && ((Pet) this.player).master.charms.tdDeTu > System.currentTimeMillis()) {
                dameAttack = Math.min(dameAttack * 2, 2_000_000_000L);
            }
        }

        // reset hậu tố cho lần sau
        dameAfter = 0;

        // chí mạng
        if (isCrit) {
            dameAttack *= 2;
            dameAttack += (dameAttack * tlSDCM) / 100L;
        }

        // [PUNCH STACK] gộp % stack vào % tổng (additive, tránh nhân chồng)
        if (isPunch && this.punchDmgStack > 0) {
            percentXDame += this.punchDmgStack;
        }

        // áp % xDame tổng
        if (percentXDame != 0) {
            dameAttack += (dameAttack * percentXDame) / 100L;
        }

        // ngẫu nhiên ±5%
        long tempDameAttack = (dameAttack / 100L) * 5L;
        if (tempDameAttack <= 0) tempDameAttack = 1;
        dameAttack += (long) (Util.getOne(-1, 1) * Util.nextInt((int) tempDameAttack) + 1);

        // hiệu ứng xChuong (chỉ cho KAME/ANTOMIC/MASENKO)
        if (player.effectSkin != null && player.effectSkin.isXChuong
                && (skillSelect.template.id == Skill.KAMEJOKO
                || skillSelect.template.id == Skill.ANTOMIC
                || skillSelect.template.id == Skill.MASENKO)) {
            dameAttack = Math.min(dameAttack * xChuong, 2_000_000_000L);
            player.effectSkin.isXDame     = true;
            player.effectSkin.isXChuong   = false;
            player.effectSkin.lastTimeXChuong = System.currentTimeMillis();
        }

        // clamp
        if (dameAttack > 2_000_000_000L) dameAttack = 2_000_000_000L;

        // [PUNCH STACK] luôn +1% sau khi đấm xong (kể cả lần đầu), tối đa 50%
        if (isPunch) {
            this.punchDmgStack = Math.min(this.punchDmgStack + 1, 50);
        }

        return (int) dameAttack;
    }

    public int getCurrPercentHP() {
        if (this.hpMax == 0) {
            return 100;
        }
        return (int) ((long) this.hp * 100 / this.hpMax);
    }

    public int getCurrPercentMP() {
        return (int) ((long) this.mp * 100 / this.mpMax);
    }

    public void setFullHpMp() {
        this.hp = this.hpMax;
        this.mp = this.mpMax;
    }

    public void subHP(long sub) {
        this.hp -= sub;
        if (this.hp <= 0) {
            this.hp = 0;
            this.setHp(0);
        }
    }

    public void subMP(long sub) {
        this.mp -= sub;
        if (this.mp <= 0) {
            this.mp = 0;
        }

        if (this.mp > 2_000_000_000) {
            this.mp = 2_000_000_000;
        }
    }

    public long calSucManhTiemNang(long tiemNang) {
        if (power < getPowerLimit()) {
            for (Integer tl : this.tlTNSM) {
                tiemNang += ((long) tiemNang * tl / 100);
            }

            long tn = tiemNang;
            if (this.player.charms.tdTriTue > System.currentTimeMillis()) {
                tiemNang += tn;
            }
            if (this.player.charms.tdTriTue3 > System.currentTimeMillis()) {
                tiemNang += tn * 2;
            }
            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += tn * 3;
            }
            if (this.player.charms.tdTriTue4 > System.currentTimeMillis()) {
                tiemNang += tn * 3;
            }
            if (this.player.effectSkill.isChibi && this.player.typeChibi == 2) {
                tiemNang += tn * 0.95;
            }
            if (this.player.getSession() != null && this.player.getSession().vip > 0 || this.player.isPet
                    && ((Pet) this.player).master.getSession() != null && ((Pet) this.player).master.getSession().vip > 0) {
                tiemNang += tn * 3;
            }
            if (this.player.itemTime != null && this.player.itemTime.isUseDK) {
                tiemNang += tn * 2;
            }
            if (player.zone.map.mapId >= 135 && player.zone.map.mapId <= 138) {
                if (this.player.itemTime != null && this.player.itemTime.isUseKhoBauX2) {
                    tiemNang += tn * 2;
                }
            }
            if (this.player.satellite != null && this.player.satellite.isIntelligent) {
                tiemNang += tn / 5;
            }
            if (this.intrinsic != null && this.intrinsic.id == 24) {
                tiemNang += ((long) tiemNang * this.intrinsic.param1 / 100);
            }
            if (this.power >= 60_000_000_000L) {
                tiemNang -= ((long) tiemNang * 80 / 100);
            }
            if (this.player.isPet) {
                if (((Pet) this.player).master.itemTime.isUseBuaSanta) {
                    tiemNang += tn * 2;
                }
                if (((Pet) this.player).master.nPoint != null && ((Pet) this.player).master.nPoint.tlTNSMPet > 0) {
                    tiemNang += tn / 100 * (((Pet) this.player).master.nPoint.tlTNSMPet + 100);
                }
            }
            if (MapService.gI().isMapNguHanhSon(this.player.zone.map.mapId)) {
            }
            if (MapService.gI().isMapBanDoKhoBau(this.player.zone.map.mapId)) {
                tiemNang *= 2;
            }
            if (this.player.cFlag != 0) {
                if (this.player.cFlag == 8) {
                    tiemNang += ((long) tiemNang * 10 / 100);
                } else {
                    tiemNang += ((long) tiemNang * 5 / 100);
                }
            }
            tiemNang *= GameData.RATE_EXP_SERVER;
            tiemNang = calSubTNSM(tiemNang);
            if (tiemNang <= 0) {
                tiemNang = 1;
            }
        } else {
            tiemNang = 10;
        }
        return tiemNang;
    }

    public long calSubTNSM(long tiemNang) {
        if (power >= 80_000_000_000L) {
            tiemNang /= 30;
        } else if (power >= 60_000_000_000L) {
            tiemNang /= 20;
        } else if (power >= 50_000_000_000L) {
            tiemNang /= 10;
        } else if (power >= 40_000_000_000L) {
            tiemNang /= 5;
        }
        return tiemNang;
    }

    public short getTileHutHp(boolean isMob) {
        if (isMob) {
            return (short) (this.tlHutHp + this.tlHutHpMob);
        } else {
            return this.tlHutHp;
        }
    }

    public short getTiLeHutMp() {
        return this.tlHutMp;
    }

    public int subDameInjureWithDeff(long dame) {
        long def = this.def;
        dame -= def;
        if (dame < 0) {
            dame = 1;
        }
        return (int) dame;
    }

    /*------------------------------------------------------------------------*/
    public boolean canOpenPower() {
        return this.power >= getPowerLimit();
    }

    public long getPowerLimit() {
        switch (limitPower) {
            case 0:
                return 17999999999L; // Chưa mở giới hạn
            case 1:
                return 19999999999L; // Đã mở giới hạn lần 1
            case 2:
                return 24999999999L; // Đã mở giới hạn lần 2
            case 3:
                return 29999999999L; // Đã mở giới hạn lần 3
            case 4:
                return 39999999999L; // Đã mở giới hạn lần 4
            case 5:
                return 50010000000L; // Đã mở giới hạn lần 5
            case 6:
                return 60010000000L; // Đã mở giới hạn lần 6
            case 7:
                return 70010000000L; // Đã mở giới hạn lần 7
            case 8:
                return 80010000000L; // Đã mở giới hạn lần 8
            default:
                return 0;
        }
    }

    public long getPowerNextLimit() {
        switch (limitPower + 1) {
            case 0:
                return 17999999999L; // Chưa mở giới hạn
            case 1:
                return 19999999999L; // Đã mở giới hạn lần 1
            case 2:
                return 24999999999L; // Đã mở giới hạn lần 2
            case 3:
                return 29999999999L; // Đã mở giới hạn lần 3
            case 4:
                return 39999999999L; // Đã mở giới hạn lần 4
            case 5:
                return 50010000000L; // Đã mở giới hạn lần 5
            case 6:
                return 60010000000L; // Đã mở giới hạn lần 6
            case 7:
                return 70010000000L; // Đã mở giới hạn lần 7
            case 8:
                return 80010000000L; // Đã mở giới hạn lần 8
            default:
                return 0;
        }
    }

    public int getHpMpLimit() {
        switch (limitPower) {
            case 0:
                return 220000; // Chưa mở giới hạn
            case 1:
                return 240000; // Đã mở giới hạn lần 1
            case 2:
                return 300000; // Đã mở giới hạn lần 2
            case 3:
                return 350000; // Đã mở giới hạn lần 3
            case 4:
                return 400000; // Đã mở giới hạn lần 4
            case 5:
                return 450000; // Đã mở giới hạn lần 5
            case 6:
                return 500000; // Đã mở giới hạn lần 6
            case 7:
                return 525000; // Đã mở giới hạn lần 7
            case 8:
                return 550000; // Đã mở giới hạn lần 8
            default:
                return 0;
        }
    }

    public int getDameLimit() {
        switch (limitPower) {
            case 0:
                return 11000; // Chưa mở giới hạn
            case 1:
                return 12000; // Đã mở giới hạn lần 1
            case 2:
                return 15000; // Đã mở giới hạn lần 2
            case 3:
                return 18000; // Đã mở giới hạn lần 3
            case 4:
                return 20000; // Đã mở giới hạn lần 4
            case 5:
                return 22000; // Đã mở giới hạn lần 5
            case 6:
                return 24000; // Đã mở giới hạn lần 6
            case 7:
                return 24500; // Đã mở giới hạn lần 7
            case 8:
                return 25000; // Đã mở giới hạn lần 8
            default:
                return 0;
        }
    }

    public short getDefLimit() {
        switch (limitPower) {
            case 0:
                return 550; // Chưa mở giới hạn
            case 1:
                return 600; // Đã mở giới hạn lần 1
            case 2:
                return 700; // Đã mở giới hạn lần 2
            case 3:
                return 800; // Đã mở giới hạn lần 3
            case 4:
                return 1000; // Đã mở giới hạn lần 4
            case 5:
                return 1200; // Đã mở giới hạn lần 5
            case 6:
                return 1400; // Đã mở giới hạn lần 6
            case 7:
                return 1500; // Đã mở giới hạn lần 7
            case 8:
                return 1600; // Đã mở giới hạn lần 8
            default:
                return 0;
        }
    }

    public byte getCritLimit() {
        switch (limitPower) {
            case 0:
                return 1; // Chưa mở giới hạn
            case 1:
                return 2; // Đã mở giới hạn lần 1
            case 2:
                return 3; // Đã mở giới hạn lần 2
            case 3:
                return 4; // Đã mở giới hạn lần 3
            case 4:
                return 5; // Đã mở giới hạn lần 4
            case 5:
                return 6; // Đã mở giới hạn lần 5
            case 6:
                return 7; // Đã mở giới hạn lần 6
            case 7:
                return 8; // Đã mở giới hạn lần 7
            case 8:
                return 9; // Đã mở giới hạn lần 8
            default:
                return 0;
        }
    }

    public void powerUp(long power) {
        this.power += power;
        TaskService.gI().checkDoneTaskPower(player, this.power);
    }

    public void tiemNangUp(long tiemNang) {
        this.tiemNang += tiemNang;
    }

    public void increasePoint(byte type, short point) {
        if (point <= 0 || point > 1000) {
            return;
        }
        long tiemNangUse;
        if (type == 0) {
            int pointHp = point * 20;
            tiemNangUse = point * (2 * (this.hpg + 1000) + pointHp - 20) / 2;
            if ((this.hpg + pointHp) <= getHpMpLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    hpg += pointHp;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 1) {
            int pointMp = point * 20;
            tiemNangUse = point * (2 * (this.mpg + 1000) + pointMp - 20) / 2;
            if ((this.mpg + pointMp) <= getHpMpLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    mpg += pointMp;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 2) {
            TaskService.gI().checkDoneTaskNangCS(player);
            tiemNangUse = point * (2 * this.dameg + point - 1) / 2 * 100;
            if ((this.dameg + point) <= getDameLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    dameg += point;
                }
                TaskService.gI().checkDoneTaskNangCS(player);
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 3) {
            tiemNangUse = 2 * (this.defg + 5) / 2 * 100000;
            if ((this.defg + point) <= getDefLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    defg += point;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        if (type == 4) {
            tiemNangUse = 50000000L;
            for (int i = 0; i < this.critg; i++) {
                tiemNangUse *= 5L;
            }
            if ((this.critg + point) <= getCritLimit()) {
                if (doUseTiemNang(tiemNangUse)) {
                    critg += point;
                }
            } else {
                Service.gI().sendThongBaoOK(player, "Vui lòng mở giới hạn sức mạnh");
                return;
            }
        }
        Service.gI().point(player);
    }

    private boolean doUseTiemNang(long tiemNang) {
        if (this.tiemNang < tiemNang) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ tiềm năng");
            return false;
        }
        if (this.tiemNang >= tiemNang && this.tiemNang - tiemNang >= 0) {
            this.tiemNang -= tiemNang;
            TaskService.gI().checkDoneTaskUseTiemNang(player);
            return true;
        }
        return false;
    }

    public long getFullTN() {
        long tnhp = 0, tnki = 0, tnsd = 0, tng = 0, tncm = 0;

        if (hpg > 0) {
            tnhp = (((hpg / 20L) * (50L + (50L + (hpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (mpg > 0) {
            tnki = (((mpg / 20L) * (50L + (50L + (mpg / 20L) - 1L)) / 2L) * 20L);
        }
        if (dameg > 0) {
            tnsd = ((dameg * (dameg - 1L) * 100L) / 2L);
        }
        if (defg > 0) {
            tng = ((defg * (500000L + (500000L + (defg - 1L) * 100000L))) / 2L);
        }
        if (critg > 0) {
            tncm = ((50L * (((long) Math.pow(5L, critg) - 1L)) / (5L - 1L) * 1000000L));
        }
        return tnhp + tnki + tnsd + tng + tncm;
    }

    public void update() {
        if (player != null && player.effectSkill != null) {
            if (player.effectSkill.isCharging && player.effectSkill.countCharging < 10) {
                int tiLeHoiPhuc = SkillUtil.getPercentCharge(player.playerSkill.skillSelect.point);
                if (player.effectSkill.isCharging && !player.isDie() && !player.effectSkill.isHaveEffectSkill()
                        && (hp < hpMax || mp < mpMax)) {
                    long hpRecovered = hpMax / 100 * tiLeHoiPhuc;
                    long mpRecovered = mpMax / 100 * tiLeHoiPhuc;

                    if (hp + hpRecovered > 2_000_000_000) {
                        hpRecovered = 2_000_000_000 - hp;
                    }
                    if (mp + mpRecovered > 2_000_000_000) {
                        mpRecovered = 2_000_000_000 - mp;
                    }

                    PlayerService.gI().hoiPhuc(player, hpRecovered, mpRecovered);

                    if (player.effectSkill.countCharging % 3 == 0) {
                        Service.gI().chat(player, "Phục hồi năng lượng " + getCurrPercentHP() + "%");
                    }
                } else {
                    EffectSkillService.gI().stopCharge(player);
                }
                if (++player.effectSkill.countCharging >= 10) {
                    EffectSkillService.gI().stopCharge(player);
                }
            }

            if (Util.canDoWithTime(lastTimeHoiPhuc, 30000)) {
                PlayerService.gI().hoiPhuc(this.player, hpHoi, mpHoi);
                this.lastTimeHoiPhuc = System.currentTimeMillis();
            }

            if (Util.canDoWithTime(lastTimeHoiStamina, 60000) && this.stamina < this.maxStamina) {
                this.stamina++;
                this.lastTimeHoiStamina = System.currentTimeMillis();

                if (!this.player.isBoss && !this.player.isPet) {
                    PlayerService.gI().sendCurrentStamina(this.player);
                }
            }
        }
    }

    public void dispose() {
        this.intrinsic = null;
        this.player = null;
        this.tlHp = null;
        this.tlMp = null;
        this.tlDef = null;
        this.tlDame = null;
        this.tlDameAttMob = null;
        this.tlTNSM = null;
    }

    public long calPercent(long param, int percent) {
        return param * percent / 100;
    }

}
