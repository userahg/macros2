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
import star.base.neo.ClientServerObject;
import star.base.report.ExpressionReport;
import star.base.report.Report;
import star.base.report.SumReport;
import star.common.GlobalParameterManager;
import star.common.PartSurface;
import star.common.Representation;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;
import star.common.StarPlot;
import star.common.Tag;
import star.common.TagManager;
import star.common.UserFieldFunction;
import star.common.XYPlot;
import star.meshing.MeshOperationPart;

/**
 *
 * @author cd8unu
 */
public class MakeSensitivityReportsCST extends StarMacro {
    
    boolean clean_up = false;
    String[] reportNames = new String[] {"Cl", "Cd", "Area_Constraint", "L_over_D"};
    String[] paramNames = new String[] {"P_trailingEdge_thickness", "P_W_L0", "P_W_L1", "P_W_L2", "P_W_L3", "P_W_L4", "P_W_L5", "P_W_L6", "P_W_L7", "P_W_L8", "P_W_L9", "P_W_L10", "P_W_U0", "P_W_U1", "P_W_U2", "P_W_U3", "P_W_U4", "P_W_U5", "P_W_U6", "P_W_U7", "P_W_U8", "P_W_U9", "P_W_U10"};
        
    Simulation _sim;
    
    Report[] reports;
    ScalarGlobalParameter[] params;
    TagManager tagManager;
    private final String tag_name = "MSRJAVA";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        
        try {
            initialize();
            if (clean_up) {
                deleteTaggedObjects();
            }
            //createExpressionSensitivityReports();
            //createSumSensitivityReports();
            createMeshAnglePlots();
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
            } catch(Exception ex) {
                _sim.println("Parameter " + s + " not found!!! Cannot continue.");
                
            }
        }
        params = paramList.toArray(new ScalarGlobalParameter[paramList.size()]);
    }
    
    private void createExpressionSensitivityReports() {
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                _sim.println("Making expression report for:\n\t" + r.getPresentationName() + "\n\t" + sgp.getPresentationName());
                String name = getExpressionSensitivityReportName(r, sgp);
                if (!_sim.getReportManager().has(name)) {
                    ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
                    tagManager.addTags(er, getTag());
                    er.setPresentationName(name);
                    er.setDefinition(getExpression(r, sgp));
                }
            }
        }
    }
    
    private void createSumSensitivityReports() {
        PartSurface[] partSurfaces = getPartSurfaces();
        Representation rep = _sim.getRepresentationManager().getObject("Latest Surface/Volume");
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                _sim.println("Making manual summed report for:\n\t" + r.getPresentationName() + "\n\t" + sgp.getPresentationName());
                String manName = getSumManualSensitivityReportName(r, sgp);
                if (!_sim.getReportManager().has(manName)) {
                    SumReport sr = _sim.getReportManager().createReport(SumReport.class);
                    tagManager.addTags(sr, getTag());
                    sr.setPresentationName(manName);
                    sr.setFieldFunction(getManualSensitivityFF(r, sgp));
                    for (PartSurface ps : partSurfaces) {
                        sr.getParts().addPart(ps);
                    }
                    sr.setRepresentation(rep);
                }
            }
        }
        
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                _sim.println("Making mapped summed report for:\n\t" + r.getPresentationName() + "\n\t" + sgp.getPresentationName());
                String manName = getSumMappedSensitivityReportName(r, sgp);
                if (!_sim.getReportManager().has(manName)) {
                    SumReport sr = _sim.getReportManager().createReport(SumReport.class);
                    tagManager.addTags(sr, getTag());
                    sr.setPresentationName(manName);
                    sr.setFieldFunction(getMappedSensitivityFF(r, sgp));
                    for (PartSurface ps : partSurfaces) {
                        sr.getParts().addPart(ps);
                    }
                    sr.setRepresentation(rep);
                }
            }
        }
    }
    
    private void createMeshAnglePlots() {
        XYPlot template = (XYPlot) _sim.getPlotManager().getPlot("GS_Manual_VS_Mesh_Angle_0");
        for (ScalarGlobalParameter sgp : params) {
            String plotName = getMeshAnglePlotName(sgp);
            if (!_sim.getPlotManager().has(plotName)) {
                XYPlot newPlot = _sim.getPlotManager().create("star.common.XYPlot");
                tagManager.addTags(newPlot, getTag());
                newPlot.copyProperties(template);
                newPlot.setPresentationName(plotName);
                newPlot.setTitle(getMeshAnglePlotTitle(sgp));
                newPlot.getYAxes().getAxisType("Y Type 2").getScalarFunction().setFieldFunction(getMeshAngleFF(sgp));
            }            
        }
    }
    
    private void createManualVSMeshLocalSensitivityPlots() {
        XYPlot template = (XYPlot) _sim.getPlotManager().getPlot("Manual_VS_Mapped_Area_0_Sensitivity");
        for (Report r : reports) {
            for (ScalarGlobalParameter sgp : params) {
                String plotName = getManualVsMeshLocalSensitivityPlotName(r, sgp);
                if (!_sim.getPlotManager().has(plotName)) {
                    XYPlot newPlot = _sim.getPlotManager().create("star.common.XYPlot");
                    tagManager.addTags(newPlot, getTag());
                    newPlot.copyProperties(template);
                    newPlot.setPresentationName(plotName);
                    newPlot.setTitle(getManualVsMeshLocalSensitivityPlotTitle(r, sgp));
                    newPlot.getYAxes().getAxisType("Y Type 2").getScalarFunction().setFieldFunction(getManualLocalSensFF(r, sgp));
                    newPlot.getYAxes().getAxisType("Y Type 3").getScalarFunction().setFieldFunction(getMappedLocalSensFF(r, sgp));
                }
            }
        }
    }
    
    private String getExpressionSensitivityReportName(Report r, ScalarGlobalParameter sgp) {
        String rName = getReportName(r);
        String pName = getParamName(sgp);
        String s = "Sens ";
        s += rName;
        s += " w.r.t. ";
        s += pName;
        return s;
    }
    
    private String getSumManualSensitivityReportName(Report r, ScalarGlobalParameter sgp) {
        String reportS = getReportName(r);
        String paramS = getParamName(sgp);
        String s = "Sens " + reportS + " w.r.t. ";
        s += paramS;
        s += " ManGS";
        return s;
    }
    
    private String getSumMappedSensitivityReportName(Report r, ScalarGlobalParameter sgp) {
        String reportS = getReportName(r);
        String paramS = getParamName(sgp);
        String s = "Sens " + reportS + " w.r.t. ";
        s += paramS;
        s += " MappedGS";
        return s;
    }
    
    private String getMeshAnglePlotName(ScalarGlobalParameter sgp) {
        String paramName = sgp.getPresentationName().contains("_trailingEdge") ? "P_TE" : sgp.getPresentationName();
        String[] nameSplit = paramName.split("_");
        paramName = nameSplit[nameSplit.length - 1];
        String plotName = "GS_Manual_VS_Mesh_Angle_" + paramName;
        return plotName;
    }
    
    private String getManualVsMeshLocalSensitivityPlotName(Report r, ScalarGlobalParameter sgp) {
        String reportName = r.getPresentationName().contains("Area") ? "Area" : r.getPresentationName();
        String paramName = sgp.getPresentationName().contains("_trailingEdge") ? "P_TE" : sgp.getPresentationName();
        String[] nameSplit = paramName.split("_");
        paramName = nameSplit[nameSplit.length - 1];
        String plotName = "Manual_VS_Mapped_" + reportName + "_" + paramName + "_Sensitivity";
        return plotName;
    }
    
    private String getMeshAnglePlotTitle(ScalarGlobalParameter sgp) {
        String paramName = sgp.getPresentationName().contains("_trailingEdge") ? "P_TE" : sgp.getPresentationName().replace("P_", "");
        String title = "Angle Between Manual and Mesher Geometric Sensitivity " + paramName;
        return title;
    }
    
    private String getManualVsMeshLocalSensitivityPlotTitle(Report r, ScalarGlobalParameter sgp) {
        String reportName = r.getPresentationName().contains("Area") ? "Area" : r.getPresentationName();
        String paramName = sgp.getPresentationName().contains("_trailingEdge") ? "P_TE" : sgp.getPresentationName().replace("P_", "");
        String title = "Manual Vs Mapped Local Sens " + reportName + " w.r.t. " + paramName;
        return title;
    }
    
    private UserFieldFunction getManualSensitivityFF(Report r, ScalarGlobalParameter sgp) {
        String rName = getReportName(r);
        String pName = getParamName(sgp);
        String name = "Sens_" + rName + "_" + pName + "_Manual";
        UserFieldFunction ff = (UserFieldFunction) _sim.getFieldFunctionManager().getFunction(name);
        return ff;
    }
    
    private UserFieldFunction getMappedSensitivityFF(Report r, ScalarGlobalParameter sgp) {
        String rName = getReportName(r);
        String pName = getParamName(sgp);
        String name = "Local_Sensitivity_" + rName + "_" + pName;
        UserFieldFunction ff = (UserFieldFunction) _sim.getFieldFunctionManager().getFunction(name);
        return ff;
    }
    
    private UserFieldFunction getMeshAngleFF(ScalarGlobalParameter sgp) {
        String paramName = sgp.getPresentationName().contains("TE") ? "P_TE" : sgp.getPresentationName();
        String ffName = "GS_" + paramName + "_Man_VS_Mesh_Angle";
        UserFieldFunction ff = (UserFieldFunction) _sim.getFieldFunctionManager().getFunction(ffName);
        return ff;
    }
    
    private UserFieldFunction getManualLocalSensFF(Report r, ScalarGlobalParameter sgp) {
        String reportName = r.getPresentationName().contains("Area") ? "Area" : r.getPresentationName();
        String paramName = sgp.getPresentationName().contains("TE") ? "TE" : sgp.getPresentationName();
        String ffName = "Sens_" + reportName + "_" + paramName + "_Manual";
        UserFieldFunction ff = (UserFieldFunction) _sim.getFieldFunctionManager().getFunction(ffName);
        return ff;
    }
    
    private UserFieldFunction getMappedLocalSensFF(Report r, ScalarGlobalParameter sgp) {
        String reportName = r.getPresentationName().contains("Area") ? "Area" : r.getPresentationName();
        String paramName = sgp.getPresentationName().contains("TE") ? "TE" : sgp.getPresentationName();
        String ffName = "Local_Sensitivity_" + reportName + "_" + paramName;
        UserFieldFunction ff = (UserFieldFunction) _sim.getFieldFunctionManager().getFunction(ffName);
        return ff;
    }
    
    private PartSurface[] getPartSurfaces() {
        MeshOperationPart meshOpPart = (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart("Subtract");
        PartSurface ps0 = ((PartSurface) meshOpPart.getPartSurfaceManager().getPartSurface("SVDAirfoil.01_LIFTING_SURFACE"));
        PartSurface ps1 = ((PartSurface) meshOpPart.getPartSurfaceManager().getPartSurface("SVDAirfoil.01_TRAILING_EDGE"));
        ArrayList<PartSurface> partSurfaces = new ArrayList<>();
        partSurfaces.add(ps0);
        partSurfaces.add(ps1);
        return partSurfaces.toArray(new PartSurface[partSurfaces.size()]);
    }
    
    private String getExpression(Report r, ScalarGlobalParameter sgp) {
        String s = "sensitivity(${";
        s += r.getPresentationName();
        s += "}, ${";
        s += sgp.getPresentationName();
        s += "})";
        return s;        
    }
    
    private String getReportName(Report r) {
        return r.getPresentationName().equals("Area_Constraint") ? "Area" : r.getPresentationName();
    }
    
    private String getParamName(ScalarGlobalParameter sgp) {
        return sgp.getPresentationName().equals("P_trailingEdge_thickness") ? "TE" : sgp.getPresentationName().replace("W_", "");
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
        Collection<ClientServerObject> toDelete = new ArrayList<>();
        for (Report r : _sim.getReportManager().getObjects()) {
            if (r.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDelete.add(r);
            }
        }

        for (StarPlot sp : _sim.getPlotManager().getObjects()) {
            if (sp.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDelete.add(sp);
            }
        }
        _sim.deleteObjects(toDelete);
    }
    
    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw);
    }    
}
