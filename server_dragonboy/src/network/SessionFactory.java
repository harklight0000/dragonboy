package network;

import network.inetwork.ISession;

import java.net.Socket;

public class SessionFactory {
    private static SessionFactory instance;

    public static SessionFactory gI() {
        if (instance == null) {
            instance = new SessionFactory();
        }
        return instance;
    }

    public ISession cloneSession(Class clazz, Socket socket) throws Exception {
        return (ISession) clazz.getConstructor(Socket.class).newInstance(socket);
    }
}

