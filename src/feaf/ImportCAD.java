/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feaf;

import star.common.Simulation;
import star.common.StarMacro;
/**
 *
 * @author cd8unu
 */
public class ImportCAD extends StarMacro {

    Simulation _sim;
    
    @Override
    public void execute() {
        _sim = getActiveSimulation();
    }
    
    
}
