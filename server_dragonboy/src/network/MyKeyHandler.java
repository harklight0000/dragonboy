package network;

/*
 * @Author: NgocRongWhis
 * @Description: Ngọc Rồng Whis - Máy Chủ Chuẩn Teamobi 2024
 * @Group Zalo: https://zalo.me/g/qabzvn331
 */


import network.inetwork.ISession;

public class MyKeyHandler extends KeyHandler {

    @Override
    public void sendKey(ISession session) {
        super.sendKey(session);
        SessionService.sendDataImageVersion((MySession) session);
        SessionService.sendVersionRes((MySession) session);
    }

}
