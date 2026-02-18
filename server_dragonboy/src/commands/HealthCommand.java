/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package commands;

import java.util.logging.Level;
import picocli.CommandLine.Command;
import java.util.logging.Logger;
import network.SessionManager;
import picocli.CommandLine;
import server.Client;
import server.DragonBoy;
import services.Service;
import utils.SystemMetrics;
/**
 *
 * @author er3
 */
@Command(name = "health", description = "Check health.")
public class HealthCommand implements Runnable {
   private static final Logger LOGGER = Logger.getLogger(SayCommand.class.getName());

    
    @Override
    public void run() {
            LOGGER.log(Level.INFO, "Time start: {0}. Clients: {1} player.. Sessions: {2}. Threads: {3} {4}", new Object[]{DragonBoy.timeStart, Client.gI().getPlayers().size(), SessionManager.gI().getNumSession(), Thread.activeCount(), SystemMetrics.ToString()});
        
    }
}
