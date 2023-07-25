/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import star.common.Simulation;
import star.common.StarMacro;
import star.vis.Scene;
/**
 *
 * @author cd8unu
 */
public class HardcopyTimer extends StarMacro {

    @Override
    public void execute() {
        Simulation sim = getActiveSimulation();
        Scene s = sim.getSceneManager().getScene("01_Geom_Bottom_View");
        String path = sim.getSessionDir() + File.separator + s.getPresentationName() + ".png";
        Instant start = Instant.now();
        s.printAndWait(path, 1, 1920, 1080);
        Instant end = Instant.now();
        Duration dur = Duration.between(start, end);
        sim.println(dur.toSeconds() + " Seconds");        
    }
    
}
