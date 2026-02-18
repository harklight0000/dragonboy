package commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.util.logging.Logger;
import services.Service;

@Command(name = "say", description = "Broadcast a server message.")
public class SayCommand implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SayCommand.class.getName());

    @Parameters(description = "The message to broadcast.", arity = "1..*")
    private String[] messageParts;

    @Override
    public void run() {
        String msg = String.join(" ", messageParts);
        // TODO: Client.gI().broadcastServerMessage(msg);
        Service.gI().sendThongBaoAllPlayer(msg);
    }
}