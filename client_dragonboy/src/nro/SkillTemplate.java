package nro;

public final class SkillTemplate {
   public byte id;
   public String name;
   public int maxPoint;
   public int manaUseType;
   public int type;
   public int iconId;
   public String[] description;
   public Skill[] skills;
   public String damInfo;

   public final boolean isBuffSkill() {
      return this.type == 2;
   }

   public final boolean isSpecialSkill() {
      return this.type == 3;
   }

   public final boolean isPassiveSkill() {
      return this.type == 4;
   }
}
