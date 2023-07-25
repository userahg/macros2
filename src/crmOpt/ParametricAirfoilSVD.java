// Simcenter STAR-CCM+ macro: parametric_svd_spine.java
// Written by Simcenter STAR-CCM+ 18.02.008
package crmOpt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import star.common.*;
import star.base.neo.*;
import star.base.report.ElementCountReport;
import star.base.report.ExpressionReport;
import star.base.report.MonitorDataSet;
import star.base.report.ReportMonitor;
import star.base.report.SurfaceIntegralReport;
import star.cadmodeler.*;
import star.coupledflow.AdjointFlowModel;
import star.coupledflow.CoupledEnergyModel;
import star.coupledflow.CoupledFlowModel;
import star.energy.ConstantSpecificHeat;
import star.energy.MinimumAllowableTemperature;
import star.energy.SpecificHeatProperty;
import star.energy.StaticTemperatureProfile;
import star.energy.SutherlandLaw;
import star.energy.ThermalConductivityProperty;
import star.flow.DynamicViscosityProperty;
import star.flow.FlowDirectionProfile;
import star.flow.ForceCoefficientReport;
import star.flow.ForceReport;
import star.flow.IdealGasModel;
import star.flow.MachNumberProfile;
import star.flow.MinimumAllowableAbsolutePressure;
import star.flow.PressureCoefficientFunction;
import star.flow.ReferencePressure;
import star.flow.SkinFrictionCoefficientFunction;
import star.flow.VelocityProfile;
import star.mapping.SolutionInterpolationModel;
import star.material.Gas;
import star.material.SingleComponentGasModel;
import star.meshing.*;
import star.metrics.ThreeDimensionalModel;
import star.metrics.TwoDimensionalModel;
import star.morpher.SurfaceSensitivityModel;
import star.prismmesher.*;
import star.resurfacer.ResurfacerAutoMesher;
import star.resurfacer.SurfaceGrowthRate;
import star.resurfacer.VolumeControlResurfacerSizeOption;
import star.saturb.SaAllYplusWallTreatment;
import star.saturb.SaTurbModel;
import star.saturb.SpalartAllmarasTurbulence;
import star.sweptmesher.*;
import star.twodmesher.DualAutoMesher2d;
import star.turbulence.AdjointFrozenTurbulenceModel;
import star.turbulence.RansTurbulenceModel;
import star.turbulence.TurbulentModel;
import star.turbulence.TurbulentViscosityRatioProfile;
import star.vis.AspectRatioEnum;
import star.vis.ClipMode;
import star.vis.GlyphSettings;
import star.vis.Legend;
import star.vis.LookupTableManager;
import star.vis.PartDisplayer;
import star.vis.PredefinedLookupTable;
import star.vis.ScalarDisplayer;
import star.vis.Scene;
import star.vis.VectorDisplayer;
import star.vis.VectorStyle;

public class ParametricAirfoilSVD extends StarMacro {

    Type type = Type.TWO_D;
    int splineSampleRatio = 10;
    String fs = File.separator;
    String tableBaseDir = "D:" + fs + "PycharmProjects" + fs + "siemens" + fs + "projects" + fs + "parameterization";
    String modesTablePath = tableBaseDir + fs + "svd_1_modes.csv";
    String zBarTablePath = tableBaseDir + fs + "svd_1_zbar_T.csv";
    String xCoordTablePath = tableBaseDir + fs + "common_x_coord.csv";

    Simulation _sim;
    double te_thickness = 0.002940088960013056;
    double[] weights = new double[]{1.33078230e-01, 8.51496774e-02, -9.48804267e-02, 3.18459896e-02,
        2.67566604e-02, 3.03834756e-02, 1.98453140e-02, -6.09390903e-03,
        1.25572861e-02, 1.15049696e-02, 4.94917018e-04, -1.00655629e-02,
        3.42290138e-03, -4.42431652e-04, 7.39350647e-03, 5.61439102e-03,
        -2.64909703e-04, 9.43406370e-06, 7.12068421e-04, 2.54300471e-04,
        -1.96432640e-03};
    String airfoilCADName = "ParametricAirfoil";
    String airfoilBodyName = "SVDAirfoil";
    String domainCADName = "Domain";
    String refinementsCADName = "Refinements";
    String chordParamName = "C_Chord";
    String spanParamName = "C_Span";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        createPhysics();
        importTables();
        createGlobalParams();
        drawAirfoil(true);
        drawDomain();
        drawRefinements();
        createRegion();
        createFieldFunctions();
        createReportsAndMonitors();
        createPlots();
        createScenes();
    }

    private void createPhysics() {
        String continuumName = "Physics - External Aero";
        PhysicsContinuum physics = null;

        for (PhysicsContinuum continuum : _sim.getContinuumManager().getObjectsOf(PhysicsContinuum.class)) {
            if (continuum.getPresentationName().equals("Physics 1")) {
                physics = continuum;
                physics.setPresentationName(continuumName);
            }
        }

        if (physics == null) {
            physics = (PhysicsContinuum) _sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
            physics.setPresentationName(continuumName);
        }

        physics.enable(SteadyModel.class);
        physics.enable(SingleComponentGasModel.class);
        physics.enable(CoupledFlowModel.class);
        physics.enable(IdealGasModel.class);
        physics.enable(CoupledEnergyModel.class);
        physics.enable(TurbulentModel.class);
        physics.enable(RansTurbulenceModel.class);
        physics.enable(SpalartAllmarasTurbulence.class);
        physics.enable(SaTurbModel.class);
        physics.enable(SaAllYplusWallTreatment.class);
        physics.enable(SolutionInterpolationModel.class);

        if (type == Type.TWO_D) {
            physics.enable(TwoDimensionalModel.class);
        } else {
            physics.enable(ThreeDimensionalModel.class);
            physics.enable(AdjointModel.class);
            physics.enable(AdjointFlowModel.class);
            physics.enable(AdjointFrozenTurbulenceModel.class);
            physics.enable(SurfaceSensitivityModel.class);
        }
    }

    private void importTables() {
        _sim.getTableManager().createFromFile(resolvePath(modesTablePath));
        _sim.getTableManager().createFromFile(resolvePath(zBarTablePath));
        _sim.getTableManager().createFromFile(resolvePath(xCoordTablePath));
    }

    private void createGlobalParams() {

        Dimensions length = Dimensions.Builder().length(1).build();
        Dimensions area = Dimensions.Builder().length(2).build();
        Dimensions angle = Dimensions.Builder().angle(1).build();
        Dimensions temp = Dimensions.Builder().temperature(1).build();
        Dimensions velocity = Dimensions.Builder().length(1).time(-1).build();
        Dimensions density = Dimensions.Builder().mass(1).volume(-1).build();
        Dimensions pressure = Dimensions.Builder().pressure(1).build();
        Dimensions pressure_time = Dimensions.Builder().pressure(1).time(1).build();
        Dimensions gas_const = Dimensions.Builder().energy(1).mass(-1).temperature(-1).build();
        Dimensions kappa_dim = Dimensions.Builder().power(1).length(-1).temperature(-1).build();
        Units meters = (Units) _sim.getUnitsManager().getObject("m");
        Units kelvin = (Units) _sim.getUnitsManager().getObject("K");
        Units meters_sq = (Units) _sim.getUnitsManager().getObject("m^2");
        Units meters_per_s = (Units) _sim.getUnitsManager().getObject("m/s");
        Units joules_per_kg_K = (Units) _sim.getUnitsManager().getObject("J/kg-K");
        Units degrees = (Units) _sim.getUnitsManager().getObject("deg");
        Units watts_per_m_K = (Units) _sim.getUnitsManager().getObject("W/m-K");
        Units pressureUnits = _sim.getUnitsManager().getPreferredUnits(Dimensions.Builder().pressure(1).build());

        FileTable x_coord = (FileTable) _sim.getTableManager().getObject("common_x_coord");
        TableData data = x_coord.getSeriesData();
        double[] x_vals = data.getSeries(0);

        GlobalParameterManager paramMan = _sim.get(GlobalParameterManager.class);
        paramMan.getGroupsManager().createGroup("X_values");
        paramMan.getGroupsManager().createGroup("Z_values");
        paramMan.getGroupsManager().createGroup("Params");
        paramMan.getGroupsManager().createGroup("Constants");
        paramMan.getGroupsManager().createGroup("Expressions");
        ClientServerObjectGroup x_group = (ClientServerObjectGroup) paramMan.getGroupsManager().getObject("X_values");
        ClientServerObjectGroup z_group = (ClientServerObjectGroup) paramMan.getGroupsManager().getObject("Z_values");
        ClientServerObjectGroup param_group = (ClientServerObjectGroup) paramMan.getGroupsManager().getObject("Params");
        ClientServerObjectGroup constant_group = (ClientServerObjectGroup) paramMan.getGroupsManager().getObject("Constants");
        ClientServerObjectGroup expressions_group = (ClientServerObjectGroup) paramMan.getGroupsManager().getObject("Expressions");
        String[] weight_vars = new String[weights.length];

        ScalarGlobalParameter thickness = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "P_TE_Thickness");
        thickness.setDimensions(length);
        thickness.getQuantity().setValueAndUnits(te_thickness, meters);
        param_group.getGroupsManager().groupObjects(param_group.getPresentationName(), new NeoObjectVector(new Object[]{thickness}), true);
        ScalarGlobalParameter chord = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, chordParamName);
        chord.setDimensions(length);
        chord.getQuantity().setValueAndUnits(1.0, meters);
        ScalarGlobalParameter span = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, spanParamName);
        span.setDimensions(length);
        if (type == Type.THREE_D_DIRECTED_MESH) {
            span.getQuantity().setDefinitionAndUnits("0.01*${" + chordParamName + "}", meters);
        } else {
            span.getQuantity().setValueAndUnits(1.0, meters);
        }

        constant_group.getGroupsManager().groupObjects(constant_group.getPresentationName(), new NeoObjectVector(new Object[]{chord, span}), true);

        for (int i = 0; i < weights.length; i++) {
            String w_name = "P_W_" + Integer.toString(i);
            ScalarGlobalParameter sgp_w = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, w_name);
            sgp_w.getQuantity().setDefinition(Double.toString(weights[i]));
            param_group.getGroupsManager().groupObjects(param_group.getPresentationName(), new NeoObjectVector(new Object[]{sgp_w}), true);
            weight_vars[i] = w_name;
        }

        for (int i = 0; i < 301; i++) {
            String x_name = "X_" + Integer.toString(i);
            ScalarGlobalParameter sgp_x = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, x_name);
            sgp_x.setDimensions(Dimensions.Builder().length(1).build());
            sgp_x.getQuantity().setUnits(meters);
            sgp_x.getQuantity().setDefinition(Double.toString(x_vals[i]) + " * ${" + chordParamName + "}");
            ScalarGlobalParameter sgp_z = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Z_" + Integer.toString(i));
            sgp_z.setDimensions(Dimensions.Builder().length(1).build());
            sgp_z.getQuantity().setUnits(meters);
            sgp_z.getQuantity().setDefinition(getZExpression(x_name, weight_vars));
            x_group.getGroupsManager().groupObjects(x_group.getPresentationName(), new NeoObjectVector(new Object[]{sgp_x}), true);
            z_group.getGroupsManager().groupObjects(z_group.getPresentationName(), new NeoObjectVector(new Object[]{sgp_z}), true);
        }

        ScalarGlobalParameter gamma = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Gamma");
        gamma.getQuantity().setValue(1.4);
        ScalarGlobalParameter rGas = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_R_Gas");
        rGas.setDimensions(gas_const);
        rGas.getQuantity().setValueAndUnits(287.03807860141404, joules_per_kg_K);
        ScalarGlobalParameter alpha = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Alpha");
        alpha.setDimensions(angle);
        alpha.getQuantity().setValueAndUnits(6.0, degrees);
        ScalarGlobalParameter altitude = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Altitude");
        altitude.setDimensions(length);
        altitude.getQuantity().setValueAndUnits(3000.0, meters);
        ScalarGlobalParameter cpRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Cp_Ref");
        cpRef.setDimensions(gas_const);
        cpRef.getQuantity().setValueAndUnits(1004.6332751049492, joules_per_kg_K);
        ScalarGlobalParameter kappa = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Kappa_Ref");
        kappa.setDimensions(kappa_dim);
        kappa.getQuantity().setValueAndUnits(0.06660908599302123, watts_per_m_K);
        ScalarGlobalParameter l_ref = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_L_Ref");
        l_ref.setDimensions(length);
        l_ref.getQuantity().setValueAndUnits(1.0, meters);
        ScalarGlobalParameter mach = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Ma");
        mach.getQuantity().setValue(0.4);
        ScalarGlobalParameter pr = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_Pr");
        pr.getQuantity().setValue(0.71);
        ScalarGlobalParameter s_ref = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "C_S_Ref");
        s_ref.setDimensions(area);
        s_ref.getQuantity().setValueAndUnits(1.0, meters_sq);

        NeoObjectVector const_group = new NeoObjectVector(new Object[]{gamma, rGas, alpha, altitude, cpRef, kappa, l_ref, mach, pr, s_ref});
        constant_group.getGroupsManager().groupObjects(constant_group.getPresentationName(), const_group, true);

        ScalarGlobalParameter tRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_T_Ref");
        tRef.setDimensions(temp);
        tRef.getQuantity().setDefinition("(${C_Altitude}<11000) ? 288.15 - 0.0065 * ${C_Altitude} : 216.65");
        ScalarGlobalParameter pRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_P_Ref");
        pRef.setDimensions(pressure);
        pRef.getQuantity().setDefinition("(${C_Altitude} < 11000) ? 101290.0 * pow(${Q_T_Ref}/288.08, 5.256) : 22632.1 * exp(1.727 - 0.000157 * ${C_Altitude})");
        ScalarGlobalParameter aRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_A_Ref");
        aRef.setDimensions(velocity);
        aRef.getQuantity().setDefinitionAndUnits("sqrt(${C_Gamma}*${C_R_Gas}*${Q_T_Ref})", meters_per_s);
        ScalarGlobalParameter uRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_U_Ref");
        uRef.setDimensions(velocity);
        uRef.getQuantity().setDefinition("${C_Ma}*${Q_A_Ref}");
        ScalarGlobalParameter rhoRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_Rho_Ref");
        rhoRef.setDimensions(density);
        rhoRef.getQuantity().setDefinition("${Q_P_Ref} / (${C_R_Gas} * ${Q_T_Ref})");
        ScalarGlobalParameter muRef = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_Mu_Ref");
        muRef.setDimensions(pressure_time);
        muRef.getQuantity().setDefinition("1.716e-5 * pow(${Q_T_Ref} / 273.15, 1.5) * (273.15 + 110.4) / (${Q_T_Ref} + 110.4)");
        ScalarGlobalParameter re = (ScalarGlobalParameter) paramMan.createGlobalParameter(ScalarGlobalParameter.class, "Q_Re");
        re.getQuantity().setDefinition("${Q_Rho_Ref} * ${Q_U_Ref} * ${C_L_Ref} / ${Q_Mu_Ref}");

        NeoObjectVector exp_group = new NeoObjectVector(new Object[]{tRef, pRef, aRef, uRef, rhoRef, muRef, re});
        expressions_group.getGroupsManager().groupObjects(expressions_group.getPresentationName(), exp_group, true);

        PhysicsContinuum physics = (PhysicsContinuum) _sim.getContinuumManager().getContinuum("Physics - External Aero");
        SingleComponentGasModel gasModel = physics.getModelManager().getModel(SingleComponentGasModel.class);
        Gas air = (Gas) gasModel.getMaterial();
        air.getMaterialProperties().getMaterialProperty(DynamicViscosityProperty.class).setMethod(SutherlandLaw.class);
        ConstantSpecificHeat constantCp = (ConstantSpecificHeat) air.getMaterialProperties().getMaterialProperty(SpecificHeatProperty.class).getMethod();
        constantCp.getQuantity().setDefinition("${C_Cp_Ref}");
        air.getMaterialProperties().getMaterialProperty(ThermalConductivityProperty.class).setMethod(SutherlandLaw.class);

        physics.getReferenceValues().get(ReferencePressure.class).setDefinition("${Q_P_Ref}");

        physics.getReferenceValues().get(MinimumAllowableTemperature.class).setValueAndUnits(10.0, kelvin);
        physics.getReferenceValues().get(MinimumAllowableAbsolutePressure.class).setValueAndUnits(100.0, pressureUnits);
        StaticTemperatureProfile staticTempProfile = physics.getInitialConditions().get(StaticTemperatureProfile.class);
        staticTempProfile.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Q_T_Ref}");
        TurbulentViscosityRatioProfile turbulentViscosityRatioProf = physics.getInitialConditions().get(TurbulentViscosityRatioProfile.class);
        turbulentViscosityRatioProf.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(0.21043825715);
        VelocityProfile velocityProf = physics.getInitialConditions().get(VelocityProfile.class);
        velocityProf.setMethod(CompositeVectorProfileMethod.class);
        ScalarProfile xComponent = velocityProf.getMethod(CompositeVectorProfileMethod.class).getProfile(0);
        ScalarProfile yComponent = velocityProf.getMethod(CompositeVectorProfileMethod.class).getProfile(1);
        xComponent.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Q_U_Ref} * cos(${C_Alpha})");
        yComponent.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Q_U_Ref} * sin(${C_Alpha})");
    }

    private void drawAirfoil(boolean constrain) {
        CadModel cad = getCADModel(airfoilCADName);

        CanonicalSketchPlane profileSketch = (CanonicalSketchPlane) cad.getFeature("XY");
        Sketch sketch = cad.getFeatureManager().createSketch(profileSketch);
        sketch.setPresentationName("Profile");
        cad.allowMakingPartDirty(false);
        cad.getFeatureManager().startSketchEdit(sketch);

        PointSketchPrimitive horizontalDatumPoint = sketch.createPoint(new DoubleVector(new double[]{-1.0, 0.0}));
        PointSketchPrimitive verticalDatumPoint = sketch.createPoint(new DoubleVector(new double[]{0.0, -1.0}));
        sketch.createFixationConstraint(horizontalDatumPoint);
        sketch.createFixationConstraint(verticalDatumPoint);

        double[] coord = initPositions(splineSampleRatio);
        String[] xNames = xParamNames(splineSampleRatio);
        String[] zNames = zParamNames(splineSampleRatio);

        int counter = 0;
        for (int i = 0; i < coord.length; i += 2) {
            String x = Double.toString(coord[i]);
            String z = Double.toString(coord[i + 1]);
            _sim.println("Point " + xNames[counter] + ": [" + x + ", " + z + "]");
            counter++;
        }

        SplineSketchPrimitive splineProfile = sketch.createSpline(false, null, false, null, new DoubleVector(coord));

        Collection<PointSketchPrimitive> splinePoints = splineProfile.getPoints();

        _sim.println("Spline has " + splinePoints.size() + " points.");

        if (constrain) {
            counter = 0;
            for (PointSketchPrimitive p : splinePoints) {
                String h_exp = "1.0 + ${" + xNames[counter] + "}";
                String v_exp = "1.0 + ${" + zNames[counter] + "}";
                sketch.createHorizontalDistanceDimension(horizontalDatumPoint, p, h_exp, true);
                sketch.createVerticalDistanceDimension(verticalDatumPoint, p, v_exp, true);
                counter++;
            }
        }

        PointSketchPrimitive start = splineProfile.getStartPoint();
        PointSketchPrimitive end = splineProfile.getEndPoint();
        double s_x = start.getPoint().getComponent(0);
        double s_y = start.getPoint().getComponent(1);
        double e_x = end.getPoint().getComponent(0);
        double e_y = end.getPoint().getComponent(1);

        SplineSketchPrimitive teSpline = sketch.createSpline(true, start, true, end, new DoubleVector(new double[]{s_x, s_y, e_x, e_y}));
        cad.getFeatureManager().stopSketchEdit(sketch);

        ExtrusionMerge extrude = cad.getFeatureManager().createExtrusionMerge(sketch);
        extrude.setPresentationName("Extrude");
        extrude.setDirectionOption(0);
        extrude.setExtrudedBodyTypeOption(0);
        extrude.setDistanceOption(2);
        extrude.getDistance().setDefinition("0.2*${" + spanParamName + "}");
        extrude.getDistanceAsymmetric().setDefinition("1.2*${" + spanParamName + "}");
        cad.getFeatureManager().execute(extrude);

        Face rootFace = (Face) extrude.getEndCapFace(splineProfile);
        Face liftingFace = (Face) extrude.getSideFace(splineProfile, "True");
        Face teFace = (Face) extrude.getSideFace(teSpline, "True");
        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{rootFace}), "01_ROOT", false);
        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{teFace}), "01_TRAILING_EDGE", false);
        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{liftingFace}), "01_LIFTING_SURFACE", false);

        Body airfoilBody = (Body) extrude.getBody(splineProfile);
        airfoilBody.setPresentationName(airfoilBodyName);

        if (type != Type.TWO_D) {
            cad.setUpdateMethod(UpdateMethodEnum.SYNCHRONIZE_PARTS);
        }
    }

    private void drawDomain() {
        CadModel cad = getCADModel(domainCADName);
        CanonicalSketchPlane sketchPlane = (CanonicalSketchPlane) cad.getFeature("XY");
        Sketch sketch = cad.getFeatureManager().createSketch(sketchPlane);
        cad.getFeatureManager().startSketchEdit(sketch);
        Units meters = _sim.getUnitsManager().getPreferredUnits(Dimensions.Builder().length(1).build());
        LineSketchPrimitive line = sketch.createLine(new DoubleVector(new double[]{0.0, 0.0}), new DoubleVector(new double[]{0.14, 0.0}));
        sketch.createHorizontalConstraint(line);
        PointSketchPrimitive origin = line.getStartPoint();
        PointSketchPrimitive quarterChord = line.getEndPoint();
        sketch.getProjectedSketchPrimitives(new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{origin}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}));
        FixationConstraint fixOrigin = sketch.createFixationConstraint(origin);
        sketch.getProjectedSketchPrimitives(new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{line}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}));
        LengthDimension lengthDim = sketch.createLengthDimension(line, "0.25*${" + chordParamName + "}");
        sketch.setConstructionState(new NeoObjectVector(new Object[]{line}), true);
        CircleSketchPrimitive circle = sketch.createCircle(quarterChord, 200.0);
        RadiusDimension radiusDim = sketch.createRadiusDimension(circle, 200.0, meters);
        cad.getFeatureManager().stopSketchEdit(sketch, true);
        cad.getFeatureManager().updateModelAfterFeatureEdited(sketch, null);

        ExtrusionMerge extrude = cad.getFeatureManager().createExtrusionMerge(sketch);
        extrude.setDirectionOption(1);
        extrude.getDistance().setDefinition("${" + spanParamName + "}");
        cad.getFeatureManager().execute(extrude);
        sketch.setPresentationName("Domain");
        extrude.setPresentationName("Extrude");
        Body domainBody = extrude.getBody(circle);
        domainBody.setPresentationName(domainCADName);
        Face rootFace = (Face) extrude.getStartCapFace(circle);
        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{rootFace}), "01_ROOT", false);
        Face tipFace = (Face) extrude.getEndCapFace(circle);
        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{tipFace}), "01_TIP", false);
        Face freestreamFace = (Face) extrude.getSideFace(circle, "True");
        cad.setFaceNameAttributes(new NeoObjectVector(new Object[]{freestreamFace}), "00_FREESTREAM", false);
    }

    private void drawRefinements() {
        CadModel cad = getCADModel(refinementsCADName);
        CanonicalSketchPlane sketchPlane = (CanonicalSketchPlane) cad.getFeature("XY");
        Sketch sketch = cad.getFeatureManager().createSketch(sketchPlane);
        cad.getFeatureManager().startSketchEdit(sketch);
        LineSketchPrimitive line = sketch.createLine(new DoubleVector(new double[]{0.0, 0.0}), new DoubleVector(new double[]{0.14, 0.0}));
        sketch.createHorizontalConstraint(line);
        PointSketchPrimitive origin = line.getStartPoint();
        PointSketchPrimitive midChord = line.getEndPoint();
        sketch.getProjectedSketchPrimitives(new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{origin}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}));
        FixationConstraint fixOrigin = sketch.createFixationConstraint(origin);
        sketch.getProjectedSketchPrimitives(new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{line}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}), new NeoObjectVector(new Object[]{}));
        LengthDimension lengthDim = sketch.createLengthDimension(line, "0.5*${" + chordParamName + "}");
        sketch.setConstructionState(new NeoObjectVector(new Object[]{line}), true);
        CircleSketchPrimitive circle = sketch.createCircle(midChord, 1.0);
        RadiusDimension radiusDim = sketch.createRadiusDimension(circle, "1.1*(${" + chordParamName + "}/2)");
        cad.getFeatureManager().stopSketchEdit(sketch, true);
        cad.getFeatureManager().updateModelAfterFeatureEdited(sketch, null);

        ExtrusionMerge extrude = cad.getFeatureManager().createExtrusionMerge(sketch);
        extrude.setDirectionOption(0);
        extrude.setExtrudedBodyTypeOption(0);
        extrude.setDistanceOption(2);
        extrude.getDistance().setDefinition("0.2*${" + spanParamName + "}");
        extrude.getDistanceAsymmetric().setDefinition("1.2*${" + spanParamName + "}");
        cad.getFeatureManager().execute(extrude);
        sketch.setPresentationName("Ref1");
        extrude.setPresentationName("Extrude");
        Body domainBody = extrude.getBody(circle);
        domainBody.setPresentationName("Ref1");
    }

    private void createRegion() {
        CadModel domainCAD = (CadModel) _sim.get(SolidModelManager.class).getObject(domainCADName);
        Body domainCADBody = (Body) domainCAD.getBody(domainCADName);
        domainCAD.createParts(new NeoObjectVector(new Object[]{domainCADBody}), new NeoObjectVector(new Object[]{}), true, false, 1, false, false, 3, "SharpEdges", 30.0, 2, true, 1.0E-5, false);
        CadModel airfoilCAD = (CadModel) _sim.get(SolidModelManager.class).getObject(airfoilCADName);
        Body airfoilCADBody = (Body) airfoilCAD.getBody(airfoilBodyName);
        airfoilCAD.createParts(new NeoObjectVector(new Object[]{airfoilCADBody}), new NeoObjectVector(new Object[]{}), true, false, 1, false, false, 3, "SharpEdges", 30.0, 2, true, 1.0E-5, false);
        CadModel refinementCAD = (CadModel) _sim.get(SolidModelManager.class).getObject(refinementsCADName);
        Body ref1Body = (Body) refinementCAD.getBody("Ref1");
        refinementCAD.createParts(new NeoObjectVector(new Object[]{ref1Body}), new NeoObjectVector(new Object[]{}), true, false, 1, false, false, 3, "SharpEdges", 30.0, 2, true, 1.0E-5, false);
        SolidModelPart domainPart = (SolidModelPart) _sim.get(SimulationPartManager.class).getPart(domainCADName);
        SolidModelPart airfoilPart = (SolidModelPart) _sim.get(SimulationPartManager.class).getPart(airfoilBodyName);
        SubtractPartsOperation subtract = (SubtractPartsOperation) _sim.get(MeshOperationManager.class).createSubtractPartsOperation(new NeoObjectVector(new Object[]{domainPart, airfoilPart}));
        subtract.getTargetPartManager().setObjects(domainPart);
        subtract.setPerformCADBoolean(true);
        CadTessellationOption tessellationOption = subtract.getBooleanOperationValuesManager().get(CadTessellationOption.class);
        tessellationOption.getTessellationDensityOption().setSelected(TessellationDensityOption.Type.VERY_FINE);
        subtract.execute();
        MeshOperationPart subtractPart = (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart("Subtract");

        switch (type) {
            case TWO_D:
                PrepareFor2dOperation badgeFor2D = (PrepareFor2dOperation) _sim.get(MeshOperationManager.class).createPrepareFor2dOperation(new NeoObjectVector(new Object[]{subtractPart}));
                badgeFor2D.execute();
                Region reg2D = _sim.getRegionManager().createEmptyRegion();
                reg2D.setPresentationName(domainCADName);
                // Region reg2D = _sim.getRegionManager().getRegion(domainCADName);
                Boundary defaultBnd2D = reg2D.getBoundaryManager().getBoundary("Default");
                reg2D.getBoundaryManager().removeBoundaries(new NeoObjectVector(new Object[]{defaultBnd2D}));
                _sim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{subtractPart}), "OneRegion", reg2D, "OneBoundaryPerPart", null, RegionManager.CreateInterfaceMode.BOUNDARY, "OneEdgeBoundaryPerPart", null);
                Boundary freestreamBnd2D = reg2D.getBoundaryManager().createEmptyBoundary("00_FREESTREAM");
                // Boundary freestreamBnd2D = reg2D.getBoundaryManager().getBoundary("00_FREESTREAM");
                PartSurface freestreamSurf2D = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface(domainCADName + ".00_FREESTREAM");
                PartSurface rootSurf2D = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface(domainCADName + ".01_ROOT");
                freestreamBnd2D.getGeometryPartEntityGroup().addObjects(freestreamSurf2D);
                Boundary wallBnd2D = reg2D.getBoundaryManager().getBoundary("Subtract");
                wallBnd2D.setPresentationName("01_WALLS");
                freestreamBnd2D.setBoundaryType(FreeStreamBoundary.class);
                FlowDirectionProfile flowDirection2D = freestreamBnd2D.getValues().get(FlowDirectionProfile.class);
                flowDirection2D.setMethod(CompositeVectorProfileMethod.class);
                ScalarProfile xComponent2D = flowDirection2D.getMethod(CompositeVectorProfileMethod.class).getProfile(0);
                ScalarProfile yComponent2D = flowDirection2D.getMethod(CompositeVectorProfileMethod.class).getProfile(1);
                xComponent2D.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("cos($C_Alpha)");
                yComponent2D.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("sin($C_Alpha)");
                MachNumberProfile machProfile2D = freestreamBnd2D.getValues().get(MachNumberProfile.class);
                machProfile2D.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${C_Ma}");
                StaticTemperatureProfile tempProfile2D = freestreamBnd2D.getValues().get(StaticTemperatureProfile.class);
                tempProfile2D.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Q_T_Ref}");
                TurbulentViscosityRatioProfile turbViscosityProfile2D = freestreamBnd2D.getValues().get(TurbulentViscosityRatioProfile.class);
                turbViscosityProfile2D.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(0.2104382571);
                setUp2DMesh(subtractPart, new PartSurface[]{freestreamSurf2D, rootSurf2D});
                break;

            case THREE_D:
            case THREE_D_DIRECTED_MESH:
                Region reg = _sim.getRegionManager().createEmptyRegion();
                reg.setPresentationName(domainCADName);
                Boundary defaultBnd = reg.getBoundaryManager().getBoundary("Default");
                reg.getBoundaryManager().removeBoundaries(new NeoObjectVector(new Object[]{defaultBnd}));
                _sim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{subtractPart}), "OneRegion", reg, "OneBoundaryPerPart", null, RegionManager.CreateInterfaceMode.BOUNDARY, "OneEdgeBoundaryPerPart", null);
                Boundary freestreamBnd = reg.getBoundaryManager().createEmptyBoundary("00_FREESTREAM");
                PartSurface freestreamSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface(domainCADName + ".00_FREESTREAM");
                freestreamBnd.getGeometryPartEntityGroup().addObjects(freestreamSurf);
                Boundary symmBnd = reg.getBoundaryManager().createEmptyBoundary("01_SYMM");
                PartSurface rootSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface(domainCADName + ".01_ROOT");
                PartSurface tipSurf = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface(domainCADName + ".01_TIP");
                symmBnd.getGeometryPartEntityGroup().addObjects(rootSurf, tipSurf);
                Boundary wallsBnd = reg.getBoundaryManager().getBoundary("Subtract");
                wallsBnd.setPresentationName("01_WALLS");
                freestreamBnd.setBoundaryType(FreeStreamBoundary.class);
                FlowDirectionProfile flowDirection = freestreamBnd.getValues().get(FlowDirectionProfile.class);
                flowDirection.setMethod(CompositeVectorProfileMethod.class);
                ScalarProfile xComponent = flowDirection.getMethod(CompositeVectorProfileMethod.class).getProfile(0);
                ScalarProfile yComponent = flowDirection.getMethod(CompositeVectorProfileMethod.class).getProfile(1);
                xComponent.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("cos($C_Alpha)");
                yComponent.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("sin($C_Alpha)");
                MachNumberProfile machProfile = freestreamBnd.getValues().get(MachNumberProfile.class);
                machProfile.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${C_Ma}");
                StaticTemperatureProfile tempProfile = freestreamBnd.getValues().get(StaticTemperatureProfile.class);
                tempProfile.getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Q_T_Ref}");
                TurbulentViscosityRatioProfile turbViscosityProfile = freestreamBnd.getValues().get(TurbulentViscosityRatioProfile.class);
                turbViscosityProfile.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(0.2104382571);
                symmBnd.setBoundaryType(SymmetryBoundary.class);

                if (type == Type.THREE_D) {
                    setUp3DMesh(subtractPart, new PartSurface[]{freestreamSurf, rootSurf, tipSurf});
                } else {
                    setUp3DDirectedMesh(subtractPart, new PartSurface[]{freestreamSurf, rootSurf, tipSurf});
                }
        }

    }

    private void createFieldFunctions() {
        Dimensions lengthDim = Dimensions.Builder().length(1).build();
        UserFieldFunction lowerZ = _sim.getFieldFunctionManager().createFieldFunction();
        lowerZ.setPresentationName("00_LowerZ");
        lowerZ.setFunctionName("LowerZ");
        lowerZ.setDimensions(lengthDim);
        lowerZ.setDefinition("$${Area}[1] > 0.0 ? $${Centroid}[1] : 0.0");
        UserFieldFunction upperZ = _sim.getFieldFunctionManager().createFieldFunction();
        upperZ.setPresentationName("00_UpperZ");
        upperZ.setFunctionName("UpperZ");
        upperZ.setDimensions(lengthDim);
        upperZ.setDefinition("$${Area}[1] < 0.0 ? $${Centroid}[1] : 0.0");
        UserFieldFunction airfoilThickness = _sim.getFieldFunctionManager().createFieldFunction();
        airfoilThickness.setPresentationName("00_AirfoilThickness");
        airfoilThickness.setFunctionName("AirfoilThickness");
        airfoilThickness.setDimensions(lengthDim);
        airfoilThickness.setDefinition("${UpperZ} - ${LowerZ}");

        PressureCoefficientFunction cpFieldFunction = (PressureCoefficientFunction) _sim.getFieldFunctionManager().getFunction("PressureCoefficient");
        cpFieldFunction.getReferenceDensity().setDefinition("${Q_Rho_Ref}");
        cpFieldFunction.getReferenceVelocity().setDefinition("${Q_U_Ref}");

        SkinFrictionCoefficientFunction cfFieldFunction = (SkinFrictionCoefficientFunction) _sim.getFieldFunctionManager().getFunction("SkinFrictionCoefficient");
        cfFieldFunction.getReferenceDensity().setDefinition("${Q_Rho_Ref}");
        cfFieldFunction.getReferenceVelocity().setDefinition("${Q_U_Ref}");
    }

    private void createReportsAndMonitors() {
        Region region = _sim.getRegionManager().getRegion(domainCADName);
        Boundary walls = _sim.getRegionManager().getRegion(domainCADName).getBoundaryManager().getBoundary("01_WALLS");
        LatestMeshProxyRepresentation latestSurfaceVolume = (LatestMeshProxyRepresentation) _sim.getRepresentationManager().getObject("Latest Surface/Volume");

        ForceCoefficientReport cl = _sim.getReportManager().createReport(ForceCoefficientReport.class);
        cl.setPresentationName("Cl");
        cl.getDirection().setDefinition("[-sin(${C_Alpha}), cos(${C_Alpha}), 0.0]");
        cl.getReferenceDensity().setDefinition("${Q_Rho_Ref}");
        cl.getReferenceVelocity().setDefinition("${Q_U_Ref}");
        cl.getReferenceArea().setDefinition("${C_S_Ref}");
        cl.getParts().setQuery(null);
        cl.getParts().setObjects(walls);
        cl.setRepresentation(latestSurfaceVolume);
        cl.createMonitor();

        ForceCoefficientReport cd = _sim.getReportManager().createReport(ForceCoefficientReport.class);
        cd.setPresentationName("Cd");
        cd.getDirection().setDefinition("[cos(${C_Alpha}), sin(${C_Alpha}), 0.0]");
        cd.getReferenceDensity().setDefinition("${Q_Rho_Ref}");
        cd.getReferenceVelocity().setDefinition("${Q_U_Ref}");
        cd.getReferenceArea().setDefinition("${C_S_Ref}");
        cd.getParts().setQuery(null);
        cd.getParts().setObjects(walls);
        cd.setRepresentation(latestSurfaceVolume);
        cd.createMonitor();

        ForceReport lift = _sim.getReportManager().createReport(ForceReport.class);
        lift.setPresentationName("Lift");
        lift.getDirection().setDefinition("[-sin(${C_Alpha}), cos(${C_Alpha}), 0.0]");
        lift.getParts().setQuery(null);
        lift.getParts().setObjects(walls);
        lift.setRepresentation(latestSurfaceVolume);
        lift.createMonitor();

        ForceReport drag = _sim.getReportManager().createReport(ForceReport.class);
        drag.setPresentationName("Drag");
        drag.getDirection().setDefinition("[cos(${C_Alpha}), sin(${C_Alpha}), 0.0]");
        drag.getParts().setQuery(null);
        drag.getParts().setObjects(walls);
        drag.setRepresentation(latestSurfaceVolume);
        drag.createMonitor();

        ExpressionReport loverd = _sim.getReportManager().createReport(ExpressionReport.class);
        loverd.setPresentationName("L_over_D");
        loverd.setDefinition("${Lift}/${Drag}");
        ReportMonitor loverDMonitor = loverd.createMonitor();

        SurfaceIntegralReport airfoilArea = _sim.getReportManager().createReport(SurfaceIntegralReport.class);
        airfoilArea.setPresentationName("Airfoil_Area");
        airfoilArea.setFieldFunction(_sim.getFieldFunctionManager().getFunction("AirfoilThickness"));
        airfoilArea.getParts().setObjects(walls);
        airfoilArea.setRepresentation(latestSurfaceVolume);
        Dimensions dim = airfoilArea.getDimensions();

        ExpressionReport baselineArea = _sim.getReportManager().createReport(ExpressionReport.class);
        baselineArea.setPresentationName("Baseline_Area");
        baselineArea.setDimensions(dim);
        double value = airfoilArea.getReportMonitorValue();
        baselineArea.setDefinition(Double.toString(value));

        ExpressionReport areaConstraint = _sim.getReportManager().createReport(ExpressionReport.class);
        areaConstraint.setPresentationName("Area_Constraint");
        areaConstraint.setDefinition("${Airfoil_Area}/(0.95*${Baseline_Area})");

        ExpressionReport currentIter = _sim.getReportManager().createReport(ExpressionReport.class);
        currentIter.setPresentationName("Current_Iteration");
        currentIter.setDefinition("${Iteration}");

        ElementCountReport nCells = _sim.getReportManager().createReport(ElementCountReport.class);
        nCells.setPresentationName("Cells");
        nCells.getParts().addPart(region);
        nCells.setRepresentation(latestSurfaceVolume);

        IteratorCpuTimeReport iterCPU = _sim.getReportManager().createReport(IteratorCpuTimeReport.class);
        iterCPU.setPresentationName("SICPUT");
        iterCPU.createMonitor();
        IteratorElapsedTimeReport iterElapsed = _sim.getReportManager().createReport(IteratorElapsedTimeReport.class);
        iterElapsed.setPresentationName("SIET");
        iterElapsed.createMonitor();
        CumulativeCpuTimeReport totalCPU = _sim.getReportManager().createReport(CumulativeCpuTimeReport.class);
        totalCPU.setPresentationName("TSCPUT");
        CumulativeElapsedTimeReport totalElapsed = _sim.getReportManager().createReport(CumulativeElapsedTimeReport.class);
        totalElapsed.setPresentationName("TSET");

        if (type != Type.TWO_D) {
            ReportCostFunction objCostFunction = _sim.get(AdjointCostFunctionManager.class).createAdjointCostFunction(ReportCostFunction.class);
            objCostFunction.setPresentationName("L_over_D");
            objCostFunction.setReport(loverd);
            AdjointResidualStoppingCriterion adjointResidualStoppingCriterion = (AdjointResidualStoppingCriterion) objCostFunction.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Residual");
            MonitorIterationStoppingCriterionRelativeChangeType relativeChangeType = (MonitorIterationStoppingCriterionRelativeChangeType) adjointResidualStoppingCriterion.getCriterionType();
            relativeChangeType.setRelativeChange(1E-9);

            ReportCostFunction constCostFunction = _sim.get(AdjointCostFunctionManager.class).createAdjointCostFunction(ReportCostFunction.class);
            constCostFunction.setPresentationName("Area_Constraint");
            constCostFunction.setReport(areaConstraint);
            adjointResidualStoppingCriterion = (AdjointResidualStoppingCriterion) constCostFunction.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Residual");
            ((MonitorIterationStoppingCriterionOption) adjointResidualStoppingCriterion.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.Type.MINIMUM);
            MonitorIterationStoppingCriterionMinLimitType minLimitType = (MonitorIterationStoppingCriterionMinLimitType) adjointResidualStoppingCriterion.getCriterionType();
            minLimitType.getLimit().setValue(1E-9);
        }

        SteadySolver steadySolver = (SteadySolver) _sim.getSolverManager().getSolver(SteadySolver.class);
        StepStoppingCriterion maxSteps = steadySolver.getSolverStoppingCriterionManager().createSolverStoppingCriterion(StepStoppingCriterion.class);
        maxSteps.getMaximumNumberStepsObject().getQuantity().setValue(2501.0);
        MonitorIterationStoppingCriterion loverDCrit = steadySolver.getSolverStoppingCriterionManager().createIterationStoppingCriterion(loverDMonitor);
        ((MonitorIterationStoppingCriterionOption) loverDCrit.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.Type.ASYMPTOTIC);
        MonitorIterationStoppingCriterionAsymptoticType asymptoticType = (MonitorIterationStoppingCriterionAsymptoticType) loverDCrit.getCriterionType();
        asymptoticType.setNormalized(true);
        asymptoticType.getMaxWidth().setValue(0.0001);
        asymptoticType.setNumberSamples(150);

        StepStoppingCriterion globalMaxSteps = (StepStoppingCriterion) _sim.getSolverStoppingCriterionManager().getObject("Maximum Steps");
        _sim.getSolverStoppingCriterionManager().removeObjects(globalMaxSteps);
    }

    private void createPlots() {
        LatestMeshProxyRepresentation latestSurfaceVolume = (LatestMeshProxyRepresentation) _sim.getRepresentationManager().getObject("Latest Surface/Volume");

        ResidualPlot residuals = (ResidualPlot) _sim.getPlotManager().getPlot("Residuals");
        applyPlotOptions(residuals);
        Cartesian2DAxisManager axisManager = (Cartesian2DAxisManager) residuals.getAxisManager();
        Cartesian2DAxis bottomAxis = (Cartesian2DAxis) axisManager.getAxis("Bottom Axis");
        bottomAxis.setLockMinimum(true);

        ReportMonitor cdMonitor = (ReportMonitor) _sim.getMonitorManager().getMonitor("Cd Monitor");
        ReportMonitor clMonitor = (ReportMonitor) _sim.getMonitorManager().getMonitor("Cl Monitor");
        MonitorPlot forceCoeffPlot = _sim.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[]{cdMonitor, clMonitor}), "Force Coeff");
        axisManager = (Cartesian2DAxisManager) forceCoeffPlot.getAxisManager();
        bottomAxis = (Cartesian2DAxis) axisManager.getAxis("Bottom Axis");
        bottomAxis.setLockMinimum(true);
        Cartesian2DAxis rightAxis = (Cartesian2DAxis) axisManager.createAxis(Cartesian2DAxis.Position.Right);
        MonitorDataSet clData = (MonitorDataSet) forceCoeffPlot.getDataSetManager().getDataSet("Cl Monitor");
        clData.setYAxis(rightAxis);
        applyPlotOptions(forceCoeffPlot);

        ReportMonitor dragMonitor = (ReportMonitor) _sim.getMonitorManager().getMonitor("Drag Monitor");
        ReportMonitor liftMonitor = (ReportMonitor) _sim.getMonitorManager().getMonitor("Lift Monitor");
        MonitorPlot forcePlot = _sim.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[]{dragMonitor, liftMonitor}), "Forces");
        axisManager = (Cartesian2DAxisManager) forcePlot.getAxisManager();
        bottomAxis = (Cartesian2DAxis) axisManager.getAxis("Bottom Axis");
        bottomAxis.setLockMinimum(true);
        rightAxis = (Cartesian2DAxis) axisManager.createAxis(Cartesian2DAxis.Position.Right);
        clData = (MonitorDataSet) forcePlot.getDataSetManager().getDataSet("Lift Monitor");
        clData.setYAxis(rightAxis);
        applyPlotOptions(forcePlot);

        ReportMonitor loverdMonitor = (ReportMonitor) _sim.getMonitorManager().getMonitor("L_over_D Monitor");
        MonitorPlot loverdPlot = _sim.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[]{loverdMonitor}), "LoverD");
        loverdPlot.setTitle("L/D");
        axisManager = (Cartesian2DAxisManager) loverdPlot.getAxisManager();
        Cartesian2DAxis leftAxis = (Cartesian2DAxis) axisManager.getAxis("Left Axis");
        leftAxis.getTitle().setText("L/D");
        bottomAxis = (Cartesian2DAxis) axisManager.getAxis("Bottom Axis");
        bottomAxis.setLockMinimum(true);
        MultiColLegend legend = loverdPlot.getLegend();
        legend.setVisible(false);
        applyPlotOptions(loverdPlot);

        XYPlot cpPlot = _sim.getPlotManager().createPlot(XYPlot.class);
        cpPlot.setPresentationName("Cp");
        cpPlot.setRepresentation(latestSurfaceVolume);
        cpPlot.setAspectRatio(AspectRatioEnum.RATIO_4_3);
        YAxisType yAxisType_0 = (YAxisType) cpPlot.getYAxes().getAxisType("Y Type 1");
        FieldFunctionUnits fieldFunctionUnits = yAxisType_0.getScalarFunction();
        PressureCoefficientFunction cpFieldFunction = (PressureCoefficientFunction) _sim.getFieldFunctionManager().getFunction("PressureCoefficient");
        fieldFunctionUnits.setFieldFunction(cpFieldFunction);
        Region region = _sim.getRegionManager().getRegion(domainCADName);
        Boundary boundary = region.getBoundaryManager().getBoundary("01_WALLS");
        cpPlot.getParts().addObjects(boundary);
        axisManager = (Cartesian2DAxisManager) cpPlot.getAxisManager();
        leftAxis = (Cartesian2DAxis) axisManager.getAxis("Left Axis");
        leftAxis.setReverse(true);
        leftAxis.setMinimum(-6.0);
        leftAxis.setMaximum(2.0);
        legend = cpPlot.getLegend();
        legend.setVisible(false);
        bottomAxis = (Cartesian2DAxis) axisManager.getAxis("Bottom Axis");
        bottomAxis.setMinimum(-0.1);
        bottomAxis.setMaximum(1.1);
        InternalDataSet dataSet = (InternalDataSet) yAxisType_0.getDataSetManager().getDataSet(domainCADName + ": 01_WALLS");
        SymbolStyleWithSpacing symbolStyle = dataSet.getSymbolStyle();
        symbolStyle.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.FILLED_CIRCLE);
        symbolStyle.setColor(new DoubleVector(new double[]{0.2824000120162964, 0.23919999599456787, 0.5450999736785889}));
        symbolStyle.setSize(4);
        cpPlot.setTitle("Pressure Coefficient");
        applyPlotOptions(cpPlot);

        XYPlot cfPlot = _sim.getPlotManager().createPlot(XYPlot.class);
        cfPlot.setPresentationName("Cf");
        cfPlot.setRepresentation(latestSurfaceVolume);
        cfPlot.setAspectRatio(AspectRatioEnum.RATIO_4_3);
        yAxisType_0 = (YAxisType) cfPlot.getYAxes().getAxisType("Y Type 1");
        fieldFunctionUnits = yAxisType_0.getScalarFunction();
        SkinFrictionCoefficientFunction cfFieldFunction = (SkinFrictionCoefficientFunction) _sim.getFieldFunctionManager().getFunction("SkinFrictionCoefficient");
        fieldFunctionUnits.setFieldFunction(cfFieldFunction);
        cfPlot.getParts().addObjects(boundary);
        axisManager = (Cartesian2DAxisManager) cfPlot.getAxisManager();
        leftAxis = (Cartesian2DAxis) axisManager.getAxis("Left Axis");
        leftAxis.setMinimum(-0.002);
        leftAxis.setMaximum(0.018);
        legend = cfPlot.getLegend();
        legend.setVisible(false);
        bottomAxis = (Cartesian2DAxis) axisManager.getAxis("Bottom Axis");
        bottomAxis.setMinimum(-0.1);
        bottomAxis.setMaximum(1.1);
        dataSet = (InternalDataSet) yAxisType_0.getDataSetManager().getDataSet(domainCADName + ": 01_WALLS");
        symbolStyle = dataSet.getSymbolStyle();
        symbolStyle.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.FILLED_CIRCLE);
        symbolStyle.setColor(new DoubleVector(new double[]{0.2824000120162964, 0.23919999599456787, 0.5450999736785889}));
        symbolStyle.setSize(4);
        cfPlot.setTitle("Skin Friction Coefficient");
        applyPlotOptions(cfPlot);
    }

    private void createScenes() {
        PredefinedLookupTable blueYellowRedCmap = (PredefinedLookupTable) _sim.get(LookupTableManager.class).getObject("blue-yellow-red");
        PartRepresentation geomRepresentation = (PartRepresentation) _sim.getRepresentationManager().getObject("Geometry");
        PartSubRepresentation remeshRepresentation;
        try {
            remeshRepresentation = (PartSubRepresentation) geomRepresentation.getSubRepresentations().getObject("Geometric Sensitivity.Remesh");
        } catch (Exception ex) {
            remeshRepresentation = null;
        }
        Region region = _sim.getRegionManager().getRegion("Domain");
        Boundary wallBnd = region.getBoundaryManager().getBoundary("01_WALLS");

        _sim.getSceneManager().createEmptyScene("GEOM", null);
        Scene geomScene = _sim.getSceneManager().getScene("GEOM 1");
        geomScene.setPresentationName("GEOM");
        PartDisplayer outlineDisp = geomScene.getDisplayerManager().createPartDisplayer("Outline", -1, 4);
        outlineDisp.getVisibleParts().addParts(wallBnd);
        outlineDisp.setRepresentation(geomRepresentation);
        outlineDisp.setLineWidth(2.0);
        geomScene.setAxesVisible(false);
        geomScene.getAnnotationPropManager().getAnnotationGroup().setQuery(null);
        geomScene.getAnnotationPropManager().getAnnotationGroup().setObjects();
        geomScene.setAspectRatio(AspectRatioEnum.RATIO_4_3);

        _sim.getSceneManager().createGeometryScene("MESH", "Outline", "Mesh", 3, null);
        Scene meshScene = _sim.getSceneManager().getScene("MESH 1");
        meshScene.setPresentationName("MESH");
        meshScene.setAxesVisible(false);
        meshScene.getAnnotationPropManager().getAnnotationGroup().setQuery(null);
        meshScene.getAnnotationPropManager().getAnnotationGroup().setObjects();
        meshScene.setAspectRatio(AspectRatioEnum.RATIO_4_3);

        _sim.getSceneManager().createEmptyScene("Mach", null);
        Scene machScene = _sim.getSceneManager().getScene("Mach 1");
        machScene.setPresentationName("Mach");
        ScalarDisplayer scalarDisp = machScene.getDisplayerManager().createScalarDisplayer("Scalar", ClipMode.NONE);
        Legend legend = scalarDisp.getLegend();
        legend.setLookupTable(blueYellowRedCmap);
        MeshOperationPart subtractPart = (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart("Subtract");
        PartSurface rootSurface = (PartSurface) subtractPart.getPartSurfaceManager().getPartSurface("Domain.01_ROOT");
        scalarDisp.getVisibleParts().addParts(rootSurface);
        scalarDisp.getHiddenParts().addParts();
        PrimitiveFieldFunction machFF = (PrimitiveFieldFunction) _sim.getFieldFunctionManager().getFunction("MachNumber");
        scalarDisp.getScalarDisplayQuantity().setFieldFunction(machFF);
        scalarDisp.getScalarDisplayQuantity().getMinimumValue().setValue(0.0);
        scalarDisp.getScalarDisplayQuantity().getMaximumValue().setValue(0.6);
        machScene.setAxesVisible(false);
        machScene.getAnnotationPropManager().getAnnotationGroup().setQuery(null);
        machScene.getAnnotationPropManager().getAnnotationGroup().setObjects();
        machScene.setAspectRatio(AspectRatioEnum.RATIO_4_3);

        _sim.getSceneManager().createEmptyScene("Pressure", null);
        Scene pressureScene = _sim.getSceneManager().getScene("Pressure 1");
        pressureScene.setPresentationName("Pressure");
        scalarDisp = pressureScene.getDisplayerManager().createScalarDisplayer("Scalar", ClipMode.NONE);
        legend = scalarDisp.getLegend();
        legend.setLookupTable(blueYellowRedCmap);
        scalarDisp.getVisibleParts().addParts(rootSurface);
        scalarDisp.getHiddenParts().addParts();
        PrimitiveFieldFunction pressureFF = (PrimitiveFieldFunction) _sim.getFieldFunctionManager().getFunction("Pressure");
        scalarDisp.getScalarDisplayQuantity().setFieldFunction(pressureFF);
        scalarDisp.getScalarDisplayQuantity().getMinimumValue().setValue(-10000.0);
        scalarDisp.getScalarDisplayQuantity().getMaximumValue().setValue(5000.0);
        pressureScene.setAxesVisible(false);
        pressureScene.getAnnotationPropManager().getAnnotationGroup().setQuery(null);
        pressureScene.getAnnotationPropManager().getAnnotationGroup().setObjects();
        pressureScene.setAspectRatio(AspectRatioEnum.RATIO_4_3);

        if (type != Type.TWO_D) {
            _sim.getSceneManager().getGroupsManager().createGroup("Adj Sens");
            ClientServerObjectGroup adjGroup = (ClientServerObjectGroup) _sim.getSceneManager().getGroupsManager().getObject("Adj Sens");
            ArrayList<Scene> scenesToGroup = new ArrayList<>();
            int counter = 1;
            for (ReportCostFunction rcf : _sim.get(AdjointCostFunctionManager.class).getObjectsOf(ReportCostFunction.class)) {
                String functionName = "Adjoint" + Integer.toString(counter) + "::Surface Sensitivity";
                String sceneName = rcf.getReport().getPresentationName().replace(" ", "");
                _sim.getSceneManager().createEmptyScene(sceneName, null);
                Scene adjScene = _sim.getSceneManager().getScene(sceneName + " 1");
                adjScene.setPresentationName(sceneName);
                VectorDisplayer vectorDisp = adjScene.getDisplayerManager().createVectorDisplayer("Vector", ClipMode.NONE);
                legend = vectorDisp.getLegend();
                legend.setLookupTable(blueYellowRedCmap);
                vectorDisp.getVisibleParts().addParts(wallBnd);
                PrimitiveFieldFunction adjLoverDSens = (PrimitiveFieldFunction) _sim.getFieldFunctionManager().getFunction(functionName);
                vectorDisp.getVectorDisplayQuantity().setFieldFunction(adjLoverDSens);
                GlyphSettings glyphSettings_0 = vectorDisp.getGlyphSettings();
                glyphSettings_0.setVectorStyle(VectorStyle.HEAD_3D_TAIL_2D);
                scenesToGroup.add(adjScene);
                adjScene.setAxesVisible(false);
                adjScene.getAnnotationPropManager().getAnnotationGroup().setQuery(null);
                adjScene.getAnnotationPropManager().getAnnotationGroup().setObjects();
                adjScene.setAspectRatio(AspectRatioEnum.RATIO_4_3);
                counter++;
            }
            adjGroup.getGroupsManager().groupObjects("Adj Sens", scenesToGroup);

            _sim.getSceneManager().getGroupsManager().createGroup("Geom Sens");
            ClientServerObjectGroup geomGroup = (ClientServerObjectGroup) _sim.getSceneManager().getGroupsManager().getObject("Geom Sens");
            scenesToGroup = new ArrayList<>();
            for (ScalarGlobalParameter sgp : getGeometricSensParams()) {
                String paramName = sgp.getPresentationName();
                String sceneName = paramName;
                String functionName = "GeometricSensitivity::" + paramName;
                _sim.getSceneManager().createEmptyScene(sceneName, null);
                Scene geomSensScene = _sim.getSceneManager().getScene(sceneName + " 1");
                geomSensScene.setPresentationName(sceneName);
                VectorDisplayer vectorDisp = geomSensScene.getDisplayerManager().createVectorDisplayer("Vector", ClipMode.NONE);
                if (remeshRepresentation != null) {
                    vectorDisp.setRepresentation(remeshRepresentation);
                }
                legend = vectorDisp.getLegend();
                legend.setLookupTable(blueYellowRedCmap);
                vectorDisp.getVisibleParts().addParts(wallBnd);
                PrimitiveFieldFunction adjLoverDSens = (PrimitiveFieldFunction) _sim.getFieldFunctionManager().getFunction(functionName);
                vectorDisp.getVectorDisplayQuantity().setFieldFunction(adjLoverDSens);
                GlyphSettings glyphSettings_0 = vectorDisp.getGlyphSettings();
                glyphSettings_0.setVectorStyle(VectorStyle.HEAD_3D_TAIL_2D);
                outlineDisp = geomSensScene.getDisplayerManager().createPartDisplayer("Outline", -1, 4);
                outlineDisp.getVisibleParts().addParts(wallBnd);
                outlineDisp.setRepresentation(geomRepresentation);
                outlineDisp.setLineWidth(2.0);
                geomSensScene.setAxesVisible(false);
                geomSensScene.getAnnotationPropManager().getAnnotationGroup().setQuery(null);
                geomSensScene.getAnnotationPropManager().getAnnotationGroup().setObjects();
                geomSensScene.setAspectRatio(AspectRatioEnum.RATIO_4_3);
                scenesToGroup.add(geomSensScene);
            }
            geomGroup.getGroupsManager().groupObjects("Geom Sens", scenesToGroup);
        }
    }

    private double[] initPositions(int onRatio) {
        Integer[] points_to_use = coordinatesToUse(onRatio);

        int n_points = points_to_use.length;

        GlobalParameterManager man = _sim.get(GlobalParameterManager.class);

        double[] coord = new double[n_points * 2];
        double[] x_vals = new double[n_points];
        double[] z_vals = new double[n_points];
        String[] x_names = xParamNames(onRatio);
        String[] z_names = zParamNames(onRatio);

        for (int i = 0; i < x_vals.length; i++) {
            String x_name = x_names[i];
            String z_name = z_names[i];
            ScalarGlobalParameter sgp_x = (ScalarGlobalParameter) man.getObject(x_name);
            ScalarGlobalParameter sgp_z = (ScalarGlobalParameter) man.getObject(z_name);
            double x_val = sgp_x.getQuantity().getRawValue();
            double z_val = sgp_z.getQuantity().getRawValue();
            x_vals[i] = x_val;
            z_vals[i] = z_val;
        }

        int x_idx = 0;
        for (int i = 0; i < n_points * 2; i += 2) {
            coord[i] = x_vals[x_idx];
            coord[i + 1] = z_vals[x_idx];
            x_idx++;
        }
        return coord;
    }

    private String[] xParamNames(int onRatio) {
        Integer[] toUse = coordinatesToUse(onRatio);
        String[] xNames = new String[toUse.length];

        int counter = 0;
        for (int i : toUse) {
            String name = "X_" + Integer.toString(i);
            xNames[counter] = name;
            counter++;
        }

        return xNames;
    }

    private String[] zParamNames(int onRatio) {
        Integer[] toUse = coordinatesToUse(onRatio);
        String[] zNames = new String[toUse.length];

        int counter = 0;
        for (int i : toUse) {
            String name = "Z_" + Integer.toString(i);
            zNames[counter] = name;
            counter++;
        }

        return zNames;
    }

    private Integer[] coordinatesToUse(int onRatio) {
        int max_index = 300;
        int le_index = 150;
        int te_index = 300;
        ArrayList<Integer> points_to_use = new ArrayList<>();
        for (int i = 0; i <= max_index; i += onRatio) {
            points_to_use.add(i);
        }

        if (!points_to_use.contains(le_index)) {
            points_to_use.add(le_index);
        }
        if (!points_to_use.contains(te_index)) {
            points_to_use.add(te_index);
        }

        Collections.sort(points_to_use);

        Integer[] toUse = points_to_use.toArray(new Integer[points_to_use.size()]);
        return toUse;
    }

    private String getZExpression(String x_var, String[] weight_vars) {
        int point_n = Integer.parseInt(x_var.split("_")[1]);
        FileTable modesTable = (FileTable) _sim.getTableManager().getObject("svd_1_modes");
        FileTable zBarTable = (FileTable) _sim.getTableManager().getObject("svd_1_zbar_T");
        TableData modes = modesTable.getSeriesData();
        TableData zBar = zBarTable.getSeriesData();

        String exp = "(" + Double.toString(zBar.getSeries(1)[point_n]) + " +\n";
        exp += "${" + x_var + "} * ${P_TE_Thickness} / ${" + chordParamName + "} + \n";
        int count = 1;
        int max = weight_vars.length;

        for (String w : weight_vars) {
            if (count == max) {
                exp += "${" + w + "} * " + Double.toString(modes.getSeries(count)[point_n]) + ") * ${" + chordParamName + "}";
            } else {
                exp += "${" + w + "} * " + Double.toString(modes.getSeries(count)[point_n]) + " +\n";
            }
            count++;
        }
        return exp;
    }

    private void setUp2DMesh(MeshOperationPart part2Mesh, PartSurface[] freestreamControlSurfaces) {
        Units m = _sim.getUnitsManager().getInternalUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        Units mm = (Units) _sim.getUnitsManager().getObject("mm");
        Units dimensionlessUnits = (Units) _sim.getUnitsManager().getObject("");

        AutoMeshOperation2d meshOp = _sim.get(MeshOperationManager.class).createAutoMeshOperation2d(new StringVector(new String[]{"star.twodmesher.DualAutoMesher2d", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{part2Mesh}));
        PrismAutoMesher prismLayerMesher = (PrismAutoMesher) meshOp.getMeshers().getObject("Prism Layer Mesher");
        prismLayerMesher.getPrismStretchingOption().setSelected(PrismStretchingOption.Type.WALL_THICKNESS);

        meshOp.getDefaultValues().get(BaseSize.class).setDefinition("0.01*${C_Chord}");
        PartsTargetSurfaceSize targetSize = meshOp.getDefaultValues().get(PartsTargetSurfaceSize.class);
        targetSize.getRelativeSizeScalar().setValueAndUnits(10.0, dimensionlessUnits);
        PartsMinimumSurfaceSize minimumSize = meshOp.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        minimumSize.getRelativeSizeScalar().setValueAndUnits(0.5, dimensionlessUnits);
        SurfaceCurvature curvatureRefinement = meshOp.getDefaultValues().get(SurfaceCurvature.class);
        curvatureRefinement.setNumPointsAroundCircle(72.0);
        SurfaceGrowthRate surfaceGrowthRate = meshOp.getDefaultValues().get(SurfaceGrowthRate.class);
        surfaceGrowthRate.setGrowthRateOption(SurfaceGrowthRate.GrowthRateOption.USER_SPECIFIED);
        surfaceGrowthRate.getGrowthRateScalar().setValueAndUnits(1.05, dimensionlessUnits);
        NumPrismLayers numPrismLayers = meshOp.getDefaultValues().get(NumPrismLayers.class);
        numPrismLayers.getNumLayersValue().getQuantity().setValue(25.0);
        meshOp.getDefaultValues().get(PrismWallThickness.class).setValueAndUnits(0.008, mm);
        meshOp.getDefaultValues().get(PrismThickness.class).getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        meshOp.getDefaultValues().get(PrismThickness.class).getAbsoluteSizeValue().setValueAndUnits(20.0, mm);
        meshOp.getDefaultValues().get(PrismLayerReductionPercentage.class).setValue(10.0);
        meshOp.getDefaultValues().get(PrismLayerCoreLayerAspectRatio.class).setValue(0.8);
        meshOp.getDefaultValues().get(PrismLayerMinimumThickness.class).setValue(2.0);

        SurfaceCustomMeshControl freestreamControl = meshOp.getCustomMeshControls().createSurfaceControl();
        freestreamControl.setPresentationName("FREESTREAM");
        freestreamControl.getGeometryObjects().setQuery(null);
        freestreamControl.getGeometryObjects().setObjects(freestreamControlSurfaces);
        freestreamControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        PartsTargetSurfaceSize freestreamTarget = freestreamControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        freestreamTarget.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        freestreamTarget.getAbsoluteSizeValue().setValueAndUnits(5.0, m);

        SurfaceCustomMeshControl trailingEdgeControl = meshOp.getCustomMeshControls().createSurfaceControl();
        trailingEdgeControl.setPresentationName("TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setQuery(null);
        PartSurface trailingEdgeSurface = (PartSurface) part2Mesh.getPartSurfaceManager().getPartSurface(airfoilBodyName + ".01_TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setObjects(trailingEdgeSurface);
        trailingEdgeControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        PartsTargetSurfaceSize trailingEdgeTargetSize = trailingEdgeControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        trailingEdgeTargetSize.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        trailingEdgeTargetSize.getAbsoluteSizeValue().setDefinition("${P_TE_Thickness}/10");

        VolumeCustomMeshControl volumeRef1 = meshOp.getCustomMeshControls().createVolumeControl();
        volumeRef1.getGeometryObjects().setQuery(null);
        SolidModelPart refPart = (SolidModelPart) _sim.get(SimulationPartManager.class).getPart("Ref1");
        volumeRef1.getGeometryObjects().setObjects(refPart);
        VolumeControlResurfacerSizeOption volControlSurfaceSizeOption = volumeRef1.getCustomConditions().get(VolumeControlResurfacerSizeOption.class);
        volControlSurfaceSizeOption.setVolumeControlBaseSizeOption(true);
        VolumeControlSize volumeControlSize = volumeRef1.getCustomValues().get(VolumeControlSize.class);
        volumeControlSize.getRelativeSizeScalar().setValueAndUnits(30.0, dimensionlessUnits);
    }

    private void setUp3DMesh(MeshOperationPart part2Mesh, PartSurface[] freestreamControlSurfaces) {
        Units m = _sim.getUnitsManager().getInternalUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        Units mm = (Units) _sim.getUnitsManager().getObject("mm");
        AutoMeshOperation meshOp = _sim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.dualmesher.DualAutoMesher", "star.prismmesher.PrismAutoMesher"}), new NeoObjectVector(new Object[]{part2Mesh}));
        meshOp.getMesherParallelModeOption().setSelected(MesherParallelModeOption.Type.PARALLEL);
        ResurfacerAutoMesher resurfaceMesher = (ResurfacerAutoMesher) meshOp.getMeshers().getObject("Surface Remesher");
        resurfaceMesher.setDoProximityRefinement(false);
        PrismAutoMesher prismLayerMesher = (PrismAutoMesher) meshOp.getMeshers().getObject("Prism Layer Mesher");
        prismLayerMesher.getPrismStretchingOption().setSelected(PrismStretchingOption.Type.WALL_THICKNESS);
        meshOp.getDefaultValues().get(BaseSize.class).setDefinition("0.1*${C_Chord}");
        PartsTargetSurfaceSize targetSize = meshOp.getDefaultValues().get(PartsTargetSurfaceSize.class);
        Units dimensionlessUnits = (Units) _sim.getUnitsManager().getObject("");
        targetSize.getRelativeSizeScalar().setValueAndUnits(10.0, dimensionlessUnits);
        PartsMinimumSurfaceSize minimumSize = meshOp.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        minimumSize.getRelativeSizeScalar().setValueAndUnits(0.5, dimensionlessUnits);
        SurfaceCurvature curvatureRefinement = meshOp.getDefaultValues().get(SurfaceCurvature.class);
        curvatureRefinement.setNumPointsAroundCircle(56.0);
        SurfaceGrowthRate surfaceGrowthRate = meshOp.getDefaultValues().get(SurfaceGrowthRate.class);
        surfaceGrowthRate.setGrowthRateOption(SurfaceGrowthRate.GrowthRateOption.USER_SPECIFIED);
        surfaceGrowthRate.getGrowthRateScalar().setValueAndUnits(1.05, dimensionlessUnits);
        NumPrismLayers numPrismLayers = meshOp.getDefaultValues().get(NumPrismLayers.class);
        numPrismLayers.getNumLayersValue().getQuantity().setValue(25.0);
        meshOp.getDefaultValues().get(PrismWallThickness.class).setValueAndUnits(0.008, mm);
        meshOp.getDefaultValues().get(PrismThickness.class).getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        meshOp.getDefaultValues().get(PrismThickness.class).getAbsoluteSizeValue().setValueAndUnits(20.0, mm);
        meshOp.getDefaultValues().get(PrismLayerReductionPercentage.class).setValue(10.0);
        meshOp.getDefaultValues().get(PrismLayerCoreLayerAspectRatio.class).setValue(0.8);
        meshOp.getDefaultValues().get(PrismLayerMinimumThickness.class).setValue(2.0);

        SurfaceCustomMeshControl freestreamControl = meshOp.getCustomMeshControls().createSurfaceControl();
        freestreamControl.setPresentationName("FREESTREAM");
        freestreamControl.getGeometryObjects().setQuery(null);
        freestreamControl.getGeometryObjects().setObjects(freestreamControlSurfaces);
        freestreamControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        PartsTargetSurfaceSize freestreamTarget = freestreamControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        freestreamTarget.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        freestreamTarget.getAbsoluteSizeValue().setDefinition("${C_Span}");

        SurfaceCustomMeshControl trailingEdgeControl = meshOp.getCustomMeshControls().createSurfaceControl();
        trailingEdgeControl.setPresentationName("TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setQuery(null);
        PartSurface trailingEdgeSurface = (PartSurface) part2Mesh.getPartSurfaceManager().getPartSurface("SVDAirfoil.01_TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setObjects(trailingEdgeSurface);
        trailingEdgeControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        PartsTargetSurfaceSize trailingEdgeTargetSize = trailingEdgeControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        trailingEdgeTargetSize.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        trailingEdgeTargetSize.getAbsoluteSizeValue().setDefinition("${P_TE_Thickness}/4");

        meshOp.getMeshers().setMeshersByNames(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.dualmesher.DualAutoMesher", "star.prismmesher.PrismAutoMesher", "star.meshing.GeometricSensitivityAutoMesher"}));
        GeometricSensitivityOptions geomSensitivityOpts = meshOp.getDefaultValues().get(GeometricSensitivityOptions.class);
        geomSensitivityOpts.getParameters().setQuery(null);
        geomSensitivityOpts.getParameters().setObjects(getGeometricSensParams());
        geomSensitivityOpts.getStepSize().setValue(0.035);
        geomSensitivityOpts.getMinStep().setValue(0.0035);
    }

    private void setUp3DDirectedMesh(MeshOperationPart part2Mesh, PartSurface[] freestreamControlSurfaces) {
        Units dimensionlessUnits = (Units) _sim.getUnitsManager().getObject("");
        Units m = _sim.getUnitsManager().getInternalUnits(new IntVector(new int[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        Units mm = (Units) _sim.getUnitsManager().getObject("mm");
        NeoObjectVector parts2Mesh = new NeoObjectVector(new Object[]{part2Mesh});
        PartSurface trailingEdgeSurface = (PartSurface) part2Mesh.getPartSurfaceManager().getPartSurface("SVDAirfoil.01_TRAILING_EDGE");

        DirectedMeshOperation directedMeshOp = (DirectedMeshOperation) _sim.get(MeshOperationManager.class).createDirectedMeshOperation(parts2Mesh);
        PartSurface source = part2Mesh.getPartSurfaceManager().getPartSurface(domainCADName + ".01_ROOT");
        PartSurface target = part2Mesh.getPartSurfaceManager().getPartSurface(domainCADName + ".01_TIP");
        directedMeshOp.getSourceSurfaceGroup().setObjects(source);
        directedMeshOp.getTargetSurfaceGroup().setObjects(target);
        _sim.println(part2Mesh.getPresentationName());
        DirectedMeshPartCollection directedMeshPartCollection = directedMeshOp.getGuidedMeshPartCollectionManager().getObject(part2Mesh.getPresentationName());
        NeoObjectVector directedMeshParts2Mesh = new NeoObjectVector(new Object[]{directedMeshPartCollection});
        directedMeshOp.getGuidedSurfaceMeshBaseManager().createAutoSourceMesh(new StringVector(new String[]{"star.twodmesher.DualAutoMesher2d", "star.prismmesher.PrismAutoMesher"}), directedMeshParts2Mesh);
        DirectedAutoSourceMesh sourceMesh = (DirectedAutoSourceMesh) directedMeshOp.getGuidedSurfaceMeshBaseManager().getObject("Auto Mesh");
        DualAutoMesher2d polygonalMesher = (DualAutoMesher2d) sourceMesh.getMeshers().getObject("Polygonal Mesher");
        polygonalMesher.setDoProximityRefinement(false);
        sourceMesh.getDefaultValues().get(BaseSize.class).setDefinition("0.01*${C_Chord}");
        PartsTargetSurfaceSize targetSize = sourceMesh.getDefaultValues().get(PartsTargetSurfaceSize.class);
        targetSize.getRelativeSizeScalar().setValueAndUnits(10.0, dimensionlessUnits);
        PartsMinimumSurfaceSize minimumSize = sourceMesh.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        minimumSize.getRelativeSizeScalar().setValueAndUnits(0.5, dimensionlessUnits);
        SurfaceCurvature curvatureRefinement = sourceMesh.getDefaultValues().get(SurfaceCurvature.class);
        curvatureRefinement.setNumPointsAroundCircle(72.0);
        SurfaceGrowthRate surfaceGrowthRate = sourceMesh.getDefaultValues().get(SurfaceGrowthRate.class);
        surfaceGrowthRate.setGrowthRateOption(SurfaceGrowthRate.GrowthRateOption.USER_SPECIFIED);
        surfaceGrowthRate.getGrowthRateScalar().setValueAndUnits(1.05, dimensionlessUnits);
        PrismAutoMesher prismLayerMesher = (PrismAutoMesher) sourceMesh.getMeshers().getObject("Prism Layer Mesher");
        prismLayerMesher.getPrismStretchingOption().setSelected(PrismStretchingOption.Type.WALL_THICKNESS);
        NumPrismLayers numPrismLayers = sourceMesh.getDefaultValues().get(NumPrismLayers.class);
        numPrismLayers.getNumLayersValue().getQuantity().setValue(25.0);
        sourceMesh.getDefaultValues().get(PrismWallThickness.class).setValueAndUnits(0.008, mm);
        sourceMesh.getDefaultValues().get(PrismThickness.class).getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        sourceMesh.getDefaultValues().get(PrismThickness.class).getAbsoluteSizeValue().setValueAndUnits(20.0, mm);
        sourceMesh.getDefaultValues().get(PrismLayerReductionPercentage.class).setValue(10.0);
        sourceMesh.getDefaultValues().get(PrismLayerCoreLayerAspectRatio.class).setValue(0.8);
        sourceMesh.getDefaultValues().get(PrismLayerMinimumThickness.class).setValue(2.0);

        SurfaceCustomMeshControl freestreamControl = sourceMesh.getCustomMeshControls().createSurfaceControl();
        freestreamControl.setPresentationName("FREESTREAM");
        freestreamControl.getGeometryObjects().setQuery(null);
        freestreamControl.getGeometryObjects().setObjects(freestreamControlSurfaces);
        freestreamControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        PartsTargetSurfaceSize freestreamTarget = freestreamControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        freestreamTarget.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        freestreamTarget.getAbsoluteSizeValue().setValueAndUnits(5.0, m);

        SurfaceCustomMeshControl trailingEdgeControl = sourceMesh.getCustomMeshControls().createSurfaceControl();
        trailingEdgeControl.setPresentationName("TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setQuery(null);
        trailingEdgeControl.getGeometryObjects().setObjects(trailingEdgeSurface);
        trailingEdgeControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        PartsTargetSurfaceSize trailingEdgeTargetSize = trailingEdgeControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        trailingEdgeTargetSize.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        trailingEdgeTargetSize.getAbsoluteSizeValue().setDefinition("${P_TE_Thickness}/10.0");

        VolumeCustomMeshControl volumeRef1 = sourceMesh.getCustomMeshControls().createVolumeControl();
        volumeRef1.getGeometryObjects().setQuery(null);
        SolidModelPart refPart = (SolidModelPart) _sim.get(SimulationPartManager.class).getPart("Ref1");
        volumeRef1.getGeometryObjects().setObjects(refPart);
        VolumeControlResurfacerSizeOption volControlSurfaceSizeOption = volumeRef1.getCustomConditions().get(VolumeControlResurfacerSizeOption.class);
        volControlSurfaceSizeOption.setVolumeControlBaseSizeOption(true);
        VolumeControlSize volumeControlSize = volumeRef1.getCustomValues().get(VolumeControlSize.class);
        volumeControlSize.getRelativeSizeScalar().setValueAndUnits(30.0, dimensionlessUnits);

        directedMeshOp.getDirectedMeshDistributionManager().createDirectedMeshDistribution(directedMeshParts2Mesh, "Constant");
        DirectedMeshDistribution directedMeshDistribution = (DirectedMeshDistribution) directedMeshOp.getDirectedMeshDistributionManager().getObject("Volume Distribution");
        DirectedMeshNumLayers directedMeshNumLayers = directedMeshDistribution.getDefaultValues().get(DirectedMeshNumLayers.class);
        directedMeshNumLayers.setNumLayers(1);

        AutoMeshOperation meshOp = _sim.get(MeshOperationManager.class).createAutoMeshOperation(new StringVector(new String[]{"star.resurfacer.ResurfacerAutoMesher", "star.meshing.GeometricSensitivityAutoMesher"}), parts2Mesh);
        meshOp.setPresentationName("Geometric Sensitivity");
        ResurfacerAutoMesher resurfaceMesher = (ResurfacerAutoMesher) meshOp.getMeshers().getObject("Surface Remesher");
        resurfaceMesher.setDoProximityRefinement(false);
        meshOp.getDefaultValues().get(BaseSize.class).setDefinition("0.01*${C_Chord}");
        targetSize = meshOp.getDefaultValues().get(PartsTargetSurfaceSize.class);
        targetSize.getRelativeSizeScalar().setValueAndUnits(10.0, dimensionlessUnits);
        minimumSize = meshOp.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        minimumSize.getRelativeSizeScalar().setValueAndUnits(0.5, dimensionlessUnits);
        curvatureRefinement = meshOp.getDefaultValues().get(SurfaceCurvature.class);
        curvatureRefinement.setNumPointsAroundCircle(72.0);
        surfaceGrowthRate = meshOp.getDefaultValues().get(SurfaceGrowthRate.class);
        surfaceGrowthRate.setGrowthRateOption(SurfaceGrowthRate.GrowthRateOption.USER_SPECIFIED);
        surfaceGrowthRate.getGrowthRateScalar().setValueAndUnits(1.05, dimensionlessUnits);
        GeometricSensitivityOptions geomSensitivityOpts = meshOp.getDefaultValues().get(GeometricSensitivityOptions.class);
        geomSensitivityOpts.getParameters().setQuery(null);
        geomSensitivityOpts.getParameters().setObjects(getGeometricSensParams());
        geomSensitivityOpts.getStepSize().setValue(0.035);
        geomSensitivityOpts.getMinStep().setValue(0.0035);

        freestreamControl = meshOp.getCustomMeshControls().createSurfaceControl();
        freestreamControl.setPresentationName("FREESTREAM");
        freestreamControl.getGeometryObjects().setQuery(null);
        freestreamControl.getGeometryObjects().setObjects(freestreamControlSurfaces);
        freestreamControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        freestreamTarget = freestreamControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        freestreamTarget.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        freestreamTarget.getAbsoluteSizeValue().setValueAndUnits(5.0, m);

        trailingEdgeControl = meshOp.getCustomMeshControls().createSurfaceControl();
        trailingEdgeControl.setPresentationName("TRAILING_EDGE");
        trailingEdgeControl.getGeometryObjects().setQuery(null);
        trailingEdgeControl.getGeometryObjects().setObjects(trailingEdgeSurface);
        trailingEdgeControl.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        trailingEdgeTargetSize = trailingEdgeControl.getCustomValues().get(PartsTargetSurfaceSize.class);
        trailingEdgeTargetSize.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.ABSOLUTE);
        trailingEdgeTargetSize.getAbsoluteSizeValue().setDefinition("${P_TE_Thickness}/10.0");
    }
    
    private ArrayList<ScalarGlobalParameter> getGeometricSensParams() {
        ArrayList<ScalarGlobalParameter> params = new ArrayList<>();
        for (ScalarGlobalParameter sgp : _sim.get(GlobalParameterManager.class).getObjectsOf(ScalarGlobalParameter.class)) {
            if (sgp.getPresentationName().startsWith("P_")) {
                params.add(sgp);
            }
        }
        
        return params;
    }

    private CadModel getCADModel(String presentationName) {
        SolidModelManager cadManager = _sim.get(SolidModelManager.class);
        CadModel cad = null;

        for (CadModelBase cmb : cadManager.getObjects()) {
            if (cmb.getPresentationName().equals(presentationName)) {
                cad = (CadModel) cmb;
                break;
            }
        }

        if (cad == null) {
            cad = (CadModel) _sim.get(SolidModelManager.class).createSolidModel();
            cad.setPresentationName(presentationName);
        }

        return cad;
    }

    private void applyPlotOptions(StarPlot plot) {
        MultiColLegend legend = plot.getLegend();
        legend.setFont(new java.awt.Font("Siemens Sans Global", 0, 24));
        Cartesian2DAxisManager cartesian2DAxisManager = (Cartesian2DAxisManager) plot.getAxisManager();
        plot.setAspectRatio(AspectRatioEnum.RATIO_4_3);
        plot.setTitleFont(new java.awt.Font("Siemens Sans Global", 0, 30));
        plot.getLegend().setPositionInfo(0.9, 0.8, ChartPositionOption.Type.NORTH_EAST);

        for (Axis axis : cartesian2DAxisManager.getAxes()) {
            if (axis instanceof Cartesian2DAxis) {
                Cartesian2DAxis cartesianAxis = (Cartesian2DAxis) axis;
                AxisTitle axisTitle = cartesianAxis.getTitle();
                axisTitle.setFont(new java.awt.Font("Siemens Sans Global", 0, 24));
                AxisLabels axisLabels = cartesianAxis.getLabels();
                axisLabels.setFont(new java.awt.Font("Siemens Sans Global", 0, 24));
                axisLabels.setGridColor(new DoubleVector(new double[]{0.7843137383460999, 0.7843137383460999, 0.7843137383460999}));
                axisLabels.getGridLinePatternOption().setSelected(LinePatternOption.Type.SOLID);
                axisLabels.setGridWidth(1);
                AxisTicks axisTicks = cartesianAxis.getTicks();
                axisTicks.setCount(3);
                axisTicks.setGridColor(new DoubleVector(new double[]{0.8627451062202454, 0.8627451062202454, 0.8627451062202454}));
                axisTicks.getGridLinePatternOption().setSelected(LinePatternOption.Type.NONE);
                axisTicks.setGridWidth(1);
            }
        }

        if (plot instanceof MonitorPlot) {
            MonitorPlot monitorPlot = (MonitorPlot) plot;

            for (MonitorDataSet mds : monitorPlot.getDataSetManager().getObjectsOf(MonitorDataSet.class)) {
                LineStyle lineStyle = mds.getLineStyle();
                lineStyle.setLineWidth(2.0);
                SymbolStyleWithSpacing symbolStyle = mds.getSymbolStyle();
                symbolStyle.setSize(10);
            }

            if (monitorPlot instanceof ResidualPlot) {
                monitorPlot.setAspectRatio(AspectRatioEnum.RATIO_16_10);
            }
        }
    }

    private enum Type {
        TWO_D, THREE_D, THREE_D_DIRECTED_MESH;
    }
}
