package nro;

public final class SettingMn {
   public static boolean a = false;
   private static SettingMn l;
   public static float b = 0.5F;
   public static int c = 30;
   public static int d = 31;
   public static int e = 32;
   public static int f = 33;
   public static int g = 34;
   public static int h = 35;
   public static int i = 36;
   public static int j = 37;
   public static int k = 38;

   public static SettingMn stopAll() {
      if (l == null) {
         l = new SettingMn();
      }

      return l;
   }

   public final void b() {
      if (Char.isEnableSpecialAura) {
         Rms.saveRMSInt("isPaintAura", 0);
         Char.isEnableSpecialAura = false;
      } else {
         Rms.saveRMSInt("isPaintAura", 1);
         Char.isEnableSpecialAura = true;
      }

      g();
   }

   public final void c() {
      if (!main.GameCanvas.isTouch) {
         if (GameScreen.bU = !GameScreen.bU) {
            Rms.saveRMSInt("serverchat", 0);
         } else {
            Rms.saveRMSInt("serverchat", 1);
         }

         g();
      } else {
         if (GameScreen.aN == 0) {
            GameScreen.aN = 1;
            Rms.saveRMSInt("analog", GameScreen.aN);
            GameScreen.p();
         } else {
            GameScreen.aN = 0;
            Rms.saveRMSInt("analog", GameScreen.aN);
            GameScreen.p();
         }

         g();
      }
   }

   public final void d() {
      if (main.GameCanvas.isLowGraphic) {
         Rms.saveRMSInt("lowGraphic", 0);
         main.GameCanvas.startOK(mResources.cL, 8885, (Object)null);
      } else {
         Rms.saveRMSInt("lowGraphic", 1);
         main.GameCanvas.startOK(mResources.cL, 8885, (Object)null);
      }

      g();
   }

   public final void e() {
      if (Char.isEnablePowerAura) {
         Rms.saveRMSInt("isPaintAura2", 0);
         Char.isEnablePowerAura = false;
      } else {
         Rms.saveRMSInt("isPaintAura2", 1);
         Char.isEnablePowerAura = true;
      }

      g();
   }

   public static void f() {
      if (main.GameCanvas.loginScr.isLogin2 && Char.myCharz().aD != null && Char.myCharz().aD.c >= 2) {
         Panel.F = new String[]{mResources.j, mResources.bS, mResources.bR, mResources.bQ, mResources.aF, mResources.bP, mResources.ap, mResources.bT, mResources.T, mResources.I};
         if (Char.myCharz().havePet) {
            Panel.F = new String[]{mResources.j, mResources.bS, mResources.bR, mResources.x, mResources.bQ, mResources.aF, mResources.bP, mResources.ap, mResources.bT, mResources.T, mResources.I};
         }
      } else {
         Panel.F = new String[]{mResources.j, mResources.bS, mResources.bR, mResources.bQ, mResources.aF, mResources.bP, mResources.ap, mResources.bT, mResources.T};
         if (Char.myCharz().havePet) {
            Panel.F = new String[]{mResources.j, mResources.bS, mResources.bR, mResources.x, mResources.bQ, mResources.aF, mResources.bP, mResources.ap, mResources.bT, mResources.T};
         }
      }

      if (a) {
         String[] var0 = new String[Panel.F.length + 1];

         for(int var1 = 0; var1 < Panel.F.length; ++var1) {
            var0[var1] = Panel.F[var1];
         }

         var0[Panel.F.length] = mResources.e;
         Panel.F = var0;
      }

   }

  public static void g() {
      String iconOn = "[x]   "; 
      String iconOff = "[  ]   "; 
      
      String strAnalogOrChat;
      if (main.GameCanvas.isTouch) {
          strAnalogOrChat = (GameScreen.aN == 1) ? iconOn + mResources.G : iconOff + mResources.G;
      } else {
          strAnalogOrChat = (GameScreen.bU) ? iconOn + mResources.bW : iconOff + mResources.bW;
      }

      Panel.G = new String[]{
          Char.isEnableSpecialAura ? iconOn + mResources.ca : iconOff + mResources.ca,
          Char.isEnablePowerAura ? iconOn + mResources.cb : iconOff + mResources.cb, 
          main.GameCanvas.isEnableSound ? iconOn + mResources.fz : iconOff + mResources.fz, 
          main.GameCanvas.isLowGraphic ? iconOn + mResources.z : iconOff + mResources.z,   
          strAnalogOrChat 
      };
   }
   public static void h() {
      Session_ME2.Session_ME().close();
      main.GameCanvas.panel.A();
      main.GameCanvas.loginScr.endDlg();
      main.GameCanvas.loginScr.switchToMe();
   }
}
