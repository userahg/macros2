/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crmOpt;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import star.base.neo.ClientServerObjectGroup;
import star.base.report.ExpressionReport;
import star.base.report.Report;
import star.base.report.SumReport;
import star.common.FieldFunctionTypeOption;
import star.common.GlobalParameterManager;
import star.common.PartSurface;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;
import star.common.StarPlot;
import star.common.Tag;
import star.common.TagManager;
import star.common.UserFieldFunction;
import star.common.XYPlot;
import star.meshing.LatestMeshProxyRepresentation;
import star.meshing.MeshOperationPart;

/**
 *
 * @author cd8unu
 */
public class MakeLocalSensPlotsCST extends StarMacro {

    boolean clean_reports = false;
    boolean clean_plots = true;
    String[] reportNames = new String[]{"Cl", "Cd", "Area_Constraint", "L_over_D"};
    String[] paramNames = new String[]{"P_TE_Thickness", "P_W_L0", "P_W_L1", "P_W_L2", "P_W_L3", "P_W_L4", "P_W_L5", "P_W_L6", "P_W_L7", "P_W_L8", "P_W_L9", "P_W_L10", "P_W_U0", "P_W_U1", "P_W_U2", "P_W_U3", "P_W_U4", "P_W_U5", "P_W_U6", "P_W_U7", "P_W_U8", "P_W_U9", "P_W_U10"};
    String gsComparisonPlotTemplateName = "GS_Manual_VS_Mesh_Angle_L0";
    String lsComparisonPlotTemplateName = "Manual_VS_Mapped_Area_L0_Sensitivity";
    Simulation _sim;

    Report[] reports;
    ScalarGlobalParameter[] params;
    StarPlot gsComparisonPlotTemplate;
    StarPlot lsComparisonPlotTemplate;
    TagManager tagManager;
    private final String tag_name = "MLSPLTCSTJAVA";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        try {
            initialize();
            deleteTaggedObjects();
//            createGeomSensMappedFFs();
//            createMappedLocalSensFFs();
//            createManualLocalSenssFFs();
//            createExpressionSensReports();
//            createSumSensReports();
//            createGSComparisonPlots();
            createLSComparisonPlots();
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void initialize() throws Exception {
        tagManager = _sim.get(TagManager.class);
        ArrayList<Report> reportList = new ArrayList<>();
        ArrayList<ScalarGlobalParameter> paramList = new ArrayList<>();
        for (String s : reportNames) {
            try {
                Report r = _sim.getReportManager().getReport(s);
                if (r == null) {
                    throw new Exception();
                } else {
                    reportList.add(r);
                }
            } catch (Exception ex) {
                _sim.println("Report " + s + " not found!!! Cannot continue.");
                throw ex;
            }
        }
        reports = reportList.toArray(new Report[reportList.size()]);

        for (String s : paramNames) {
            try {
                ScalarGlobalParameter sgp = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject(s);
                if (sgp == null) {
                    throw new Exception();
                } else {
                    paramList.add(sgp);
                }
            } catch (Exception ex) {
                _sim.println("Parameter " + s + " not found!!! Cannot continue.");

            }
        }
        params = paramList.toArray(new ScalarGlobalParameter[paramList.size()]);
        if (!_sim.getPlotManager().has(gsComparisonPlotTemplateName)) {
            throw new Exception("Template plot " + gsComparisonPlotTemplateName + " does not exist. Please create!");
        }
        if (!_sim.getPlotManager().has(lsComparisonPlotTemplateName)) {
            throw new Exception("Template plot " + lsComparisonPlotTemplateName + " does not exist. Please create!");
        }
        gsComparisonPlotTemplate = _sim.getPlotManager().getPlot(gsComparisonPlotTemplateName);
        lsComparisonPlotTemplate = _sim.getPlotManager().getPlot(lsComparisonPlotTemplateName);
    }

    private void createGeomSensMappedFFs() {
        for (ScalarGlobalParameter sgp : params) {
            UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
            uff.getTagGroup().add(getTagAsTag());
            uff.getTypeOption().setSelected(FieldFunctionTypeOption.Type.VECTOR);
            uff.setPresentationName(getMappedGSFFName(sgp));
            uff.setSyncName(true);
            uff.setDefinition(getGeomSensMappedFFDefinition(sgp));
        }
    }

    private void createMappedLocalSensFFs() {
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
                uff.getTagGroup().add(getTagAsTag());
                uff.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
                uff.setPresentationName(getMappedLocalSensFFName(r, sgp));
                uff.setSyncName(true);
                uff.setDefinition(getMappedLocalSensFFDefinition(r, sgp));
            }
        }
    }

    private void createManualLocalSenssFFs() {
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
                uff.getTagGroup().add(getTagAsTag());
                uff.getTypeOption().setSelected(FieldFunctionTypeOption.Type.SCALAR);
                uff.setPresentationName(getManualLocalSensFFName(r, sgp));
                uff.setSyncName(true);
                uff.setDefinition(getManualLocalSensFFDefinition(r, sgp));
            }
        }
    }

    private void createExpressionSensReports() {
        String groupName = "Sens ER";
        ClientServerObjectGroup group;
        ArrayList<Report> created = new ArrayList<>();
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
                er.getTagGroup().add(getTagAsTag());
                er.setPresentationName(getExpressionSensReportName(r, sgp));
                er.setDefinition(getExpressionSensReportDefinition(r, sgp));
                created.add(er);
            }
        }

        if (!_sim.getReportManager().getGroupsManager().has(groupName)) {
            _sim.getReportManager().getGroupsManager().createGroup(groupName);
        }
        group = (ClientServerObjectGroup) _sim.getReportManager().getGroupsManager().getObject(groupName);
        group.getGroupsManager().groupObjects(groupName, created);
    }

    private void createSumSensReports() {
        MeshOperationPart subtract = (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart("Subtract");
        PartSurface press = (PartSurface) subtract.getPartSurfaceManager().getPartSurface("CSTAirfoil.01_PRESSURE");
        PartSurface suctn = (PartSurface) subtract.getPartSurfaceManager().getPartSurface("CSTAirfoil.01_SUCTION");
        PartSurface te = (PartSurface) subtract.getPartSurfaceManager().getPartSurface("CSTAirfoil.01_TRAILING_EDGE");
        LatestMeshProxyRepresentation latestSurfVol = (LatestMeshProxyRepresentation) _sim.getRepresentationManager().getObject("Latest Surface/Volume");

        String groupName = "Sens Man";
        ClientServerObjectGroup group;
        ArrayList<Report> created = new ArrayList<>();
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                SumReport sr = _sim.getReportManager().createReport(SumReport.class);
                sr.getTagGroup().add(getTagAsTag());
                sr.setPresentationName(getManualSensitivityReportName(r, sgp));
                sr.setFieldFunction(_sim.getFieldFunctionManager().getFunction(getManualLocalSensFFName(r, sgp)));
                sr.getParts().setObjects(press, suctn, te);
                sr.setRepresentation(latestSurfVol);
                created.add(sr);
            }
        }
        if (!_sim.getReportManager().getGroupsManager().has(groupName)) {
            _sim.getReportManager().getGroupsManager().createGroup(groupName);
        }
        group = (ClientServerObjectGroup) _sim.getReportManager().getGroupsManager().getObject(groupName);
        group.getGroupsManager().groupObjects(groupName, created);

        groupName = "Sens Mapped";
        created.clear();
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                SumReport sr = _sim.getReportManager().createReport(SumReport.class);
                sr.getTagGroup().add(getTagAsTag());
                sr.setPresentationName(getMappedSensitivityReportName(r, sgp));
                sr.setFieldFunction(_sim.getFieldFunctionManager().getFunction(getMappedLocalSensFFName(r, sgp)));
                sr.getParts().setObjects(press, suctn, te);
                sr.setRepresentation(latestSurfVol);
                created.add(sr);
            }
        }
        if (!_sim.getReportManager().getGroupsManager().has(groupName)) {
            _sim.getReportManager().getGroupsManager().createGroup(groupName);
        }
        group = (ClientServerObjectGroup) _sim.getReportManager().getGroupsManager().getObject(groupName);
        group.getGroupsManager().groupObjects(groupName, created);
    }

    private void createGSComparisonPlots() {
        XYPlot template = (XYPlot) gsComparisonPlotTemplate;
        for (ScalarGlobalParameter sgp : params) {
            String plotName = getGSComparisonPlotName(sgp);
            if (!_sim.getPlotManager().has(plotName)) {
                XYPlot newPlot = _sim.getPlotManager().create("star.common.XYPlot");
                newPlot.getTagGroup().add(getTagAsTag());
                newPlot.copyProperties(template);
                newPlot.setPresentationName(plotName);
                newPlot.setTitle(getGSComparisonPlotTitle(sgp));
                newPlot.getYAxes().getAxisType("Y Type 2").getScalarFunction().setFieldFunction(getManualVSMesherGSDotFF(sgp));
            }
        }
    }

    private void createLSComparisonPlots() {
        XYPlot template = (XYPlot) lsComparisonPlotTemplate;
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                String plotName = getLSComparisonPlotName(r, sgp);
                if (!_sim.getPlotManager().has(plotName)) {
                    XYPlot newPlot = _sim.getPlotManager().create("star.common.XYPlot");
                    newPlot.getTagGroup().add(getTagAsTag());
                    newPlot.copyProperties(template);
                    newPlot.setPresentationName(plotName);
                    newPlot.setTitle(getLSComparisonPlotTitle(r, sgp));
                    newPlot.getYAxes().getAxisType("Y Type 2").getScalarFunction().setFieldFunction(_sim.getFieldFunctionManager().getFunction(getManualLocalSensFFName(r, sgp)));
                    newPlot.getYAxes().getAxisType("Y Type 3").getScalarFunction().setFieldFunction(_sim.getFieldFunctionManager().getFunction(getMappedLocalSensFFName(r, sgp)));
                    newPlot.getAxisManager().getAxis("Right Axis").getTitle().setText(getLSComparisonPlotRightAxisTitle(r, sgp));
                }
            }
        }
    }
    
    private String getLSComparisonPlotRightAxisTitle(Report r, ScalarGlobalParameter sgp) {
        return "dot(Sens " + getReportNameForFFName(r) + "::Position, GS " + getParamNameForFFName(sgp) + ")";
    }

    private String getLSComparisonPlotName(Report r, ScalarGlobalParameter sgp) {
        return "Manual_VS_Mapped_" + getReportNameForFFName(r) + "_" + getParamNameForFFName(sgp) + "_Sensitivity";
    }

    private String getLSComparisonPlotTitle(Report r, ScalarGlobalParameter sgp) {
        return "Geometry VS Local Sens " + getReportNameForFFName(r) + " w.r.t " + getParamNameForFFName(sgp);
    }

    private String getGSComparisonPlotName(ScalarGlobalParameter sgp) {
        return "GS_Manual_VS_Mesh_Angle_" + getParamNameForFFName(sgp);
    }

    private String getGSComparisonPlotTitle(ScalarGlobalParameter sgp) {
        return "Dot Product of Manual and Mesher Geometric Sensitivities: " + getParamNameForFFName(sgp);
    }

    private String getManualSensitivityReportName(Report r, ScalarGlobalParameter sgp) {
        String rName = getReportNameForFFName(r);
        String pName = getParamNameForFFName(sgp);
        String name = getExpressionSensReportName(r, sgp) + " ManGS";
        return name;
    }

    private String getMappedSensitivityReportName(Report r, ScalarGlobalParameter sgp) {
        String rName = getReportNameForFFName(r);
        String pName = getParamNameForFFName(sgp);
        String name = getExpressionSensReportName(r, sgp) + " MappedGS";
        return name;
    }

    private String getExpressionSensReportName(Report r, ScalarGlobalParameter sgp) {
        String rName = getReportNameForFFName(r);
        String pName = getParamNameForFFName(sgp);
        String name = "Sens " + rName + " w.r.t " + pName;
        return name;
    }

    private String getExpressionSensReportDefinition(Report r, ScalarGlobalParameter sgp) {
        String rExp = "${" + r.getPresentationName() + "}";
        String pExp = "${" + sgp.getPresentationName() + "}";
        String def = "sensitivity(" + rExp + ", " + pExp + ")";
        return def;
    }

    private String getUnfilteredSensitivityFFName(Report r) {
        if (r.getPresentationName().contains("Area")) {
            return "$${Adjoint1::Unfiltered Surface Sensitivity}";
        } else if (r.getPresentationName().contains("over")) {
            return "$${Adjoint2::Unfiltered Surface Sensitivity}";
        } else if (r.getPresentationName().contains("Cd")) {
            return "$${Adjoint3::Unfiltered Surface Sensitivity}";
        } else {
            return "$${Adjoint4::Unfiltered Surface Sensitivity}";
        }
    }

    private String getMappedGSFFName(ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String ffName = "Mapped_GS_" + pName;
        return ffName;
    }

    private String getManualGSFFName(ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String ffName = "GS_" + pName + "_Man";
        return ffName;
    }

    private String getMappedLocalSensFFName(Report r, ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String rName = getReportNameForFFName(r);
        String name = "Mapped_Local_Sensitivity_" + rName + "_" + pName;
        return name;
    }

    private String getManualLocalSensFFName(Report r, ScalarGlobalParameter sgp) {
        String pName = getParamNameForFFName(sgp);
        String rName = getReportNameForFFName(r);
        String name = "Manual_Local_Sensitivity_" + rName + "_" + pName;
        return name;
    }

    private UserFieldFunction getManualVSMesherGSDotFF(ScalarGlobalParameter sgp) {
        String suffix = sgp.getPresentationName().contains("TE") ? "_Man_VS_Mesh_Dot" : "_VS_Mesh_Dot";
        String name = "GS_" + getParamNameForFFName(sgp) + suffix;
        return (UserFieldFunction) _sim.getFieldFunctionManager().getFunction(name);
    }

    private String getParamNameForFFName(ScalarGlobalParameter sgp) {
        return sgp.getPresentationName().contains("TE") ? "TE" : sgp.getPresentationName().replace("P_W_", "");
    }

    private String getReportNameForFFName(Report r) {
        return r.getPresentationName().contains("Area") ? "Area" : r.getPresentationName();
    }

    private String getGeomSensMappedFFDefinition(ScalarGlobalParameter sgp) {
        String[] components = new String[]{"i", "j", "k"};
        String pName = sgp.getPresentationName();
        String prefix = "${MappedVertexSensitivity of Position w.r.t ";
        String ff = "[" + prefix + pName + "[" + components[0] + "]}";
        for (int i = 1; i <= 2; i++) {
            ff += ",\n" + prefix + pName + "[" + components[i] + "]}";
        }
        ff += "]";
        return ff;
    }

    private String getMappedLocalSensFFDefinition(Report r, ScalarGlobalParameter sgp) {
        String def = "dot(";
        def += getUnfilteredSensitivityFFName(r);
        def += ", ";
        def += "$${" + getMappedGSFFName(sgp) + "})";
        return def;
    }

    private String getManualLocalSensFFDefinition(Report r, ScalarGlobalParameter sgp) {
        String def = "dot(";
        def += getUnfilteredSensitivityFFName(r);
        def += ", ";
        def += "$${" + getManualGSFFName(sgp) + "})";
        return def;
    }

    private Collection<Tag> getTag() {
        Collection<Tag> tagCollection = new ArrayList<>();
        for (Tag ti : _sim.get(TagManager.class).getObjects()) {
            if (ti.getPresentationName().equals(tag_name)) {
                tagCollection = new ArrayList<>();
                tagCollection.add(ti);
                return tagCollection;
            }
        }

        Tag createdBytag = _sim.get(TagManager.class).createNewUserTag(tag_name);
        tagCollection.add(createdBytag);
        return tagCollection;
    }

    private Tag getTagAsTag() {
        return getTag().toArray(new Tag[1])[0];
    }

    private void deleteTaggedObjects() {
        int maxTries = 10;
        int count = 0;
        if (clean_reports) {
            while (count < maxTries) {
                for (Report r : _sim.getReportManager().getObjects()) {
                    if (r.getTagGroup().getObjects().contains(getTagAsTag())) {
                        _sim.deleteObject(r);
                    }
                }
                count++;
            }
        }

        count = 0;
        if (clean_plots) {
            while (count < maxTries) {
                for (StarPlot sp : _sim.getPlotManager().getObjects()) {
                    if (sp.getTagGroup().getObjects().contains(getTagAsTag())) {
                        _sim.deleteObject(sp);
                    }
                }
                count++;
            }
        }
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }
}
