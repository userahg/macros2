
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import star.base.neo.DoubleVector;
import star.base.neo.NeoObjectVector;
import star.base.report.MaxReport;
import star.base.report.MinReport;
import star.base.report.Report;
import star.base.report.ReportManager;
import star.base.report.ReportMonitor;
import star.base.report.VolumeAverageReport;
import star.base.report.VolumeStandardDeviationReport;
import star.common.AxisTypeMode;
import star.common.Boundary;
import star.common.ConditionType;
import star.common.FieldFunction;
import star.common.FluidRegion;
import star.common.GlobalParameterManager;
import star.common.PartManager;
import star.common.PlotManager;
import star.common.PorousRegion;
import star.common.PrimitiveFieldFunction;
import star.common.Region;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.StarPlot;
import star.common.Tag;
import star.common.TagManager;
import star.common.VectorComponentFieldFunction;
import star.common.XYPlot;
import star.vis.PartColorMode;
import star.vis.PartDisplayer;
import star.vis.Scene;
import star.vis.SceneManager;
import star.vis.ThresholdMode;
import star.vis.ThresholdPart;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class CreateSolverTroubleshootingObjects extends StarMacro {

    boolean cleanUp = true;
    boolean create = true;

    Simulation _sim;
    String _tag_name = "CSTSO_JAVA";
    HashMap<String, ReportMonitor> _monitors = new HashMap<>();
    TagManager _tagManager;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _tagManager = _sim.get(TagManager.class);

        if (cleanUp) {
            deleteTaggedObjects();
        }
        if (create) {
            createThresholds();
            createScenes();
            createHeatMapPlots();
        }
    }

    private void createThresholds() {
        for (FieldFunction ffi : getResidualFieldFunctions()) {
            double[] range = getThresholdRange(ffi);
            ThresholdPart threshold = _sim.getPartManager().createThresholdPart(new NeoObjectVector(getFluidRegions()), new DoubleVector(range), ffi, 0);
            threshold.setMode(ThresholdMode.OUTSIDE_TAG);
            threshold.setPresentationName(ffi.getPresentationName());
            _tagManager.addTags(threshold, getTag());
        }
    }

    private void createScenes() {
        Scene common = _sim.getSceneManager().createScene();
        common.setPresentationName("All Residuals");
        PartDisplayer partDisp = common.getDisplayerManager().createPartDisplayer("Surface", -1, 1);
        Collection<Boundary> boundaries = new ArrayList<>();
        for (Region ri : getFluidRegions()) {
            boundaries.addAll(ri.getBoundaryManager().getBoundaries());
        }
        partDisp.getVisibleParts().addParts(boundaries);
        partDisp.setOpacity(0.2);
        _tagManager.addTags(common, getTag());

        for (ThresholdPart parti : getThresholdParts()) {
            String ff_name = parti.getPresentationName();
            double[] color;
            if (ff_name.contains("Continuity")) {
                color = new double[]{0.6980000138282776, 0.0, 0.0};
            } else if (ff_name.contains("Energy")) {
                color = new double[]{0.019999999552965164, 0.7200000286102295, 0.800000011920929};
            } else if (ff_name.contains("X-Momentum")) {
                color = new double[]{0.2199999988079071, 0.3700000047683716, 0.05999999865889549};
            } else if (ff_name.contains("Y-Momentum")) {
                color = new double[]{0.09799999743700027, 0.09799999743700027, 0.4392000138759613};
            } else {
                color = new double[]{1.0, 0.7843137383460999, 0.0};
            }

            Scene scene = _sim.getSceneManager().createScene(parti.getPresentationName());
            scene.setPresentationName(parti.getPresentationName());
            PartDisplayer partDisp2 = scene.getDisplayerManager().createPartDisplayer("Surface", -1, 1);
            partDisp2.getVisibleParts().addParts(boundaries);
            partDisp2.setOpacity(0.2);
            _tagManager.addTags(scene, getTag());

            PartDisplayer residualDisplayer = scene.getDisplayerManager().createPartDisplayer("Surface", -1, 1);
            residualDisplayer.setPresentationName(parti.getPresentationName());
            residualDisplayer.getVisibleParts().addParts(parti);
            residualDisplayer.setColorMode(PartColorMode.CONSTANT);
            residualDisplayer.setDisplayerColor(new DoubleVector(color));

            PartDisplayer commonResidualDisplayer = common.getDisplayerManager().createPartDisplayer("Surface", -1, 1);
            commonResidualDisplayer.setPresentationName(parti.getPresentationName());
            commonResidualDisplayer.getVisibleParts().addParts(parti);
            commonResidualDisplayer.setColorMode(PartColorMode.CONSTANT);
            commonResidualDisplayer.setDisplayerColor(new DoubleVector(color));
        }
    }

    private void createHeatMapPlots() {
        PlotManager plotManager = _sim.getPlotManager();
        double[] direction = getPrincipalDirection();
        double x = direction[0];
        double y = direction[1];
        double z = direction[2];

        for (FieldFunction ffi : getResidualFieldFunctions()) {
            XYPlot xyPlot = plotManager.createPlot(XYPlot.class);
            xyPlot.getParts().setObjects(getFluidRegions());
            xyPlot.getXAxisType().setMode(AxisTypeMode.SCALAR);
            xyPlot.getXAxisType().getScalarFunction().setFieldFunction(ffi);
            xyPlot.getYAxes().getAxisType("Y Type 1").setMode(AxisTypeMode.DIRECTION);
            xyPlot.getYAxes().getAxisType("Y Type 1").getDirectionVector().setComponents(x, y, z);
            xyPlot.setPresentationName(ffi.getPresentationName());
            _tagManager.addTags(xyPlot, getTag());
        }

        FieldFunction volChange = _sim.getFieldFunctionManager().getFunction("VolumeChange");
        FieldFunction quality = _sim.getFieldFunctionManager().getFunction("CellQuality");
        XYPlot qualityPlot = plotManager.createPlot(XYPlot.class);
        qualityPlot.getParts().setObjects(getFluidRegions());
        qualityPlot.getXAxisType().setMode(AxisTypeMode.SCALAR);
        qualityPlot.getXAxisType().getScalarFunction().setFieldFunction(volChange);
        qualityPlot.getYAxes().getAxisType("Y Type 1").getScalarFunction().setFieldFunction(quality);
        qualityPlot.setPresentationName("Vol Change vs Quality");
        _tagManager.addTags(qualityPlot, getTag());
    }

    private double[] getThresholdRange(FieldFunction ff) {
        Region[] regions = getFluidRegions();
        VolumeAverageReport avgReport = _sim.getReportManager().createReport(VolumeAverageReport.class);
        avgReport.setFieldFunction(ff);
        avgReport.getParts().setObjects(regions);
        VolumeStandardDeviationReport stDevReport = _sim.getReportManager().createReport(VolumeStandardDeviationReport.class);
        stDevReport.setFieldFunction(ff);
        stDevReport.getParts().setObjects(regions);
        MaxReport maxReport = _sim.getReportManager().createReport(MaxReport.class);
        maxReport.setFieldFunction(ff);
        maxReport.getParts().setObjects(regions);
        MinReport minReport = _sim.getReportManager().createReport(MinReport.class);
        minReport.setFieldFunction(ff);
        minReport.getParts().setObjects(regions);
        _tagManager.addTags(avgReport, getTag());
        _tagManager.addTags(stDevReport, getTag());
        _tagManager.addTags(maxReport, getTag());
        _tagManager.addTags(minReport, getTag());

        double max = maxReport.getReportMonitorValue();
        double min = minReport.getReportMonitorValue();
        double mean = avgReport.getReportMonitorValue();
        double stDev = stDevReport.getReportMonitorValue();

        double upper;
        double lower;

        if (max > 0) {
            upper = Double.min(0.8 * max, mean + 2 * stDev);
        } else {
            upper = Double.min(max * 1.2, mean + 2 * stDev);
        }

        if (min > 0) {
            lower = Double.max(min * 1.2, mean - 2 * stDev);
        } else {
            lower = Double.max(0.8 * min, mean - 2 * stDev);
        }

        ReportManager reportManager = _sim.getReportManager();
        reportManager.deleteChildren(new NeoObjectVector(new Report[]{avgReport, stDevReport, maxReport, minReport}));

        return new double[]{lower, upper};
    }

    private double[] getPrincipalDirection() {
        PrimitiveFieldFunction centroid = (PrimitiveFieldFunction) _sim.getFieldFunctionManager().getFunction("Centroid");
        VectorComponentFieldFunction centroid_x = (VectorComponentFieldFunction) centroid.getComponentFunction(0);
        VectorComponentFieldFunction centroid_y = (VectorComponentFieldFunction) centroid.getComponentFunction(1);
        VectorComponentFieldFunction centroid_z = (VectorComponentFieldFunction) centroid.getComponentFunction(2);
        double maxX, minX, maxY, minY, maxZ, minZ, diffX, diffY, diffZ;
        double[] direction;

        Collection<Report> toDelete = new ArrayList<>();
        Region[] regions = getFluidRegions();
        MaxReport maxReport = _sim.getReportManager().createReport(MaxReport.class);
        maxReport.setFieldFunction(centroid_x);
        maxReport.getParts().setObjects(regions);
        toDelete.add(maxReport);
        MinReport minReport = _sim.getReportManager().createReport(MinReport.class);
        minReport.setFieldFunction(centroid_y);
        minReport.getParts().setObjects(regions);
        toDelete.add(minReport);

        maxX = maxReport.getReportMonitorValue();
        minX = minReport.getReportMonitorValue();
        diffX = java.lang.Math.abs(maxX - minX);
        maxReport.setFieldFunction(centroid_y);
        minReport.setFieldFunction(centroid_y);
        maxY = maxReport.getReportMonitorValue();
        minY = minReport.getReportMonitorValue();
        diffY = java.lang.Math.abs(maxY - minY);
        maxReport.setFieldFunction(centroid_z);
        minReport.setFieldFunction(centroid_z);
        maxZ = maxReport.getReportMonitorValue();
        minZ = minReport.getReportMonitorValue();
        diffZ = java.lang.Math.abs(maxZ - minZ);
        _sim.getReportManager().deleteChildren(toDelete);

        if (diffX > diffY && diffX > diffZ) {
            direction = new double[]{1, 0, 0};
        } else if (diffY > diffX && diffY > diffZ) {
            direction = new double[]{0, 1, 0};
        } else {
            direction = new double[]{0, 0, 1};
        }

        return direction;
    }

    private Region[] getFluidRegions() {
        ArrayList<Region> fluidRegions = new ArrayList<>();
        for (Region ri : _sim.getRegionManager().getRegions()) {
            ConditionType type = ri.getRegionType();
            if (type instanceof FluidRegion) {
                fluidRegions.add(ri);
            } else if (type instanceof PorousRegion) {
                fluidRegions.add(ri);
            }

        }
        return fluidRegions.toArray(new Region[fluidRegions.size()]);
    }

    private Collection<ThresholdPart> getThresholdParts() {
        Collection<ThresholdPart> parts = new ArrayList<>();
        for (ThresholdPart parti : _sim.getPartManager().getObjectsOf(ThresholdPart.class)) {
            if (parti.getTagGroup().getObjects().contains(getTagAsTag())) {
                parts.add(parti);
            }
        }
        return parts;
    }

    private Collection<FieldFunction> getResidualFieldFunctions() {
        Collection<FieldFunction> residuals = new ArrayList<>();
        for (FieldFunction ffi : _sim.getFieldFunctionManager().getObjects()) {
            if (ffi.getPresentationName().endsWith(" Residual")) {
                residuals.add(ffi);
            }
        }
        return residuals;
    }

    private void deleteTaggedObjects() {

        PartManager partManager = _sim.getPartManager();
        Collection<ThresholdPart> partsToDelete = new ArrayList<>();
        for (ThresholdPart parti : partManager.getObjectsOf(ThresholdPart.class)) {
            if (parti.getTagGroup().getObjects().contains(getTagAsTag())) {
                partsToDelete.add(parti);
            }
        }
        partManager.deleteChildren(partsToDelete);

        SceneManager sceneManager = _sim.getSceneManager();
        Collection<Scene> scenesToDelete = new ArrayList<>();
        for (Scene si : sceneManager.getScenes()) {
            if (si.getTagGroup().getObjects().contains(getTagAsTag())) {
                scenesToDelete.add(si);
            }
        }
        sceneManager.deleteChildren(scenesToDelete);

        Collection<StarPlot> toDeleteSP = new ArrayList<>();
        for (StarPlot sp : _sim.getPlotManager().getPlots()) {
            if (sp.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteSP.add(sp);
            }
        }
        _sim.getPlotManager().deleteChildren(toDeleteSP);

        Collection<Report> toDeleteR = new ArrayList<>();
        for (Report r : _sim.getReportManager().getObjects()) {
            if (r.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteR.add(r);
            }
        }
        _sim.getReportManager().deleteChildren(toDeleteR);

//        SteadySolver steadySolver = _sim.getSolverManager().getSolver(SteadySolver.class);
//        Collection<MonitorIterationStoppingCriterion> toDeleteSC = new ArrayList<>();
//        Collection<MonitorIterationStoppingCriterion> criteria = steadySolver.getSolverStoppingCriterionManager().getObjectsOf(MonitorIterationStoppingCriterion.class);
//        for (MonitorIterationStoppingCriterion misc : criteria) {
//            if (misc.getTagGroup().getObjects().contains(getTagAsTag())) {
//                toDeleteSC.add(misc);
//            }
//        }
//        steadySolver.getSolverStoppingCriterionManager().deleteChildren(toDeleteSC);
//        Collection<Monitor> toDeleteM = new ArrayList<>();
//        for (Monitor m : _sim.getMonitorManager().getMonitors()) {
//            if (m.getTagGroup().getObjects().contains(getTagAsTag())) {
//                toDeleteM.add(m);
//            }
//        }
//        _sim.getMonitorManager().deleteChildren(toDeleteM);
//
//        toDeleteR = new ArrayList<>();
//        for (Report r : _sim.getReportManager().getObjects()) {
//            if (r.getTagGroup().getObjects().contains(getTagAsTag())) {
//                toDeleteR.add(r);
//            }
//        }
//        _sim.getReportManager().deleteChildren(toDeleteR);
//        Collection<ScalarGlobalParameter> toDeleteSGP = new ArrayList<>();
//        for (ScalarGlobalParameter sgp : _sim.get(GlobalParameterManager.class).getObjectsOf(ScalarGlobalParameter.class)) {
//            if (sgp.getTagGroup().getObjects().contains(getTagAsTag())) {
//                toDeleteSGP.add(sgp);
//            }
//        }
//        _sim.get(GlobalParameterManager.class).deleteChildren(toDeleteSGP);
    }

    private Tag getTagAsTag() {
        return getTag().toArray(new Tag[1])[0];
    }

    private Collection<Tag> getTag() {
        Collection<Tag> tagCollection = new ArrayList<>();
        for (Tag ti : _tagManager.getObjects()) {
            if (ti.getPresentationName().equals(_tag_name)) {
                tagCollection = new ArrayList<>();
                tagCollection.add(ti);
                return tagCollection;
            }
        }

        Tag createdBytag = _tagManager.createNewUserTag(_tag_name);
        tagCollection.add(createdBytag);
        return tagCollection;
    }
}
