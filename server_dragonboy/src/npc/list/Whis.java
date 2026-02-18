package npc.list;

import boss.BossID;
import consts.ConstNpc;
import item.Item;
import java.io.IOException;
import logger.NLogger;
import network.Message;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.Service;
import services.SkillService;
import services.CombineService;
import services.TrainingService;
import shop.ShopService;
import player.skill.Skill;
import services.ItemService;
import services.PlayerService;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;

public class Whis extends Npc {

    private static final int COST_HD = 50000000;

    public Whis(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    Item hasBiKipTuyetKy(Player pl){
        return InventoryService.gI().findItem(pl.inventory.itemsBag, 1229);
      
    }
    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 154) {
                if (hasBiKipTuyetKy(player) != null) {
                    createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Thử đánh với ta xem nào.\nNgươi còn 1 lượt nữa cơ mà.",
                            "Nói chuyện", "Học\nkỹ năng", "Học\ntuyệt kỹ", "[LV:" + (player.traning.getTop() + 1) + "]");
                } else {
                    createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Thử đánh với ta xem nào.\nNgươi còn 1 lượt nữa cơ mà.",
                            "Nói chuyện", "Học\nkỹ năng", "[LV:" + (player.traning.getTop() + 1) + "]");
                }
            }else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Đi chỗ khác chơi?",
                        "Dạ");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        // Sub menus theo index
        int index = player.idMark.getIndexMenu();
        if (!canOpenNpc(player)) return;

        if(this.mapId != 154) return;
        if (player.idMark.isBaseMenu()) {
        
            Item bikiptuyetky = hasBiKipTuyetKy(player);
            if (bikiptuyetky != null) {
                switch (select) {
                    case 0 -> this.createOtherMenu(player, 5, "Ta sẽ giúp ngươi chế tạo trang bị thiên sứ", "Shop thiên sứ", "Chế tạo", "Từ chối");
                    case 1 -> openHocKyNangMenu(player);
                    case 2 -> openHocTuyetKyMenu(player, bikiptuyetky);
                    case 3 -> TrainingService.gI().callBoss(player, BossID.WHIS, false);
                    default -> {}
                }
            } else {
                switch (select) {
                    case 0 -> this.createOtherMenu(player, 5, "Ta sẽ giúp ngươi chế tạo trang bị thiên sứ", "Shop thiên sứ", "Chế tạo", "Từ chối");
                    case 1 -> openHocKyNangMenu(player);
                    case 2 -> TrainingService.gI().callBoss(player, BossID.WHIS, false);
                    default -> {}
                }
            }
           
        }
       
        if (index == 5) {
            switch (select) {
                case 0 -> ShopService.gI().opendShop(player, "THIEN_SU", false);
                case 1 -> {
                    if (!player.setClothes.checkSetDes()) {
                        this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Ngươi hãy trang bị đủ 5 món trang bị Hủy Diệt rồi ta nói chuyện tiếp.","OK" );
                    } else {
                        CombineService.gI().openTabCombine(player, CombineService.CHE_TAO_TRANG_BI_THIEN_SU);
                    }
                }
                default -> { /* no-op */ }
            }
            return;
        }

        if (index == CombineService.CHE_TAO_TRANG_BI_THIEN_SU) {
            if (select == 0) CombineService.gI().startCombine(player);
            return;
        }

        if (index == 6) {
            if (select == 0) confirmHocTuyetKy(player);
        }
        
        if(index == ConstNpc.MENU_HOC_KY_NANG){
            switch (select) {
                    case 0 -> {
                        var time = player.LearnSkill.Time - System.currentTimeMillis();
                        var ngoc = 5;
                        if (time / 600_000 >= 2) ngoc += time / 600_000;
                        if (player.inventory.gem < ngoc) {
                            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                            return;
                        }
                        player.inventory.subGem(ngoc);
                        player.LearnSkill.Time = -1;

                        try {
                            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                            byte level = Byte.parseByte(subName[subName.length - 1]);
                            Skill curSkill = SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId);

                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
                            player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                            SkillUtil.setSkill(player, curSkill);

                            var msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            player.sendMessage(msg);
                            msg.cleanup();
                            PlayerService.gI().sendInfoHpMpMoney(player);
                        } catch (Exception e) {
                            NLogger.logError(e);
                        }
                    }
                    case 1 ->
                            createOtherMenu(player, 13, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
                }
        }
    }

    private void openHocKyNangMenu(Player pl){
        if (pl.LearnSkill.Time != -1) {
            var ngoc = 5;
            var time = pl.LearnSkill.Time - System.currentTimeMillis();
            if (time / 600_000 >= 2) ngoc += time / 600_000;
            String[] subName = ItemService.gI().getTemplate(pl.LearnSkill.ItemTemplateSkillId).name.split("");
            byte level = Byte.parseByte(subName[subName.length - 1]);
            createOtherMenu(pl, ConstNpc.MENU_HOC_KY_NANG,
                    "Con đang học kỹ năng\n" + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(pl.LearnSkill.ItemTemplateSkillId)).name
                            + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time),
                    "Học Cấp tốc " + ngoc + " ngọc", "Huỷ", "Bỏ qua");
        } else {
            ShopService.gI().opendShop(pl, "QUY_LAO", false);
        }
    }
    private void openHocTuyetKyMenu(Player player, Item Biky) {
        int idSkill = switch (player.gender) {
            case 0 -> Skill.SUPER_KAME;
            case 1 -> Skill.MA_PHONG_BA;
            case 2 -> Skill.LIEN_HOAN_CHUONG;
            default -> Skill.MA_PHONG_BA;
        };
        Skill currentSkill = SkillUtil.getSkillbyId(player, idSkill);
        boolean firstLearn = currentSkill == null || currentSkill.point == 0;
        int BikiptuyetkyRequire = firstLearn ? 9999 : 999; // Điều kiện yêu cầu bí kíp
        int skillLevel = firstLearn ? 1 : currentSkill.point + 1;

        String skillName = switch (player.gender) {
            case 0 -> "Super kamejoko";
            case 1 -> "Ma phong ba";
            case 2 -> "Ca đíc liên hoàn chưởng";
            default -> "Skill đặc biệt";
        };

        createOtherMenu(player, 6,
                "|1|Ta sẽ dạy ngươi tuyệt kỹ " + skillName + " " + skillLevel +
                        "\n|7|Bí kiếp tuyệt kỹ: " + Biky.quantity + "/" + BikiptuyetkyRequire +
                        "\n|2|Giá vàng: 10.000.000\n|2|Giá ngọc: 99",
                "Đồng ý", "Từ chối");
    }

    private void confirmHocTuyetKy(Player player) {
        Item Biky = InventoryService.gI().findItemBag(player, 1229);
        if (Biky == null) return;

        if (player.nPoint.power < 60000000000L) {
            Service.gI().sendThongBao(player, "Ngươi không đủ sức mạnh để học tuyệt kỹ");
            return;
        }
        if (player.inventory.gold < 10000000) {
            Service.gI().sendThongBao(player, "Hãy có đủ vàng thì quay lại gặp ta.");
            return;
        }
        if (player.inventory.gem <= 99) {
            Service.gI().sendThongBao(player, "Hãy có đủ ngọc xanh thì quay lại gặp ta.");
            return;
        }

        int idSkill = switch (player.gender) {
            case 0 -> Skill.SUPER_KAME;
            case 1 -> Skill.MA_PHONG_BA;
            case 2 -> Skill.LIEN_HOAN_CHUONG;
            default -> Skill.MA_PHONG_BA;
        };
        Skill currentSkill = SkillUtil.getSkillbyId(player, idSkill);
        boolean firstLearn = currentSkill == null || currentSkill.point == 0;
        int BikiptuyetkyRequire = firstLearn ? 9999 : 999;

        if (Biky.quantity < BikiptuyetkyRequire) {
            Service.gI().sendThongBao(player, "Ngươi còn thiếu " + (BikiptuyetkyRequire - Biky.quantity) + " bí kíp nữa.\nHãy tìm đủ rồi đến gặp ta.");
            return;
        }

        try {
            boolean success = firstLearn ? Util.isTrue(15, 15) : Util.isTrue(1, 30);
            int trubk = success ? BikiptuyetkyRequire : 99;
            int iconSkill = success ? switch (player.gender) {
                case 0 -> 11162;
                case 1 -> 11194;
                case 2 -> 11193;
                default -> 11194;
            } : 15313;
            String msg = success ? (firstLearn ? "Học skill thành công!" : "Nâng skill thành công!") : "Tư chất kém!";
            String msg2 = success ? "Chúc mừng con nhé!" : "Ngu dốt!";

            if (success) {
                if (firstLearn) {
                    SkillService.gI().learSkillSpecial(player, (byte) idSkill);
                } else {
                    currentSkill.point++;
                    currentSkill.currLevel = 0;
                    SkillService.gI().sendCurrLevelSpecial(player, currentSkill);
                }
            }

            Message ms = new Message(-81);
            ms.writer().writeByte(0);
            ms.writer().writeUTF("Skill 9");
            ms.writer().writeUTF("---");
            ms.writer().writeShort(tempId);
            player.sendMessage(ms);
            ms.cleanup();

            ms = new Message(-81);
            ms.writer().writeByte(1);
            ms.writer().writeByte(1);
            ms.writer().writeByte(InventoryService.gI().getIndexItemBag(player, Biky));
            player.sendMessage(ms);
            ms.cleanup();

            ms = new Message(-81);
            ms.writer().writeByte(trubk == 99 ? 8 : 7);
            ms.writer().writeShort(iconSkill);
            player.sendMessage(ms);
            ms.cleanup();

            npcChat(player, msg2);
            Service.gI().sendThongBao(player, msg);

            InventoryService.gI().subQuantityItemsBag(player, Biky, trubk);
            player.inventory.gold -= 10000000;
            player.inventory.gem -= 99;
            InventoryService.gI().sendItemBags(player);

        } catch (IOException e) {
        }
    }
}