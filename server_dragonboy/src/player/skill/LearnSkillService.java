/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package player.skill;

import item.Item;
import java.io.IOException;
import logger.MyLogger;
import network.Message;
import npc.Npc;
import player.Player;
import services.ItemService;
import services.PlayerService;
import services.Service;
import services.SkillService;
import services.player.InventoryService;
import utils.SkillUtil;
import utils.Util;

/**
 *
 * @author er3
 */
public class LearnSkillService {

    private static LearnSkillService instance;

    public static LearnSkillService gI() {
        if (instance == null) {
            instance = new LearnSkillService();
        }
        return instance;
    }

    public byte getLevelFromItem(int itemTemplateId) {
        try {
            String name = ItemService.gI().getTemplate(itemTemplateId).name.trim();
            return Byte.parseByte(name.substring(name.length() - 1));
        } catch (Exception e) {
            return 0;
        }
    }

    public void cancelLearn(Player player) {
        if (player.LearnSkill.Time != -1) {
            long refund = player.LearnSkill.Potential / 2;
            player.nPoint.tiemNang += refund;

            player.LearnSkill.Time = -1;
            player.LearnSkill.ItemTemplateSkillId = -1;
            player.LearnSkill.Potential = 0;

            Service.gI().sendThongBao(player, "Đã hủy học kỹ năng. Con nhận lại " + refund + " tiềm năng.");
            PlayerService.gI().sendInfoHpMpMoney(player);
        }
    }

    public void useItemLearnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                byte level = getLevelFromItem(item.template.id);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {//Hoc skill moi
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            pl.BoughtSkill.add((int) item.template.id);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else { // skill lv > 1
                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            pl.BoughtSkill.add((int) item.template.id);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp " + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }

    public void finishLearnSkill(Player pl) {
        if (pl.LearnSkill.ItemTemplateSkillId == -1) {
            return;
        }

        try {
            int itemId = (int) pl.LearnSkill.ItemTemplateSkillId;
            byte levelTarget = getLevelFromItem(itemId);

            Skill newSkill = SkillUtil.createSkill(
                    SkillUtil.getTempSkillSkillByItemID(itemId),
                    levelTarget
            );

            SkillUtil.setSkill(pl, newSkill);
            pl.BoughtSkill.add(itemId);

            pl.LearnSkill.Time = -1;
            pl.LearnSkill.ItemTemplateSkillId = -1;
            pl.LearnSkill.Potential = 0;

            PlayerService.gI().sendInfoHpMpMoney(pl);

            Message msg = Service.gI().messageSubCommand((byte) 62);
            msg.writer().writeShort(newSkill.skillId);
            pl.sendMessage(msg);
            msg.cleanup();

            InventoryService.gI().sendItemBags(pl);

    

        } catch (Exception e) {
            MyLogger.logError(e);
        }
    }
    // Hiển thị menu học tuyệt kỹ (Giữ nguyên logic yêu cầu bí kíp)
    public void openHocTuyetKyMenu(Npc npc, Player player, Item bikip) {
        int idSkill = switch (player.gender) {
            case 0 -> Skill.SUPER_KAME;
            case 1 -> Skill.MA_PHONG_BA;
            case 2 -> Skill.LIEN_HOAN_CHUONG;
            default -> Skill.MA_PHONG_BA;
        };
        Skill currentSkill = SkillUtil.getSkillbyId(player, idSkill);
        boolean firstLearn = currentSkill == null || currentSkill.point == 0;
        int bikipRequire = firstLearn ? 9999 : 999;
        int skillLevel = firstLearn ? 1 : currentSkill.point + 1;

        String skillName = switch (player.gender) {
            case 0 -> "Super kamejoko";
            case 1 -> "Ma phong ba";
            case 2 -> "Ca đíc liên hoàn chưởng";
            default -> "Skill đặc biệt";
        };

        npc.createOtherMenu(player, 6,
                "|1|Ta sẽ dạy ngươi tuyệt kỹ " + skillName + " " + skillLevel +
                        "\n|7|Bí kiếp tuyệt kỹ: " + bikip.quantity + "/" + bikipRequire +
                        "\n|2|Giá vàng: 10.000.000\n|2|Giá ngọc: 99",
                "Đồng ý", "Từ chối");
    }

    public void confirmHocTuyetKy(Npc npc, Player player) {
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
        int bikipRequire = firstLearn ? 9999 : 999;

        if (Biky.quantity < bikipRequire) {
            Service.gI().sendThongBao(player, "Ngươi còn thiếu " + (bikipRequire - Biky.quantity) + " bí kíp nữa.\nHãy tìm đủ rồi đến gặp ta.");
            return;
        }

        try {
            boolean success = firstLearn ? Util.isTrue(15, 15) : Util.isTrue(1, 30);
            int trubk = success ? bikipRequire : 99;
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
            ms.writer().writeShort(npc.tempId);
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

            npc.npcChat(player, msg2);
            Service.gI().sendThongBao(player, msg);

            InventoryService.gI().subQuantityItemsBag(player, Biky, trubk);
            player.inventory.gold -= 10000000;
            player.inventory.gem -= 99;
            
            InventoryService.gI().sendItemBags(player);
            Service.gI().point(player);
            PlayerService.gI().sendInfoHpMpMoney(player);

        } catch (IOException e) {
            MyLogger.logError(e);
        }
    }
    public void openHocNhanhMenu(Npc npc, Player pl) {
        if (pl.LearnSkill.Time != -1) {
            long timeRemaining = pl.LearnSkill.Time - System.currentTimeMillis();
            
            int ngocCost = 5;
            if (timeRemaining / 600_000 >= 2) {
                ngocCost += (int) (timeRemaining / 600_000);
            }

            byte levelTarget = getLevelFromItem((int) pl.LearnSkill.ItemTemplateSkillId);
            String skillName = SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(pl.LearnSkill.ItemTemplateSkillId)).name;

            npc.createOtherMenu(pl, 12,
                    "Con đang học kỹ năng\n" + skillName + " cấp " + levelTarget +
                    "\nThời gian còn lại " + utils.TimeUtil.getTime(timeRemaining),
                    "Học Cấp tốc\n" + ngocCost + " ngọc", "Huỷ", "Bỏ qua");
        }
    }

    public void finishLearnSkillFast(Player pl) {
        if (pl.LearnSkill.Time == -1) return;

        long timeRemaining = pl.LearnSkill.Time - System.currentTimeMillis();
        int ngocCost = (timeRemaining / 600_000 >= 2) ? (5 + (int) (timeRemaining / 600_000)) : 5;

        if (pl.inventory.gem < ngocCost) {
            Service.gI().sendThongBao(pl, "Bạn không có đủ ngọc để học cấp tốc");
            return;
        }

        pl.inventory.subGem(ngocCost);
        Service.gI().sendMoney(pl); 
        finishLearnSkill(pl); 
    }

}
