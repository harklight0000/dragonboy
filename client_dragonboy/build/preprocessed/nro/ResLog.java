package nro;

import java.util.Random;

public final class ResLog {
   private static short[] sinTable = new short[]{0, 18, 36, 54, 71, 89, 107, 125, 143, 160, 178, 195, 213, 230, 248, 265, 282, 299, 316, 333, 350, 367, 384, 400, 416, 433, 449, 465, 481, 496, 512, 527, 543, 558, 573, 587, 602, 616, 630, 644, 658, 672, 685, 698, 711, 724, 737, 749, 761, 773, 784, 796, 807, 818, 828, 839, 849, 859, 868, 878, 887, 896, 904, 912, 920, 928, 935, 943, 949, 956, 962, 968, 974, 979, 984, 989, 994, 998, 1002, 1005, 1008, 1011, 1014, 1016, 1018, 1020, 1022, 1023, 1023, 1024, 1024};
   private static short[] cosTable;
   private static int[] tanTable;
   public static Random randomGen;
   public static boolean isDebug;
   public static boolean isShowLog;

   static {
      String[] var10000 = new String[]{"<color=#ff0000ff>[  LOG_CAT  ]</color>", "<color=#ff0000ff>[LOG_SESSION]</color>", "<color=#ffff00ff>[LOG_SESSION]</color>", "<color=#ff0000ff>[LOG_MOBILE ]</color>", ""};
      randomGen = new Random();
      new MyVector("");
   }

   public static void initTrigonometry() {
      cosTable = new short[91];
      tanTable = new int[91];

      for(int var0 = 0; var0 <= 90; ++var0) {
         cosTable[var0] = sinTable[90 - var0];
         if (cosTable[var0] == 0) {
            tanTable[var0] = Integer.MAX_VALUE;
         } else {
            tanTable[var0] = (sinTable[var0] << 10) / cosTable[var0];
         }
      }

   }

   public static final int sin(int var0) {
      if ((var0 = fixAngle(var0)) >= 0 && var0 < 90) {
         return sinTable[var0];
      } else if (var0 >= 90 && var0 < 180) {
         return sinTable[180 - var0];
      } else {
         return var0 >= 180 && var0 < 270 ? -sinTable[var0 - 180] : -sinTable[360 - var0];
      }
   }

   public static final int cos(int var0) {
      if ((var0 = fixAngle(var0)) >= 0 && var0 < 90) {
         return cosTable[var0];
      } else if (var0 >= 90 && var0 < 180) {
         return -cosTable[180 - var0];
      } else {
         return var0 >= 180 && var0 < 270 ? -cosTable[var0 - 180] : cosTable[360 - var0];
      }
   }

   public static final int getAngle(int var0, int var1) {
      int var10000;
      int var2;
      if (var0 != 0) {
         var2 = Math.abs((var1 << 10) / var0);

         label44: {
            for(int var3 = 0; var3 <= 90; ++var3) {
               if (tanTable[var3] >= var2) {
                  var10000 = var3;
                  break label44;
               }
            }

            var10000 = 0;
         }

         var2 = var10000;
         if (var1 >= 0 && var0 < 0) {
            var2 = 180 - var2;
         }

         if (var1 < 0 && var0 < 0) {
            var2 += 180;
         }

         if (var1 >= 0 || var0 < 0) {
            return var2;
         }

         var10000 = 360 - var2;
      } else {
         var10000 = var1 > 0 ? 90 : 270;
      }

      var2 = var10000;
      return var2;
   }

   public static final int fixAngle(int var0) {
      if (var0 >= 360) {
         var0 -= 360;
      }

      if (var0 < 0) {
         var0 += 360;
      }

      return var0;
   }

   public static void drawDebugInfo(mGraphics var0) {
      mFont.tahoma_7b_dark.drawStringBd(var0, "check Controller= " + Service.d, 3, 55, 0);
      mFont.tahoma_7b_dark.drawStringBd(var0, "check Map= " + Service.e, 3, 75, 0);
   }

   public static void updateChecks() {
      long currTime = mSystem.currentTimeMillis();
      if (Service.b - currTime > 5000L) {
         Service.gI().sendCheckController();
      }

      if (Service.c - currTime > 5000L) {
         Service.gI().sendCheckMap();
      }

   }

   public static String filterName(String name) {
      if (mSystem.d != 7) {
         return name;
      } else if (Char.myCharz() != null && Char.myCharz().aD != null && Char.myCharz().aD.c >= 10) {
         return name;
      } else {
         if (name.indexOf("Gohan") != -1) {
            name = replace(name, "Gohan", "Hango");
         }

         if (name.indexOf("Gôhan") != -1) {
            name = replace(name, "Gôhan", "Hango");
         }

         if (name.indexOf("Bunma") != -1) {
            name = replace(name, "Bunma", "Mabun");
         }

         if (name.indexOf("Bunman") != -1) {
            name = replace(name, "Bunman", "Mabun");
         }

         if (name.indexOf("Bun ma") != -1) {
            name = replace(name, "Bun ma", "Mabun");
         }

         if (name.indexOf("Đậu thần") != -1) {
            name = replace(name, "Đậu thần", "Cây thần");
         }

         if (name.indexOf("Mabu") != -1) {
            name = replace(name, "Mabu", "Buma");
         }

         if (name.indexOf("Ma bư") != -1) {
            name = replace(name, "Ma bư", "Buma");
         }

         if (name.indexOf("Xayda") != -1) {
            name = replace(name, "Xayda", "Dasay");
         }

         if (name.indexOf("Xay da") != -1) {
            name = replace(name, "Xay da", "Dasay");
         }

         if (name.indexOf("Namếc") != -1) {
            name = replace(name, "Namếc", "Mecda");
         }

         if (name.indexOf("Na mếc") != -1) {
            name = replace(name, "Na mếc", "Mecda");
         }

         if (name.indexOf("Namek") != -1) {
            name = replace(name, "Namek", "Mecda");
         }

         if (name.indexOf("Rồng thần") != -1) {
            name = replace(name, "Rồng thần", "Rồng đất");
         }

         if (name.indexOf("Kame") != -1) {
            name = replace(name, "Kame", "Meka");
         }

         if (name.indexOf("Vegeta") != -1) {
            name = replace(name, "Vegeta", "Tageve");
         }

         if (name.indexOf("Kakalot") != -1) {
            name = replace(name, "Kakalot", "Lotkaka");
         }

         if (name.indexOf("Broly") != -1) {
            name = replace(name, "Broly", "Lybro");
         }

         if (name.indexOf("Ngọc rồng") != -1) {
            name = replace(name, "Ngọc rồng", "Ngọc thần");
         }

         if (name.indexOf("ngọc rồng") != -1) {
            name = replace(name, "ngọc rồng", "Ngọc thần");
         }

         if (name.indexOf("Radic") != -1) {
            name = replace(name, "Radic", "Dicra");
         }

         if (name.indexOf("Ra dic") != -1) {
            name = replace(name, "Ra dic", "Dicra");
         }

         if (name.indexOf("Ra đíc") != -1) {
            name = replace(name, "Ra đíc", "Dicra");
         }

         if (name.indexOf("Cadic") != -1) {
            name = replace(name, "Cadic", "Dicca");
         }

         if (name.indexOf("Ca dic") != -1) {
            name = replace(name, "Ca dic", "Dicca");
         }

         if (name.indexOf("Ca đíc") != -1) {
            name = replace(name, "Ca đíc", "Dicca");
         }

         if (name.indexOf("Quy lão") != -1) {
            name = replace(name, "Quy lão", "Lão");
         }

         if (name.indexOf("quy lão") != -1) {
            name = replace(name, "quy lão", "lão");
         }

         if (name.indexOf("QuyLão") != -1) {
            name = replace(name, "Quy Lão", "Lão");
         }

         if (name.indexOf("Santa") != -1) {
            name = replace(name, "Santa", "Tasan");
         }

         if (name.indexOf("Hạt Mít") != -1) {
            name = replace(name, "Hạt Mít", "Hạt Dẻ");
         }

         if (name.indexOf("Hạt mít") != -1) {
            name = replace(name, "Hạt mít", "Hạt dẻ");
         }

         if (name.indexOf("Tàu bảy bảy") != -1) {
            name = replace(name, "Tàu bảy bảy", "Tàu tàu");
         }

         if (name.indexOf("Uron") != -1) {
            name = replace(name, "Uron", "Onru");
         }

         if (name.indexOf("U ron") != -1) {
            name = replace(name, "U ron", "Onru");
         }

         if (name.indexOf("Urôn") != -1) {
            name = replace(name, "Urôn", "Onru");
         }

         if (name.indexOf("Ngọc Rồng") != -1) {
            name = replace(name, "Ngọc Rồng", "Ngọc thần");
         }

         if (name.indexOf("Ngọc rồng") != -1) {
            name = replace(name, "Ngọc rồng", "Ngọc thần");
         }

         if (name.indexOf("Fide") != -1) {
            name = replace(name, "Fide", "Defi");
         }

         if (name.indexOf("Vegeta") != -1) {
            name = replace(name, "Vegeta", "Tageve");
         }

         if (name.indexOf("Moori") != -1) {
            name = replace(name, "Moori", "Rimoo");
         }

         if (name.indexOf("Aru") != -1) {
            name = replace(name, "Aru", "Ura");
         }

         if (name.indexOf("Kamejoko") != -1) {
            name = replace(name, "Kamejoko", "Kojomeka");
         }

         if (name.indexOf("kamejoko") != -1) {
            name = replace(name, "kamejoko", "kojomeka");
         }

         if (name.indexOf("Kame") != -1) {
            name = replace(name, "Kame", "Meka");
         }

         if (name.indexOf("kame") != -1) {
            name = replace(name, "kame", "meka");
         }

         if (name.indexOf("Masenko") != -1) {
            name = replace(name, "Masenko", "Kosenma");
         }

         if (name.indexOf("Thái Dương Hạ San") != -1) {
            name = replace(name, "Thái Dương Hạ San", "Hạ Dương");
         }

         if (name.indexOf("Solar flare") != -1) {
            name = replace(name, "Solar flare", "Solar");
         }

         if (name.indexOf("Quả cầu kênh khi") != -1) {
            name = replace(name, "Quả cầu kênh khi", "Quả cầu");
         }

         if (name.indexOf("Genki-Dama") != -1) {
            name = replace(name, "Genki-Dama", "Dama");
         }

         if (name.indexOf("Genki-Dama") != -1) {
            name = replace(name, "Genki-Dama", "Dama");
         }

         if (name.indexOf("Makankosappo") != -1) {
            name = replace(name, "Makankosappo", "Oppasoknakam");
         }

         return name;
      }
   }

   public static String replace(String var0, String var1, String var2) {
      StringBuffer var3 = new StringBuffer();
      int var4 = var0.indexOf(var1);
      int var5 = 0;

      for(int var6 = var1.length(); var4 != -1; var4 = var0.indexOf(var1, var5)) {
         var3.append(var0.substring(var5, var4)).append(var2);
         var5 = var4 + var6;
      }

      var3.append(var0.substring(var5, var0.length()));
      return var3.toString();
   }

   public static int nextInt(int var0) {
      return randomGen.nextInt(var0);
   }

   public static int nextIntRange(int var0, int var1) {
      return var0 == var1 ? var0 : var0 + randomGen.nextInt(var1 - var0);
   }

   public static int nextIntNonZero(int var0) {
      int var1;
      for(var1 = 0; var1 == 0; var1 = randomGen.nextInt() % var0) {
      }

      return var1;
   }

   public static int nextIntNegative(int var0, int var1) {
      var0 += randomGen.nextInt(var1 - var0);
      byte var2 = 2;
      if (randomGen.nextInt(var2) == 0) {
         var0 = -var0;
      }

      return var0;
   }

   public static int getDistance(int var0, int var1, int var2, int var3) {
      return sqrt((var0 - var2) * (var0 - var2) + (var1 - var3) * (var1 - var3));
   }

   public static int getMagnitude(int var0, int var1) {
      return sqrt(var0 * var0 + var1 * var1);
   }

   public static int sqrt(int var0) {
      if (var0 <= 0) {
         return 0;
      } else {
         int var1 = (var0 + 1) / 2;

         int var2;
         do {
            var2 = var1;
            var1 = var1 / 2 + var0 / (var1 * 2);
         } while(Math.abs(var2 - var1) > 1);

         return var1;
      }
   }

   public static int power(int var0, int var1) {
      var1 = 1;

      for(int var2 = 0; var2 < 2; ++var2) {
         var1 *= var0;
      }

      return var1;
   }

   public static int abs(int var0) {
      return var0 > 0 ? var0 : -var0;
   }

   public static boolean isInside(int var0, int var1, int var2, int var3, int var4, int var5) {
      return var4 >= var0 && var4 <= var0 + var2 && var5 >= var1 && var5 <= var1 + var3;
   }

   public static void b(String var0) {
   }

   public static void c(String var0) {
   }

   public static String[] splitString(String var0, String var1, int var2) {
      int var3;
      String[] var4;
      if ((var3 = var0.indexOf(var1)) >= 0) {
         var4 = splitString(var0.substring(var3 + var1.length()), var1, var2 + 1);
      } else {
         var4 = new String[var2 + 1];
         var3 = var0.length();
      }

      var4[var2] = var0.substring(0, var3);
      return var4;
   }

   /**
     * formatPotential [a]
     * Hàm định dạng số rút gọn (Dùng cho Tiềm năng/Sức mạnh)
     * Ví dụ: 155.486.918 -> 155Tr, 15.625.000.000 -> 15.6 tỉ
   */
   public static String formatPotential(long value) {
      if (value >= 1000000000L) {
        // Dùng double để lấy độ chính xác cao hơn
        double ty = (double) value / 1000000000.0;
        // Chuyển 15.625 thành "15.6"
        String s = String.valueOf(ty);
        int dot = s.indexOf(".");
        if (dot != -1 && s.length() > dot + 2) {
            return s.substring(0, dot + 2) + mResources.bf; // 15.6 tỉ
        }
        return s + mResources.bf;
    } else if (value >= 1000000L) {
        return (value / 1000000L) + mResources.bg; // 625Tr`
    }
    return String.valueOf(value);
   }

   /**
     * formatGold [b]
     * Hàm định dạng số chi tiết hơn (Dùng cho Vàng/Ngọc)
   */
   public static String formatGold(long var0) {
      String var2;
      long var4;
      String var6;
      if (var0 >= 1000000000L) {
         var2 = mResources.bf;
         var4 = var0 % 1000000000L / 10000000L;
         var0 /= 1000000000L;
         var6 = String.valueOf(var0);
         if (var4 >= 10L) {
            if (var4 % 10L == 0L) {
               var4 /= 10L;
            }

            var6 = var6 + "," + var4 + var2;
         } else if (var4 > 0L) {
            var6 = var6 + ",0" + var4 + var2;
         } else {
            var6 = var6 + var2;
         }
      } else if (var0 >= 1000000L) {
         var2 = mResources.bg;
         var4 = var0 % 1000000L / 10000L;
         var0 /= 1000000L;
         var6 = String.valueOf(var0);
         if (var4 >= 10L) {
            if (var4 % 10L == 0L) {
               var4 /= 10L;
            }

            var6 = var6 + "," + var4 + var2;
         } else if (var4 > 0L) {
            var6 = var6 + ",0" + var4 + var2;
         } else {
            var6 = var6 + var2;
         }
      } else if (var0 >= 10000L) {
         var2 = "k";
         var4 = var0 % 1000L / 10L;
         var0 /= 1000L;
         var6 = String.valueOf(var0);
         if (var4 >= 10L) {
            if (var4 % 10L == 0L) {
               var4 /= 10L;
            }

            var6 = var6 + "," + var4 + var2;
         } else if (var4 > 0L) {
            var6 = var6 + ",0" + var4 + var2;
         } else {
            var6 = var6 + var2;
         }
      } else {
         var6 = String.valueOf(var0);
      }

      return var6;
   }
}
