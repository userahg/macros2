import java.io.BufferedWriter;
import star.common.Simulation;
import star.common.StarMacro;
import javax.json.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.json.stream.JsonGenerator;
import star.common.GlobalParameterBase;
import star.common.GlobalParameterManager;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cd8unu
 */
public class ExportSimToJSON extends StarMacro{
    
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        export();
    }
    
    private void export() {
        JsonArrayBuilder simulationParameterBuilder = Json.createArrayBuilder();
        
        int count = 0;
        for (GlobalParameterBase param : _sim.get(GlobalParameterManager.class).getObjectsOf(GlobalParameterBase.class)) {
            _sim.println(param.getPresentationName() + ": " + param.getClass());
            JsonArrayBuilder dimArrayBuilder = Json.createArrayBuilder();
            for (Integer i : param.getDimensionsVector()) {
                dimArrayBuilder.add(i);
            }
            JsonObject paramJSON = Json.createObjectBuilder()
                    .add("name", param.getPresentationName())
                    .add("object_ID", param.getObjectId())
                    .add("class", param.getClass().getSimpleName())
                    .add("definition", param.getQuantity().getDefinition())
                    .add("dimension", dimArrayBuilder)
                    .build();
            simulationParameterBuilder.add(paramJSON);
            count++;
            if (count > 2) {
                break;
            }
        }
        
        JsonObject projJSON = Json.createObjectBuilder().add("name", _sim.getPresentationName()).add("parameters", simulationParameterBuilder).build();
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Workdir\\projects\\2023\\transonic_airfoil\\svd\\ExportSimToJSON_output"))){
            writerFactory.createWriter(writer).write(projJSON);
        }catch (Exception ex) {
            print(ex);
        }
    }
    
    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }
}
