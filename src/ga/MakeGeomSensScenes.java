// Simcenter STAR-CCM+ macro: MakeGeomSensScenes.java
// Written by Simcenter STAR-CCM+ 18.06.006
package ga;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import star.common.*;
import star.vis.*;
import star.meshing.*;

public class MakeGeomSensScenes extends StarMacro {
    
    Simulation _sim;
    TagManager _tagManager;
    
    private final String TAG_NAME = "MGSSOBJ";
    
    @Override
    public void execute() {
        
        _sim = getActiveSimulation();
        _tagManager = _sim.get(TagManager.class);
        int[] weights = new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        
        try {
            deleteTaggedObjects();
            
            Scene srcScene = _sim.getSceneManager().getScene("GeomSensW2");
            
            for (int i : weights) {
                String sceneName = "GeomSensW" + Integer.toString(i);
                Scene destScene = _sim.getSceneManager().createScene();
                destScene.setPresentationName(sceneName);
                destScene.copyProperties(srcScene);
                updateVectorFieldFunctions(i, destScene);
                updateHardcopySettings(destScene);
                _tagManager.addTags(destScene, getTag());
            }
        } catch (Exception ex) {
            print(ex);
        }
    }
    
    private void updateVectorFieldFunctions(int weight, Scene scene) {
        _sim.println(scene.getPresentationName() + " " + Integer.toString(weight));
        for (VectorDisplayer vd : scene.getDisplayerManager().getObjectsOf(VectorDisplayer.class)) {
            String displayerName = vd.getPresentationName();
            displayerName = displayerName.replace("Prof", "");
            char prof = displayerName.charAt(0);
            char surf = displayerName.charAt(1);
            String functionName = "GeometricSensitivity::AIRFOIL_" + prof + "_W" + surf + Integer.toString(weight);
            _sim.println("\t" + functionName);
            FieldFunction fieldFunction = _sim.getFieldFunctionManager().getFunction(functionName);
            PartSurfaceGeometricSensitivityFieldFunction geomSensFunc = (PartSurfaceGeometricSensitivityFieldFunction) fieldFunction;
            vd.getVectorDisplayQuantity().setFieldFunction(geomSensFunc);
            vd.getVectorDisplayQuantity().setAutoRange(AutoRangeMode.NONE);
        }
        
    }
    
    private void updateHardcopySettings(Scene scene) {
        SceneUpdate sceneUpdate = scene.getSceneUpdate();
        HardcopyProperties hardcopyProps = sceneUpdate.getHardcopyProperties();
        hardcopyProps.setOutputWidth(1920);
        hardcopyProps.setOutputWidth(1920);
        hardcopyProps.setOutputHeight(1080);
        hardcopyProps.setOutputHeight(1080);
        hardcopyProps.setOutputWidth(1920);
        hardcopyProps.setOutputHeight(1080);
    }
    
    private Collection<Tag> getTag() {
        Collection<Tag> tagCollection = new ArrayList<>();
        for (Tag ti : _sim.get(TagManager.class).getObjects()) {
            if (ti.getPresentationName().equals(TAG_NAME)) {
                tagCollection = new ArrayList<>();
                tagCollection.add(ti);
                return tagCollection;
            }
        }
        
        Tag createdBytag = _sim.get(TagManager.class).createNewUserTag(TAG_NAME);
        createdBytag.setColor(Color.red);
        tagCollection.add(createdBytag);
        return tagCollection;
    }
    
    private Tag getTagAsTag() {
        return getTag().toArray(new Tag[1])[0];
    }
    
    private void deleteTaggedObjects() {
        /*Delete Tagged Monitor Plots*/
        Collection<StarPlot> toDeleteSP = new ArrayList<>();
        for (StarPlot sp : _sim.getPlotManager().getPlots()) {
            if (sp.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteSP.add(sp);
            }
        }
        _sim.getPlotManager().deleteChildren(toDeleteSP);

        /*Delete tagged scenes*/
        Collection<Scene> scenesToDelete = new ArrayList<>();
        for (Scene s : _sim.getSceneManager().getScenes()) {
            if (s.getTagGroup().getObjects().contains(getTagAsTag())) {
                scenesToDelete.add(s);
            }
        }
        _sim.getSceneManager().deleteChildren(scenesToDelete);
    }
    
    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }
    
}
