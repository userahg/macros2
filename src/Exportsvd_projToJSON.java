import java.util.Map;
import star.mdx.MdxProject;
import java.io.PrintWriter;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.io.BufferedWriter;
import javax.json.*;
import java.io.FileWriter;
import java.util.HashMap;
import star.mdx.MdxDesignSet;
import star.mdx.MdxDesignStudy;
import star.mdx.MdxMacro;
import star.mdx.MdxStudyParameter;
import star.mdx.MdxStudyParameterBase;

public class Exportsvd_projToJSON extends MdxMacro {

    MdxProject _proj;

    @Override
    public void execute() {
        _proj = getActiveMdxProject();
        MdxDesignStudy s = _proj.getDesignStudyManager().getDesignStudy("Test");
        export();
    }
    
    private void parameterInfo() {
        for (MdxDesignStudy study_i : _proj.getDesignStudyManager().getDesignStudies()) {
            for (MdxStudyParameter param_i : study_i.getStudyParameters().getObjectsOf(MdxStudyParameter.class)) {
                param_i.getClass().getName();
                param_i.getClass().getSimpleName();
                MdxStudyParameterBase.ParameterType type = param_i.getParameterType();
                param_i.getBaselineQuantity().getRawValue();
                param_i.getMinimumQuantity().getRawValue();
                param_i.getMaximumQuantity().getRawValue();
                param_i.getContinuousParameterValue().getResolution();
                _proj.println(type);
            }
        }
    }

    private void export() {
        JsonArrayBuilder studyArrayBuilder = Json.createArrayBuilder();
        
        for (MdxDesignStudy study_i : _proj.getDesignStudyManager().getDesignStudies()) {
            JsonArrayBuilder paramArrayBuilder = Json.createArrayBuilder();
            for (MdxStudyParameter param_i : study_i.getStudyParameters().getObjectsOf(MdxStudyParameter.class)) {
                JsonArrayBuilder listArrayBuilder = Json.createArrayBuilder();
                for (Double val_i : param_i.getDiscreteParameterValue().getQuantity().getArray()) {
                    JsonObject listValJSON = Json.createObjectBuilder()
                            .add("value", val_i)
                            .build();
                    listArrayBuilder.add(listValJSON);
                }
                JsonObject paramJSON = Json.createObjectBuilder()
                        .add("name", param_i.getPresentationName())
                        .add("object_ID", param_i.getObjectId())
                        .add("list_values", listArrayBuilder)
                        .add("baseline", param_i.getBaselineQuantity().getRawValue())
                        .add("min", param_i.getMinimumQuantity().getRawValue())
                        .add("max", param_i.getMaximumQuantity().getRawValue())
                        .build();
                paramArrayBuilder.add(paramJSON);
            }
            
            JsonArrayBuilder designSetArrayBuilder = Json.createArrayBuilder();
            for (MdxDesignSet design_set_i : study_i.getDesignSets().getDesignSets()) {
                JsonObject designSetJSON = Json.createObjectBuilder()
                        .add("name", design_set_i.getPresentationName())
                        .add("object_ID", design_set_i.getObjectId())
                        .build();
                designSetArrayBuilder.add(designSetJSON);
            }
            JsonObject studyJSON = Json.createObjectBuilder()
                    .add("name", study_i.getPresentationName())
                    .add("object_ID", study_i.getObjectId())
                    .add("design sets", designSetArrayBuilder)
                    .add("parameters", paramArrayBuilder)
                    .build();
            studyArrayBuilder.add(studyJSON);
            break;
        }

        JsonObject projJSON = Json.createObjectBuilder().add("name", _proj.getPresentationName()).add("studies", studyArrayBuilder).build();
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Workdir\\projects\\2023\\transonic_airfoil\\svd\\Exportsvd_projToJSON_output"))){
            writerFactory.createWriter(writer).write(projJSON);
        }catch (Exception ex) {
            print(ex);
        }

    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _proj.println(sw);
    }

}