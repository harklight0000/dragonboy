package nro;

import main.GameCanvas;
import nro.Char;
import nro.ResLog;
import nro.mGraphics;
import nro.mSystem;
import nro.GameScreen;

public final class Joystick {
   private int centerX;
   private int centerY;
   private int knobX;
   private int knobY;
   private int lastKnobX;
   private int lastKnobY;
   private int maxRadius = 28;
   private int distance;
   private int touchX;
   private int touchY;
   private int deltaX;
   private int deltaY;
   private int distSq;
   private int angle;
   private int limitLeft;
   private int limitTop;
   public int limitRight;
   private int limitBottom;
   private boolean isDragging = false;
   public boolean isSmall;
   public boolean isMedium;
   public boolean isLarge;

   public Joystick() {
      if (GameCanvas.z < 300) {
         this.isSmall = true;
         this.isMedium = false;
         this.isLarge = false;
      }

      if (GameCanvas.z >= 300 && GameCanvas.z <= 380) {
         this.isSmall = false;
         this.isMedium = true;
         this.isLarge = false;
      }

      if (GameCanvas.z > 380) {
         this.isSmall = false;
         this.isMedium = false;
         this.isLarge = true;
      }

      if (!this.isLarge) {
         this.limitLeft = 0;
         this.limitRight = GameCanvas.B;
         this.limitTop = GameCanvas.hh >> 1;
         this.limitBottom = GameCanvas.A - 80;
      } else {
         this.limitLeft = 0;
         this.limitRight = GameCanvas.B / 4 * 3 - 20;
         this.limitTop = GameCanvas.hh >> 1;
         this.limitBottom = GameCanvas.A;
         if (mSystem.d == 2) {
            this.limitLeft = 0;
            this.limitTop = (GameCanvas.A >> 1) + 40;
            this.limitRight = GameCanvas.B / 4 * 3 - 40;
            this.limitBottom = GameCanvas.A;
         }

      }
   }

   public final void update() {
      try {
         if (GameScreen.aN != 0) {
            if (GameCanvas.k && !GameCanvas.m) {
               this.touchX = GameCanvas.q;
               this.touchY = GameCanvas.r;
               if (this.touchX >= 0 && this.touchX <= this.limitRight && this.touchY >= this.limitTop && this.touchY <= this.limitBottom) {
                  if (!this.isDragging) {
                     this.centerX = this.knobX = this.touchX;
                     this.centerY = this.knobY = this.touchY;
                  }

                  this.isDragging = true;
                  this.deltaX = GameCanvas.o - this.centerX;
                  this.deltaY = GameCanvas.p - this.centerY;
                  this.distSq = ResLog.power(this.deltaX, 2) + ResLog.power(this.deltaY, 2);
                  this.distance = ResLog.sqrt(this.distSq);
                  if (ResLog.abs(this.deltaX) > 4 || ResLog.abs(this.deltaY) > 4) {
                     this.angle = ResLog.getAngle(this.deltaX, this.deltaY);
                     if (!GameCanvas.isPointerHoldIn(this.centerX - this.maxRadius, this.centerY - this.maxRadius, 2 * this.maxRadius, 2 * this.maxRadius)) {
                        if (this.distance != 0) {
                           this.knobY = this.deltaY * this.maxRadius / this.distance;
                           this.knobX = this.deltaX * this.maxRadius / this.distance;
                           this.knobX += this.centerX;
                           this.knobY += this.centerY;
                           if (!ResLog.isInside(this.centerX - this.maxRadius, this.centerY - this.maxRadius, 2 * this.maxRadius, 2 * this.maxRadius, this.knobX, this.knobY)) {
                              this.knobX = this.lastKnobX;
                              this.knobY = this.lastKnobY;
                           } else {
                              this.lastKnobX = this.knobX;
                              this.lastKnobY = this.knobY;
                           }
                        } else {
                           this.knobX = this.lastKnobX;
                           this.knobY = this.lastKnobY;
                        }
                     } else {
                        this.knobX = GameCanvas.o;
                        this.knobY = GameCanvas.p;
                     }

                     GameCanvas.f();
                     boolean var1 = true;
                     boolean var10000;
                     if (GameScreen.aN == 0) {
                        var10000 = false;
                     } else if (Char.myCharz().statusMe == 3) {
                        var10000 = true;
                     } else {
                        int var5 = 2;

                        while(true) {
                           if (var5 <= 0) {
                              var10000 = true;
                              break;
                           }

                           int var2 = GameCanvas.u[var5].a - GameCanvas.u[var5 - 1].a;
                           int var3 = GameCanvas.u[var5].b - GameCanvas.u[var5 - 1].b;
                           if (ResLog.abs(var2) > 2 && ResLog.abs(var3) > 2) {
                              var10000 = false;
                              break;
                           }

                           --var5;
                        }
                     }

                     if (!var10000) {
                        GameCanvas.f();
                     } else if ((this.angle > 360 || this.angle < 340) && (this.angle < 0 || this.angle > 20)) {
                        if (this.angle > 40 && this.angle < 70) {
                           GameCanvas.j[6] = true;
                           GameCanvas.keyPressed[6] = true;
                        } else if (this.angle >= 70 && this.angle <= 110) {
                           GameCanvas.j[8] = true;
                           GameCanvas.keyPressed[8] = true;
                        } else if (this.angle > 110 && this.angle < 120) {
                           GameCanvas.j[4] = true;
                           GameCanvas.keyPressed[4] = true;
                        } else if (this.angle >= 120 && this.angle <= 200) {
                           GameCanvas.j[4] = true;
                           GameCanvas.keyPressed[4] = true;
                        } else if (this.angle > 200 && this.angle < 250) {
                           GameCanvas.j[2] = true;
                           GameCanvas.keyPressed[2] = true;
                           GameCanvas.j[4] = true;
                           GameCanvas.keyPressed[4] = true;
                        } else if (this.angle >= 250 && this.angle <= 290) {
                           GameCanvas.j[2] = true;
                           GameCanvas.keyPressed[2] = true;
                        } else if (this.angle > 290 && this.angle < 340) {
                           GameCanvas.j[2] = true;
                           GameCanvas.keyPressed[2] = true;
                           GameCanvas.j[6] = true;
                           GameCanvas.keyPressed[6] = true;
                        }
                     } else {
                        GameCanvas.j[6] = true;
                        GameCanvas.keyPressed[6] = true;
                     }
                  }
               }
            } else {
               this.knobX = this.centerX = 45;
               if (!this.isLarge) {
                  this.knobY = this.centerY = GameCanvas.A - 90;
               } else {
                  this.knobY = this.centerY = GameCanvas.A - 45;
               }

               this.isDragging = false;
               GameCanvas.f();
            }
         }
      } catch (Exception var4) {
      }
   }

   public final void paint(mGraphics var1) {
      if (GameScreen.aN != 0) {
         var1.drawImage(GameScreen.az, this.centerX, this.centerY, 3);
         var1.drawImage(GameScreen.aA, this.knobX, this.knobY, 3);
      }
   }

   public final boolean isActive() {
      return GameScreen.aN == 0 ? false : this.isDragging;
   }

   public final boolean checkTouchRegion() {
      try {
         if (GameScreen.aN == 0) {
            return false;
         } else {
            boolean var1;
            if ((GameCanvas.o < 0 || GameCanvas.o > this.limitRight || GameCanvas.p < this.limitTop || GameCanvas.p > this.limitBottom) && GameCanvas.o < GameCanvas.z - 50) {
               var1 = false;
            } else {
               var1 = true;
            }

            return var1;
         }
      } catch (Exception var2) {
         return false;
      }
   }
}
