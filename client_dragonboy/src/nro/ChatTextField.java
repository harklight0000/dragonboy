package nro;

public final class ChatTextField implements IActionListener {
   private static ChatTextField i;
   public TextField textField;
   public boolean isShow = false;
   public IChatable c;
   private long j = 0L;
   public CommandLine cmdOk;
   public CommandLine cmdClose;
   public CommandLine cmdMenu = null;
   private int x;
   private int y;
   private int width;
   private int height;
   public String title;
   public String prefix;

   public final void initLayout() {
      this.cmdOk = new CommandLine(mResources.OK, this, 8000, (Object)null, 1, main.GameCanvas.A - mScreen.ce + 1);
      this.cmdClose = new CommandLine(mResources.DELETE, this, 8001, (Object)null, main.GameCanvas.z - 70, main.GameCanvas.A - mScreen.ce + 1);
      this.cmdMenu = null;
      this.width = this.textField.width + 28;
      this.height = this.textField.height + 26;
      this.x = main.GameCanvas.z / 2 - this.width / 2;
      this.y = this.textField.b - 18;
      if (this.width > 320) {
         this.width = 320;
      }

      this.cmdOk.x = this.x;
      this.cmdClose.x = this.x + this.width - 68;
      if (main.GameCanvas.isTouch) {
         TextField var10000 = this.textField;
         var10000.b -= 5;
         this.y -= 20;
         this.height += 30;
         this.cmdOk.x = main.GameCanvas.z / 2 - 68 - 5;
         this.cmdClose.x = main.GameCanvas.z / 2 + 5;
         this.cmdOk.y = main.GameCanvas.A - 30;
         this.cmdClose.y = main.GameCanvas.A - 30;
      }

   }

   public final void keyPressed(int var1) {
      if (this.isShow) {
         this.textField.keyPress(var1);
      }

      if (this.textField.getText().equals("")) {
         this.cmdClose.caption = mResources.CLOSE;
      } else {
         this.cmdClose.caption = mResources.DELETE;
      }
   }

   public static ChatTextField gI() {
      return i == null ? (i = new ChatTextField()) : i;
   }

   public ChatTextField() {
      this.prefix = mResources.bp + " ";
      this.textField = new TextField();
      this.textField.name = mResources.bp;
      this.textField.width = main.GameCanvas.z - 32;
      if (this.textField.width > 250) {
         this.textField.width = 250;
      }

      this.textField.height = mScreen.ITEM_HEIGHT + 2;
      this.textField.a = main.GameCanvas.z / 2 - this.textField.width / 2;
      this.textField.isFocus = true;
      this.textField.setMaxLength(80);
   }

   public final void show(int var1, IChatable var2, String var3) {
      this.cmdClose.caption = mResources.CLOSE;
      this.title = var3;
      this.textField.keyPress(var1);
      if (!this.textField.getText().equals("") && main.GameCanvas.currentDialog == null) {
         this.c = var2;
         this.isShow = true;
      }

   }

   public final void a(String var1) {
    this.cmdClose.caption = mResources.CLOSE;
    this.title = var1;
    if (main.GameCanvas.isTouch) {
        // this.initTrigonometry.initTrigonometry(); // Comment dòng này để không hiện bảng Chat trắng trên PC
    }
    if (main.GameCanvas.currentDialog == null) {
        this.isShow = true;
    }

   }

   public final void c() {
      if (this.isShow) {
         if (mSystem.d != 5 && mSystem.d != 3) {
            this.textField.update();
         }

         if (this.textField.h) {
            this.textField.h = false;
            this.c.onChatFromMe(this.textField.getText(), this.title);
            this.textField.setText("");
            this.cmdClose.caption = mResources.CLOSE;
         }

      }
   }

   public final void a(mGraphics var1) {
      if (this.isShow) {
         if (mSystem.d != 5 && mSystem.d != 3) {
            PopUp.paintPopUp(var1, this.x, this.y, this.width, this.height, -1, true);
            mFont.g.drawStringBd(var1, this.prefix + this.title, this.textField.a, this.textField.b - (main.GameCanvas.isTouch ? 17 : 12), 0);
            Paint.paintCmdBar(var1, this.cmdOk, this.cmdMenu, this.cmdClose);
            this.textField.paint(var1);
         }
      }
   }

   public final void perform(int var1, Object var2) {
      switch(var1) {
      case 8000:
         ResLog.c("perform chat 1");
         if (this.c != null) {
            long var3;
            if ((var3 = System.currentTimeMillis()) - this.j < 1000L) {
               return;
            }

            this.j = var3;
            this.c.onChatFromMe(this.textField.getText(), this.title);
            this.textField.setText("");
            this.cmdClose.caption = mResources.CLOSE;
            return;
         }
         break;
      case 8001:
         ResLog.c("perform chat 2");
         if (this.textField.getText().equals("")) {
            this.isShow = false;
            this.c.onCancelChat();
         }

         this.textField.backspace();
      case 8002:
      }

   }
}
