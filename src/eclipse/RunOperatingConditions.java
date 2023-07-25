package eclipse;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import star.base.neo.NamedObject;
import star.base.neo.NeoObjectVector;
import star.base.report.ExpressionReport;
import star.base.report.Report;
import star.common.Boundary;
import star.common.CommentManager;
import star.common.FieldFunction;
import star.common.GlobalParameterManager;
import star.common.Part;
import star.common.Representation;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.StepStoppingCriterion;
import star.common.Tag;
import star.flow.ForceCoefficientReport;
import star.flow.ForceReport;
import star.post.RecordedSolutionView;
import star.post.SolutionHistory;
import star.post.SolutionHistoryManager;
import star.post.SolutionViewManager;
import star.vis.Displayer;
import star.vis.Scene;

public class RunOperatingConditions extends StarMacro {

    Simulation _sim;
    List<OperatingCondition> _conditions;
    final String _solHistName = "OpCondHistory";
    final String[] _OCTags = {"OC1", "OC2", "OC3", "OC4", "OC5"};

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _conditions = new ArrayList<>();
        populateOperatingConditions();
        for (OperatingCondition oc : _conditions) {
            oc.runSolver();
        }
        ((StepStoppingCriterion) _sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps")).setMaximumNumberSteps(_sim.getSimulationIterator().getCurrentIteration());
        _sim.saveState(_sim.getSessionDir() + File.separator + _sim.getPresentationName() + "_Custom.sim");
    }

    private void populateOperatingConditions() {
        _conditions.add(new OperatingCondition(_OCTags[0], new Object[]{"Alpha", 3.2}, new Object[]{"Beta", 12.3}, new Object[]{"M_ref", 0.527}, new Object[]{"P_ref", 16235.7}, new Object[]{"T0_ex", 416.778}, new Object[]{"T0_inlet", 228.778}, new Object[]{"T_ref", 216.65}, new Object[]{"mdot_in", 4.93}, new Object[]{"mdot_out", 4.931}));
        OperatingCondition oc = new OperatingCondition(_OCTags[1], new Object[]{"Alpha", 1.7}, new Object[]{"Beta", 0.0}, new Object[]{"M_ref", 0.65}, new Object[]{"P_ref", 16235.7}, new Object[]{"T0_ex", 427.444}, new Object[]{"T0_inlet", 235.111}, new Object[]{"T_ref", 216.65}, new Object[]{"mdot_in", 5.362}, new Object[]{"mdot_out", 5.366});
        oc.setClearSolution(true);
        _conditions.add(oc);
        oc = new OperatingCondition(_OCTags[2], new Object[]{"Alpha", -14.6}, new Object[]{"Beta", 0.0}, new Object[]{"M_ref", 0.264}, new Object[]{"P_ref", 45865.2}, new Object[]{"T0_ex", 427.0}, new Object[]{"T0_inlet", 251.333}, new Object[]{"T_ref", 247.813}, new Object[]{"mdot_in", 10.825}, new Object[]{"mdot_out", 10.85});
        oc.setClearSolution(true);
        _conditions.add(oc);
        oc = new OperatingCondition(_OCTags[3], new Object[]{"Alpha", 5.4}, new Object[]{"Beta", 17.3}, new Object[]{"M_ref", 0.264}, new Object[]{"P_ref", 45865.2}, new Object[]{"T0_ex", 427.0}, new Object[]{"T0_inlet", 251.333}, new Object[]{"T_ref", 247.813}, new Object[]{"mdot_in", 10.825}, new Object[]{"mdot_out", 10.85});
        oc.setClearSolution(true);
        _conditions.add(oc);
        _conditions.add(new OperatingCondition(_OCTags[4], new Object[]{"Alpha", 13.3}, new Object[]{"Beta", 0.0}, new Object[]{"M_ref", 0.264}, new Object[]{"P_ref", 45865.2}, new Object[]{"T0_ex", 427.0}, new Object[]{"T0_inlet", 251.333}, new Object[]{"T_ref", 247.813}, new Object[]{"mdot_in", 10.825}, new Object[]{"mdot_out", 10.85}));
    }

    void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }

    private class OperatingCondition {

        final private HashMap<String, Double> _params;
        private String _name;
        private boolean _clearSol;

        public OperatingCondition() {
            _params = new HashMap<>();
            _name = "";
            _clearSol = false;
        }

        public OperatingCondition(Object[]... params) {
            _params = new HashMap<>();
            _name = "";
            _clearSol = false;
            defineParams(params);
        }

        public OperatingCondition(String name, Object[]... params) {
            _params = new HashMap<>();
            _name = name;
            _clearSol = false;
            defineParams(params);
        }

        void setName(String name) {
            _name = name;
        }

        String getName() {
            return _name;
        }
        
        void setClearSolution(boolean clearSol) {
            _clearSol = clearSol;
        }
        
        boolean getClearSolution() {
            return _clearSol;
        }

        void defineParam(String param, double val) {
            _params.put(param, val);
        }

        final void defineParams(Object[]... params) {
            for (Object[] array : params) {
                defineParam((String) array[0], (Double) array[1]);
            }
        }

        void updateParamInSim(String param) {
            Double val = _params.get(param);
            setParam(param, val);
        }

        void updateAllParamsInSim() {
            for (Entry<String, Double> entry : _params.entrySet()) {
                setParam(entry.getKey(), entry.getValue());
            }
        }

        void runSolver() {
            prepHistory();
            updateAllParamsInSim();
            if (_clearSol) {
                _sim.getSolution().clearSolution();
            }
            updateStoppingCriteria();
            _sim.getSimulationIterator().run();
            prepRepresentation();
            updateReports();
            updateScenes();
        }

        SolutionHistory createSolutionHistory() {
            SolutionHistory hist = _sim.get(SolutionHistoryManager.class).createForFile(_sim.getSessionDir() + File.separator + _solHistName + ".simh", false, false);
            hist.setAutoRecord(false);
            hist.setAddReportFieldFunctions(true);
            hist.getInputs().setObjects(getHistoryParts());

            ArrayList<String> scalarFunctions = new ArrayList<>();
            ArrayList<String> vectorFunctions = new ArrayList<>();

            scalarFunctions.add("AbsolutePressure");
            scalarFunctions.add("Density");
            scalarFunctions.add("Pressure");
            scalarFunctions.add("PressureCoefficient");
            scalarFunctions.add("MachNumber");
            scalarFunctions.add("TotalPressure");
            scalarFunctions.add("Compressor Face Distortion");
            scalarFunctions.add("P0 / P0_ref");
            scalarFunctions.add("WallYplus");
            scalarFunctions.add("SkinFrictionCoefficient");
            vectorFunctions.add("VorticityVector");

            ArrayList<Object> objects = new ArrayList<>();

            for (String s : scalarFunctions) {
                FieldFunction fi = _sim.getFieldFunctionManager().getFunction(s);
                objects.add(fi);
            }

            for (String s : vectorFunctions) {
                FieldFunction fi = _sim.getFieldFunctionManager().getFunction(s);
                FieldFunction fii = fi.getMagnitudeFunction();
                FieldFunction fx = fi.getComponentFunction(0);
                FieldFunction fy = fi.getComponentFunction(0);
                FieldFunction fz = fi.getComponentFunction(0);
                objects.addAll(Arrays.asList(fi, fii, fx, fy, fz));
            }

            hist.setFunctions(new NeoObjectVector(objects.toArray(new Object[objects.size()])));
            hist.getRegions().setQuery(null);
            hist.getRegions().setObjects();
            return hist;
        }

        SolutionHistory createSolutionHistory4Region() {

            ArrayList<NamedObject> region = new ArrayList<>();
            region.add(_sim.getRegionManager().getRegion("Air"));

            SolutionHistory hist = _sim.get(SolutionHistoryManager.class).createForFile(_sim.getSessionDir() + File.separator + "toDelete" + ".simh", false, false);
            hist.setAutoRecord(false);
            hist.setAddReportFieldFunctions(true);
            hist.getRegions().setObjects(region);

            ArrayList<String> vectorFunctions = new ArrayList<>();

            vectorFunctions.add("VorticityVector");

            ArrayList<Object> objects = new ArrayList<>();

            for (String s : vectorFunctions) {
                FieldFunction fi = _sim.getFieldFunctionManager().getFunction(s);
                FieldFunction fii = fi.getMagnitudeFunction();
                FieldFunction fx = fi.getComponentFunction(0);
                FieldFunction fy = fi.getComponentFunction(0);
                FieldFunction fz = fi.getComponentFunction(0);
                objects.addAll(Arrays.asList(fx));
            }

            hist.setFunctions(new NeoObjectVector(objects.toArray(new Object[objects.size()])));
            return hist;
        }

        SolutionHistory getSolutionHistory() {
            SolutionHistory hist = null;
            try {
                hist = _sim.get(SolutionHistoryManager.class).getObject(_solHistName);
            } catch (Exception ex) {
                if (ex.getMessage().toLowerCase().contains("does not exist in manager")) { // Solution History Does Not Exist
                    hist = createSolutionHistory();
                } else { // Some other exception
                    print(ex);
                }
            }
            return hist;
        }

        SolutionHistory getSolutionHistory4Region() {
            SolutionHistory hist = null;
            try {
                hist = _sim.get(SolutionHistoryManager.class).getObject("toDelete");
            } catch (Exception ex) {
                if (ex.getMessage().toLowerCase().contains("does not exist in manager")) { // Solution History Does Not Exist
                    hist = createSolutionHistory4Region();
                } else { // Some other exception
                    print(ex);
                }
            }
            return hist;
        }

        RecordedSolutionView createSolutionView() {
            SolutionHistory hist = getSolutionHistory();
            RecordedSolutionView view = hist.createRecordedSolutionView(false);
            view.setPresentationName(_name);
            return view;
        }

        RecordedSolutionView createSolutionView4Region() {
            SolutionHistory hist = getSolutionHistory4Region();
            RecordedSolutionView view = hist.createRecordedSolutionView(false);
            view.setPresentationName(_name + "_region");
            return view;
        }

        RecordedSolutionView getSolutionView() {
            RecordedSolutionView view = null;
            try {
                view = (RecordedSolutionView) _sim.get(SolutionViewManager.class).getObject(_name);
            } catch (Exception ex) {
                if (ex.getMessage().contains("does not exist in manager")) { // Recorded View Does Not exist
                    view = createSolutionView();
                } else { // Some other exception
                    print(ex);
                }
            }
            return view;
        }

        RecordedSolutionView getSolutionView4Region() {
            RecordedSolutionView view = null;
            try {
                view = (RecordedSolutionView) _sim.get(SolutionViewManager.class).getObject(_name + "_region");
            } catch (Exception ex) {
                if (ex.getMessage().contains("does not exist in manager")) { // Recorded View Does Not exist
                    view = createSolutionView4Region();
                } else { // Some other exception
                    print(ex);
                }
            }
            return view;
        }

        void prepHistory() {
            getSolutionHistory();
            getSolutionHistory4Region();
        }

        void updateStoppingCriteria() {
            int currentIter = _sim.getSimulationIterator().getCurrentIteration();
            StepStoppingCriterion maxSteps = (StepStoppingCriterion) _sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps");
            maxSteps.setMaximumNumberSteps(5000 + currentIter);
        }

        void prepRepresentation() {
            getSolutionHistory().createSnapshot(_name);
            getSolutionHistory4Region().createSnapshot(_name);
            getSolutionView().setStateName(_name);
            getSolutionView4Region().setStateName(_name);
        }

        void updateReports() {
            Tag tag = _sim.getTagManager().getObject(_name);
            for (Report r : _sim.getReportManager().getObjects()) {
                for (Tag ti : r.getTagGroup().getObjects()) {
                    if (ti.equals(tag)) {
                        switch (r.getClass().getSimpleName().toLowerCase()) {
                            case "expressionreport":
                                ExpressionReport er = (ExpressionReport) r;
                                String sourceReportName = _sim.get(CommentManager.class).getCommentFor(er).getText();
                                Report sourceReport = _sim.getReportManager().getReport(sourceReportName);
                                er.setDefinition(Double.toString(sourceReport.getReportMonitorValue()));
                                break;
                            case "forcecoefficientreport":
                                ForceCoefficientReport fcr = (ForceCoefficientReport) r;
                                fcr.setRepresentation(_sim.getRepresentationManager().getObject(_name));
                                break;
                            case "forcereport":
                                ForceReport fr = (ForceReport) r;
                                fr.setRepresentation(_sim.getRepresentationManager().getObject(_name));
                                break;
                        }
                    }
                }
            }
        }

        void updateScenes() {
            Representation fvRep = _sim.getRepresentationManager().getObject("Volume Mesh");
            Representation lsvRep = _sim.getRepresentationManager().getObject("Latest Surface/Volume");
            Representation finalRep = _sim.getRepresentationManager().getObject(_name);

            Tag tag = _sim.getTagManager().getObject(_name);
            for (Scene s : _sim.getSceneManager().getScenes()) {
                for (Tag ti : s.getTagGroup().getObjects()) {
                    if (ti.equals(tag)) {
                        if (s.getPresentationName().startsWith("31")) {
                            Displayer scalar = s.getDisplayerManager().getObject("Scalar 1");
                            scalar.setRepresentation(finalRep);
                            Displayer volume = s.getDisplayerManager().getObject("Resampled Volume Scalar 1");
                            volume.setRepresentation(_sim.getRepresentationManager().getObjectIgnoreCase(_name + "_region"));
                        } else {
                            for (Displayer d : s.getDisplayerManager().getObjects()) {
                                if (d.getRepresentation().equals(fvRep) || d.getRepresentation().equals(lsvRep)) {
                                    d.setRepresentation(finalRep);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void setParam(String param_name, double val) {
            GlobalParameterManager paramMan = _sim.get(GlobalParameterManager.class);
            ScalarGlobalParameter param = (ScalarGlobalParameter) paramMan.getObject(param_name);
            param.getQuantity().setValue(val);
        }

        private NamedObject[] getHistoryParts() {
            ArrayList<NamedObject> objects = new ArrayList<>();
            Part part = _sim.getPartManager().getPart("Y=-0.9m plane");
            objects.add(part);
            part = _sim.getPartManager().getPart("Isosurface");
            objects.add(part);
            part = _sim.getPartManager().getPart("WingNacelleCS");
            objects.add(part);

            for (Boundary b : _sim.getRegionManager().getRegion("Air").getBoundaryManager().getBoundaries()) {
                if (!b.getPresentationName().equals("Farfield")) {
                    objects.add(b);
                }
            }

            return objects.toArray(new NamedObject[objects.size()]);
        }
    }
}
