package nro;

public final class Skill {
   public SkillTemplate template;
   public short skillId;
   public int point;
   public long power_require;
   public int coolDown;
   public long lastTimeUse;
   public int dx;
   public int dy;
   public int maxFight;
   public boolean isMainSkill = false;
   public short damage;
   public String moreInfo;
   public short mana_use;

   public final String getCoolDownSeconds() {
      if (this.coolDown % 1000 == 0) {
         return String.valueOf(this.coolDown / 1000);
      } else {
         int var1 = this.coolDown % 1000;
         return this.coolDown / 1000 + "." + (var1 % 100 == 0 ? var1 / 100 : var1 / 10);
      }
   }
}
