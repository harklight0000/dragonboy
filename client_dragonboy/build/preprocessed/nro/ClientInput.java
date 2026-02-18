package nro;

public final class ClientInput extends mScreen implements IActionListener {

    private static ClientInput b;
    public TextField[] tf;
    private int c;
    private int d;
    private int e;
    private int f;
    private String[] g;
    private String h;
    private ChatTextField i;
    private int j;
    private int k;

    public static ClientInput gI() {
        if (b == null) {
            b = new ClientInput();
        }

        return b;
    }

    public final void switchToMe() {
        if (mSystem.d == 5 && this.tf.length == 1 || mSystem.d == 3 && this.tf.length == 1) {
            this.i = new ChatTextField();
            this.i.prefix = this.h;
            this.i.textField.name = this.tf[0].f;
            this.i.title = "";
            this.i.isShow = true;
            this.i.textField.setIputType(0);
            if (main.GameCanvas.isTouch) {
                return;
            }
        } else {
            this.j = 0;
            super.switchToMe();
        }

    }

    public final void a(int var1, String var2) {
        this.k = var1;
        ClientInput var3 = this;
        this.h = var2;
        this.e = main.GameCanvas.z - 20;
        if (this.e > 320) {
            this.e = 320;
        }

        ResLog.c("title= " + var2);
        this.g = mFont.tahoma_7b_dark.getWidth(var2, this.e - 20);
        this.c = (main.GameCanvas.z - this.e) / 2;
        this.tf = new TextField[this.k];
        this.f = this.tf.length * 35 + (this.g.length - 1) * 20 + 40;
        this.d = main.GameCanvas.A - this.f - 40 - (this.g.length - 1) * 20;

        for (int var4 = 0; var4 < var3.tf.length; ++var4) {
            var3.tf[var4] = new TextField();
            var3.tf[var4].name = "";
            var3.tf[var4].a = var3.c + 10;
            var3.tf[var4].b = var3.d + 35 + (var3.g.length - 1) * 20 + var4 * 35;
            var3.tf[var4].width = var3.e - 20;
            var3.tf[var4].height = mScreen.ITEM_HEIGHT + 2;
            if (main.GameCanvas.isTouch) {
                var3.tf[0].isFocus = false;
            } else {
                var3.tf[0].isFocus = true;
            }

            if (!main.GameCanvas.isTouch) {
                var3.cb = var3.tf[0].j;
            }
        }

        var3.left = new CommandLine(mResources.CLOSE, var3, 1, (Object) null);
        var3.center = new CommandLine(mResources.OK, var3, 2, (Object) null);
        if (main.GameCanvas.isTouch) {
            var3.center.x = main.GameCanvas.z / 2 + 18;
            var3.left.x = main.GameCanvas.z / 2 - 85;
            var3.center.y = var3.left.y = var3.d + var3.f + 5;
        }

        this.switchToMe();
    }

    public final void paint(mGraphics var1) {
        GameScreen.gI().paint(var1);
        PopUp.paintPopUp(var1, this.c, this.d, this.e, this.f, -1, true);

        int var2;
        for (var2 = 0; var2 < this.g.length; ++var2) {
            mFont.g.drawStringBd(var1, this.g[var2], main.GameCanvas.z / 2, this.d + 15 + var2 * 20, 2);
        }

        for (var2 = 0; var2 < this.tf.length; ++var2) {
            this.tf[var2].paint(var1);
        }

        super.paint(var1);
    }

    public final void update() {
        GameScreen.gI().update();

        for (int var1 = 0; var1 < this.tf.length; ++var1) {
            this.tf[var1].update();
        }

    }

    public final void keyPress(int var1) {
        for (int var2 = 0; var2 < this.tf.length; ++var2) {
            if (this.tf[var2].isFocus) {
                this.tf[var2].keyPress(var1);
                break;
            }
        }

        super.keyPress(var1);
    }

    public final void updateKey() {
        if (main.GameCanvas.keyPressed[2]) {
            --this.j;
            if (this.j < 0) {
                this.j = this.tf.length - 1;
            }
        } else if (main.GameCanvas.keyPressed[8]) {
            ++this.j;
            if (this.j > this.tf.length - 1) {
                this.j = 0;
            }
        }

        if (main.GameCanvas.keyPressed[2] || main.GameCanvas.keyPressed[8]) {
            main.GameCanvas.clearKeyPressed();

            for (int var1 = 0; var1 < this.tf.length; ++var1) {
                if (this.j == var1) {
                    this.tf[var1].isFocus = true;
                    if (!main.GameCanvas.isTouch) {
                        super.cb = this.tf[var1].j;
                    }
                } else {
                    this.tf[var1].isFocus = false;
                }

                if (main.GameCanvas.m && main.GameCanvas.isPointerHoldIn(this.tf[var1].a, this.tf[var1].b, this.tf[var1].width, this.tf[var1].height)) {
                    this.j = var1;
                    break;
                }
            }
        }

        super.updateKey();
        main.GameCanvas.clearKeyPressed();
    }

    public final void perform(int var1, Object var2) {
        if (var1 == 1) {
            GameScreen.c.switchToMe();
            b = null;
        }

        if (var1 == 2) {
            if (mSystem.d == 5 && this.tf.length == 1 || mSystem.d == 3 && this.tf.length == 1) {
                if (this.i.textField.getText() != null && !this.i.textField.getText().equals("")) {
                    this.tf[0].setText(this.i.textField.getText());
                    Service.gI().a(this.tf);
                    GameScreen.c.switchToMe();
                    return;
                }

                main.GameCanvas.startOKDlg(mResources.gg);
                return;
            }

            var1 = 0;

            while (true) {
                if (var1 >= this.tf.length) {
                    Service.gI().a(this.tf);
                    GameScreen.c.switchToMe();
                    break;
                }

                if (this.tf[var1].getText() == null || this.tf[var1].getText().equals("")) {
                    main.GameCanvas.startOKDlg(mResources.gg);
                    return;
                }

                ++var1;
            }
        }

    }
}
