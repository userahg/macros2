// Simcenter STAR-CCM+ macro: MakeGeomSensScenes.java
// Written by Simcenter STAR-CCM+ 18.06.006
package compressor;

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

        try {
            deleteTaggedObjects();
            // makeCSTWeightScenes();
            makeOtherParamScenes();

        } catch (Exception ex) {
            print(ex);
        }
    }

    private void makeCSTWeightScenes() {
        int[] weights = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Scene srcScene = _sim.getSceneManager().getScene("SensW_U0");

        for (String surf : new String[]{"U", "L"}) {
            for (int i : weights) {
                String sceneName = "SensW_" + surf + Integer.toString(i);
                Scene destScene = _sim.getSceneManager().createScene();
                destScene.setPresentationName(sceneName);
                destScene.copyProperties(srcScene);
                updateVectorFieldFunctions(i, surf, destScene);
                updateHardcopySettings(destScene);
                _tagManager.addTags(destScene, getTag());
            }
        }
    }

    private void makeOtherParamScenes() {
        String[] params = new String[]{"angle", "chord", "P_leadingEdgeX", "P_leadingEdgeZ"};
        Scene srcScene = _sim.getSceneManager().getScene("SensW_U0");

        for (String s : params) {
            String sceneName = "Sens" + s;
            Scene destScene = _sim.getSceneManager().createScene();
            destScene.setPresentationName(sceneName);
            destScene.copyProperties(srcScene);
            updateVectorFieldFunctions2(s, destScene);
            updateHardcopySettings(destScene);
            _tagManager.addTags(destScene, getTag());
        }
    }

    private void updateVectorFieldFunctions(int weight, String surf, Scene scene) {
        _sim.println(scene.getPresentationName() + " " + Integer.toString(weight));
        for (VectorDisplayer vd : scene.getDisplayerManager().getObjectsOf(VectorDisplayer.class)) {
            String displayerName = vd.getPresentationName();
            displayerName = displayerName.replace("Sens", "").toLowerCase();
            if (!isRootOrTip(displayerName)) {
                displayerName = "prof" + displayerName;
            }
            String functionName = "GeometricSensitivity::" + displayerName + "P_W_" + surf + Integer.toString(weight);
            _sim.println("\t" + functionName);
            FieldFunction fieldFunction = _sim.getFieldFunctionManager().getFunction(functionName);
            PartSurfaceGeometricSensitivityFieldFunction geomSensFunc = (PartSurfaceGeometricSensitivityFieldFunction) fieldFunction;
            vd.getVectorDisplayQuantity().setFieldFunction(geomSensFunc);
            vd.getVectorDisplayQuantity().setAutoRange(AutoRangeMode.NONE);
        }
    }
    
    private void updateVectorFieldFunctions2(String param, Scene scene) {
        _sim.println(scene.getPresentationName() + " " + param);
        for (VectorDisplayer vd : scene.getDisplayerManager().getObjectsOf(VectorDisplayer.class)) {
            String displayerName = vd.getPresentationName();
            displayerName = displayerName.replace("Sens", "").toLowerCase();
            if (!isRootOrTip(displayerName)) {
                displayerName = "prof" + displayerName;
            }
            String functionName = "GeometricSensitivity::" + displayerName + param;
            _sim.println("\t" + functionName);
            FieldFunction fieldFunction = _sim.getFieldFunctionManager().getFunction(functionName);
            PartSurfaceGeometricSensitivityFieldFunction geomSensFunc = (PartSurfaceGeometricSensitivityFieldFunction) fieldFunction;
            vd.getVectorDisplayQuantity().setFieldFunction(geomSensFunc);
            vd.getVectorDisplayQuantity().setAutoRange(AutoRangeMode.NONE);
        }
    }

    private boolean isRootOrTip(String s) {
        String[] comp = new String[]{"root", "tip"};
        boolean isRootOrTip = false;
        for (String c_i : comp) {
            if (s.equals(c_i)) {
                isRootOrTip = true;
            }
        }
        return isRootOrTip;
    }

    private void updateHardcopySettings(Scene scene) {
        SceneUpdate sceneUpdate = scene.getSceneUpdate();
        HardcopyProperties hardcopyProps = sceneUpdate.getHardcopyProperties();
        hardcopyProps.setOutputWidth(1600);
        hardcopyProps.setOutputWidth(1600);
        hardcopyProps.setOutputHeight(1000);
        hardcopyProps.setOutputHeight(1000);
        hardcopyProps.setOutputWidth(1600);
        hardcopyProps.setOutputHeight(1000);
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
