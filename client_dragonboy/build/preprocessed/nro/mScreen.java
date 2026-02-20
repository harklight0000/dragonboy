package nro;

public class mScreen {

    public CommandLine left;
    public CommandLine center;
    public CommandLine right;
    public static int ITEM_HEIGHT;
    public static int cd = 68;
    public static int ce = 26;
    public static int cf = -1;

    public void switchToMe() {
        main.GameCanvas.clearKeyPressed();
        main.GameCanvas.f();
        main.GameCanvas.currentScreen = this;
        main.GameCanvas.h.setFullScreenMode(true);
    }

    public void keyPress(int var1) {
    }

    public void update() {
    }

    public void updateKey() {
        if (main.GameCanvas.keyPressed[5] || a(main.GameCanvas.currentScreen.center)) {
            main.GameCanvas.keyPressed[5] = false;
            cf = -1;
            main.GameCanvas.m = false;
            if (this.center != null) {
                this.center.performAction();
            }
        }

        if (main.GameCanvas.keyPressed[12] || a(main.GameCanvas.currentScreen.left)) {
            main.GameCanvas.keyPressed[12] = false;
            cf = -1;
            main.GameCanvas.m = false;
            if (ChatTextField.gI().isShow) {
                if (ChatTextField.gI().cmdOk != null) {
                    ChatTextField.gI().cmdOk.performAction();
                }
            } else if (this.left != null) {
                this.left.performAction();
            }
        }

        if (main.GameCanvas.keyPressed[13] || a(main.GameCanvas.currentScreen.right)) {
            main.GameCanvas.keyPressed[13] = false;
            cf = -1;
            main.GameCanvas.m = false;
            if (ChatTextField.gI().isShow) {
                if (ChatTextField.gI().cmdClose != null) {
                    ChatTextField.gI().cmdClose.performAction();
                    return;
                }
            } else if (this.right != null) {
                this.right.performAction();
            }
        }

    }

    public static boolean a(CommandLine var0) {
        if (var0 == null) {
            return false;
        } else if (var0.x >= 0 && var0.y != 0) {
            return var0.isPointerPressInside();
        } else {
            if (main.GameCanvas.currentDialog != null) {
                if (main.GameCanvas.currentDialog.c != null && main.GameCanvas.isPointerHoldIn(main.GameCanvas.z - cd >> 1, main.GameCanvas.A - ce - 5, cd, ce + 10)) {
                    cf = 1;
                    if (var0 == main.GameCanvas.currentDialog.c && main.GameCanvas.l && main.GameCanvas.m) {
                        return true;
                    }
                }

                if (main.GameCanvas.currentDialog.b != null && main.GameCanvas.isPointerHoldIn(0, main.GameCanvas.A - ce - 5, cd, ce + 10)) {
                    cf = 0;
                    if (var0 == main.GameCanvas.currentDialog.b && main.GameCanvas.l && main.GameCanvas.m) {
                        return true;
                    }
                }

                if (main.GameCanvas.currentDialog.d != null && main.GameCanvas.isPointerHoldIn(main.GameCanvas.z - cd, main.GameCanvas.A - ce - 5, cd, ce + 10)) {
                    cf = 2;
                    if ((var0 == main.GameCanvas.currentDialog.d || var0 == ChatTextField.gI().cmdClose) && main.GameCanvas.l && main.GameCanvas.m) {
                        return true;
                    }
                }
            } else {
                if (var0 == main.GameCanvas.currentScreen.left && main.GameCanvas.isPointerHoldIn(0, main.GameCanvas.A - ce - 5, cd, ce + 10)) {
                    cf = 0;
                    if (main.GameCanvas.l && main.GameCanvas.m) {
                        return true;
                    }
                }

                if (var0 == main.GameCanvas.currentScreen.right && main.GameCanvas.isPointerHoldIn(main.GameCanvas.z - cd, main.GameCanvas.A - ce - 5, cd, ce + 10)) {
                    cf = 2;
                    if (main.GameCanvas.l && main.GameCanvas.m) {
                        return true;
                    }
                }

                if ((var0 == main.GameCanvas.currentScreen.center || ChatPopup.currChatPopup != null) && main.GameCanvas.isPointerHoldIn(main.GameCanvas.z - cd >> 1, main.GameCanvas.A - ce - 5, cd, ce + 10)) {
                    cf = 1;
                    if (main.GameCanvas.l && main.GameCanvas.m) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static boolean isPcEmulator() {
        String platform = System.getProperty("microedition.platform");
        if (platform == null) {
            return false;
        }

        String lower = platform.toLowerCase();
        return lower.indexOf("microemulator") >= 0 || lower.indexOf("kemulator") >= 0;
    }

    public void paint(mGraphics var1) {
        var1.setColor(-var1.setColor(), -var1.b());
        var1.e(0, 0, main.GameCanvas.z, main.GameCanvas.A + 1);
        if (!ChatTextField.gI().isShow && main.GameCanvas.currentDialog == null && !main.GameCanvas.menu.a) {
            Paint.paintCmdBar(var1, this.left, this.center, this.right);
        }

    }
}
