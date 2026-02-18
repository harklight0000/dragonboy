package nro;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import main.GameMidlet;

public final class TextField implements IActionListener {

    public int a;
    public int b;
    public int width;
    public int height;
    public boolean isFocus;
    private boolean l = false;
    private static int m = 2;
    private static final int[] n = new int[]{18, 14, 11, 9, 6, 4, 2};
    private static int o = 0;
    private static String[] p = new String[]{" 0", ".,@?!_1\"/$-():*+<=>;%&~#%^&*{}[];'/1", "abc2áàảãạâấầẩẫậăắằẳẵặ2", "def3đéèẻẽẹêếềểễệ3", "ghi4íìỉĩị4", "jkl5", "mno6óòỏõọôốồổỗộơớờởỡợ6", "pqrs7", "tuv8úùủũụưứừửữự8", "wxyz9ýỳỷỹỵ9", "*", "#"};
    private static String[] q = new String[]{"0", "1", "abc2", "def3", "ghi4", "jkl5", "mno6", "pqrs7", "tuv8", "wxyz9", "0", "0"};
    public String f = "";
    private String content = "";
    private String passwordMask = "";
    private String displayText = "";
    private int caretPos = 0;
    private int v = 0;
    private int maxChars = 500;
    private int x = 0;
    private int y = -1984;
    private int keyTimer = 0;
    private int charIndex = 0;
    private int B = 10;
    private int C = 0;
    public static boolean isQwerty;
    private static int D;
    private int caseType = 0;
    private static int F;
    public boolean h;
    public String name = "";
    public CommandLine j;
    private static Image G;
    private boolean isFullScreen = true;
    public boolean isPaintCarret = true;
    private static Image I;

    static {
        String[] var10000 = new String[]{"abc", "Abc", "ABC", "123"};
        F = 11;
        G = mSystem.load("/mainImage/myTexture2der.png");
        I = mSystem.load("/mainImage/myTexture2dtf.png");
        int[][] var0 = new int[][]{{32, 48}, {49, 69}, {50, 84}, {51, 85}, {52, 68}, {53, 71}, {54, 74}, {55, 67}, {56, 66}, {57, 77}, {42, 128}, {35, 137}, {33, 113}, {63, 97}, {64, 121, 122}, {46, 111}, {44, 108}};
    }

    private static boolean isPcEmulator() {
        String platform = System.getProperty("microedition.platform");
        if (platform == null) {
            return false;
        }

        String lower = platform.toLowerCase();
        return lower.indexOf("microemulator") >= 0 || lower.indexOf("kemulator") >= 0;
    }

    public final void doClick() {
        // Trên PC emulator có touch, dùng input noi bo thay vi TextBox he thong.
        if (main.GameCanvas.isTouch && isPcEmulator()) {
            this.isFocus = true;
            return;
        }

        // 2. Nếu không phải PC (chạy trên điện thoại thật), tiếp tục logic hiện TextBox hệ thống
        TextBox var1;
        (var1 = new TextBox(this.name, "", this.maxChars, 0)).addCommand(new Command(mResources.OK, 4, 0));
        var1.addCommand(new Command(mResources.bu, 3, 0));
        var1.setCommandListener(new nr_m(this, var1));

        try {
            if (this.C == 2) {
                var1.setConstraints(65536); // Password
            } else if (this.C == 1) {
                var1.setConstraints(2);     // Numeric (Số)
            } else {
                var1.setConstraints(0);     // Any (Tất cả ký tự)
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        var1.setString(this.content);
        var1.setMaxSize(this.maxChars);

        Display.getDisplay(GameMidlet.instance).setCurrent(var1);
    }

    public TextField() {
        this.content = "";
        isQwerty = true;
        o = mFont.t.getWidth() + 1;
        this.j = new CommandLine(mResources.DELETE, this, 1000, (Object) null);
        D = 0;
        if (Rms.loadRMSInt("qwerty") == 1) {
            isQwerty = true;
        }

    }

    public final void backspace() {
        if (this.caretPos > 0 && this.content.length() > 0) {
            this.content = this.content.substring(0, this.caretPos - 1) + this.content.substring(this.caretPos, this.content.length());
            --this.caretPos;
            this.updateScroll();
            this.updatePasswordText();
        }

    }

    private void updateScroll() {
        if (this.C == 2) {
            this.displayText = this.passwordMask;
        } else {
            this.displayText = this.content;
        }

        if (this.x < 0 && mFont.t.getWidth(this.displayText) + this.x < this.width - 6 - 13) {
            this.x = this.width - 10 - mFont.t.getWidth(this.displayText);
        }

        if (this.x + mFont.t.getWidth(this.displayText.substring(0, this.caretPos)) <= 0) {
            this.x = -mFont.t.getWidth(this.displayText.substring(0, this.caretPos));
            this.x += 40;
        } else if (this.x + mFont.t.getWidth(this.displayText.substring(0, this.caretPos)) >= this.width - 22) {
            this.x = this.width - 10 - mFont.t.getWidth(this.displayText.substring(0, this.caretPos)) - 12;
        }

        if (this.x > 0) {
            this.x = 0;
        }

    }

    private void addChar(int var1) {
        if (var1 == 432) {
            var1 = 119;
        }

        if (this.C != 2 && this.C != 3 || var1 >= 48 && var1 <= 57 || var1 >= 65 && var1 <= 90 || var1 >= 97 && var1 <= 122) {
            if (this.content.length() < this.maxChars) {
                String var2 = this.content.substring(0, this.caretPos) + (char) var1;
                if (this.caretPos < this.content.length()) {
                    var2 = var2 + this.content.substring(this.caretPos, this.content.length());
                }

                this.content = var2;
                ++this.caretPos;
                this.updatePasswordText();
                this.updateScroll();
            }

        }
    }

    public final boolean keyPress(int var1) {
        if (var1 != 8 && var1 != -8 && var1 != 204) {
//            if (var1 >= 65 && var1 <= 122 && !isQwerty) {
//                isQwerty = true;
//                D = 0;
//                Rms.saveRMSInt("qwerty", 1);
//            }

            if (isQwerty) {
                if (var1 == 45) {
                    if (var1 == this.y && this.keyTimer < n[m]) {
                        this.content = this.content.substring(0, this.caretPos - 1) + '_';
                        this.displayText = this.content;
                        this.updatePasswordText();
                        this.updateScroll();
                        this.y = -1984;
                        return false;
                    }

                    this.y = 45;
                }

                if (var1 >= 32) {
                    this.addChar(var1);
                    return false;
                }
            }

            if (var1 == F) {
                ++this.caseType;
                if (this.caseType > 3) {
                    this.caseType = 0;
                }

                this.keyTimer = 1;
                this.y = var1;
                return false;
            } else {
                if (var1 == 42) {
                    var1 = 58;
                }

                if (var1 == 35) {
                    var1 = 59;
                }

                if (var1 >= 48 && var1 <= 59) {
                    if (this.C != 0 && this.C != 2 && this.C != 3) {
                        if (this.C == 1) {
                            this.addChar(var1);
                            this.keyTimer = 1;
                        }
                    } else {
                        String[] var3;
                        if (this.C != 2 && this.C != 3) {
                            var3 = p;
                        } else {
                            var3 = q;
                        }

                        char var4;
                        String var5;
                        if (var1 == this.y) {
                            this.charIndex = (this.charIndex + 1) % var3[var1 - 48].length();
                            var4 = var3[var1 - 48].charAt(this.charIndex);
                            if (this.caseType == 0) {
                                var4 = Character.toLowerCase(var4);
                            } else if (this.caseType == 1) {
                                var4 = Character.toUpperCase(var4);
                            } else if (this.caseType == 2) {
                                var4 = Character.toUpperCase(var4);
                            } else {
                                var4 = var3[var1 - 48].charAt(var3[var1 - 48].length() - 1);
                            }

                            var5 = this.content.substring(0, this.caretPos - 1) + var4;
                            if (this.caretPos < this.content.length()) {
                                var5 = var5 + this.content.substring(this.caretPos, this.content.length());
                            }

                            this.content = var5;
                            this.keyTimer = n[m];
                            this.updatePasswordText();
                        } else if (this.content.length() < this.maxChars) {
                            if (this.caseType == 1 && this.y != -1984) {
                                this.caseType = 0;
                            }

                            this.charIndex = 0;
                            var4 = var3[var1 - 48].charAt(this.charIndex);
                            if (this.caseType == 0) {
                                var4 = Character.toLowerCase(var4);
                            } else if (this.caseType == 1) {
                                var4 = Character.toUpperCase(var4);
                            } else if (this.caseType == 2) {
                                var4 = Character.toUpperCase(var4);
                            } else {
                                var4 = var3[var1 - 48].charAt(var3[var1 - 48].length() - 1);
                            }

                            var5 = this.content.substring(0, this.caretPos) + var4;
                            if (this.caretPos < this.content.length()) {
                                var5 = var5 + this.content.substring(this.caretPos, this.content.length());
                            }

                            this.content = var5;
                            this.keyTimer = n[m];
                            ++this.caretPos;
                            this.updatePasswordText();
                            this.updateScroll();
                        }

                        this.y = var1;
                    }
                } else {
                    this.charIndex = 0;
                    this.y = -1984;
                    if (var1 == 14) {
                        if (this.caretPos > 0) {
                            --this.caretPos;
                            this.updateScroll();
                            this.B = 10;
                            return false;
                        }
                    } else if (var1 == 15) {
                        if (this.caretPos < this.content.length()) {
                            ++this.caretPos;
                            this.updateScroll();
                            this.B = 10;
                            return false;
                        }
                    } else {
                        if (var1 == 19) {
                            this.backspace();
                            return false;
                        }

                        this.y = var1;
                    }
                }

                return true;
            }
        } else {
            this.backspace();
            return true;
        }
    }

    public final void paint(mGraphics var1) {
        if (this.isFullScreen) {
            var1.e(0, 0, main.GameCanvas.z, main.GameCanvas.A);
        }

        boolean var2 = this.isFocus;
        if (this.C == 2) {
            this.displayText = this.passwordMask;
        } else {
            this.displayText = this.content;
        }

        int var10002 = this.a;
        int var10003 = this.b - 1;
        int var10004 = this.width;
        int var10005 = this.height;
        int var10006 = 6 + this.x + this.a;
        int var10007 = this.b + (this.height - mFont.t.getWidth()) / 2;
        String var12 = this.name;
        String var11 = this.displayText;
        int var10 = var10007;
        int var9 = var10006;
        int var8 = var10005;
        int var7 = var10004;
        int var6 = var10003;
        int var5 = var10002;
        mGraphics var3 = var1;
        var1.setColor(0);
        int var13;
        if (var2) {
            var1.setColor(I, 0, 81, 29, 27, 0, var5, var6, 0);
            var1.setColor(I, 0, 135, 29, 27, 0, var5 + var7 - 29, var6, 0);
            var1.setColor(I, 0, 108, 29, 27, 0, var5 + var7 - 58, var6, 0);

            for (var13 = 0; var13 < (var7 - 58) / 29; ++var13) {
                var3.setColor(I, 0, 108, 29, 27, 0, var5 + 29 + var13 * 29, var6, 0);
            }
        } else {
            var1.setColor(I, 0, 0, 29, 27, 0, var5, var6, 0);
            var1.setColor(I, 0, 54, 29, 27, 0, var5 + var7 - 29, var6, 0);
            var1.setColor(I, 0, 27, 29, 27, 0, var5 + var7 - 58, var6, 0);

            for (var13 = 0; var13 < (var7 - 58) / 29; ++var13) {
                var3.setColor(I, 0, 27, 29, 27, 0, var5 + 29 + var13 * 29, var6, 0);
            }
        }

        var3.e(var5 + 3, var6 + 1, var7 - 4, var8);
        if (var11 != null && !var11.equals("")) {
            mFont.t.drawStringBd(var3, var11, var9, var10, 0);
        } else if (var12 != null) {
            if (var2) {
                mFont.i.drawStringBd(var3, var12, var9, var10 + 2, 0);
            } else {
                mFont.j.drawStringBd(var3, var12, var9, var10 + 2, 0);
            }
        }

        var1.setColor(0);
        if (this.isFocus && this.isPaintCarret) {
            if (this.keyTimer == 0 && (this.B > 0 || this.v / 5 % 2 == 0)) {
                var1.setColor(7999781);
                var1.fillRect(7 + this.x + this.a + mFont.t.getWidth(this.displayText.substring(0, this.caretPos)) - 1, this.b + (this.height - o) / 2 + 1, 1, o);
            }

            main.GameCanvas.a(var1);
            if (this.content != null && this.content.length() > 0 && main.GameCanvas.isTouch && var2) {
                var1.drawImage(G, this.a + this.width - 13, this.b + this.height / 2 + 1, 3);
            }
        }

    }

    private void updatePasswordText() {
        if (this.C == 2) {
            this.passwordMask = "";

            for (int var1 = 0; var1 < this.content.length(); ++var1) {
                this.passwordMask = this.passwordMask + "*";
            }

            if (this.keyTimer > 0 && this.caretPos > 0) {
                this.passwordMask = this.passwordMask.substring(0, this.caretPos - 1) + this.content.charAt(this.caretPos - 1) + this.passwordMask.substring(this.caretPos, this.passwordMask.length());
            }
        }

    }

    public final void update() {
        this.isPaintCarret = true;
        ++this.v;
        if (this.keyTimer > 0) {
            --this.keyTimer;
            if (this.keyTimer == 0) {
                this.charIndex = 0;
                if (this.caseType == 1 && this.y != F) {
                    this.caseType = 0;
                }

                this.y = -1984;
                this.updatePasswordText();
            }
        }

        if (this.B > 0) {
            --this.B;
        }

        if (main.GameCanvas.m) {
            if (main.GameCanvas.isPointerHoldIn(this.a + this.width - 20, this.b, 40, this.height)) {
                this.content = "";
                this.caretPos = 0;
                this.updateScroll();
                this.updatePasswordText();
                this.isFocus = true;
                return;
            }

            if (main.GameCanvas.isPointerHoldIn(this.a, this.b, this.width, this.height)) {
                this.doClick();
                return;
            }

            this.isFocus = false;
        }

    }

    public final String getText() {
        return this.content;
    }

    public final void setText(String var1) {
        if (var1 != null) {
            this.y = -1984;
            this.keyTimer = 0;
            this.charIndex = 0;
            this.content = var1;
            this.displayText = var1;
            this.updatePasswordText();
            this.caretPos = var1.length();
            this.updateScroll();
        }
    }

    public final void setMaxLength(int var1) {
        this.maxChars = var1;
    }

    public final void setIputType(int var1) {
        this.C = var1;
        short var2 = 500;
        this.maxChars = var2;
    }

    public final void perform(int var1, Object var2) {
        switch (var1) {
            case 1000:
                this.backspace();
            default:
        }
    }
}
