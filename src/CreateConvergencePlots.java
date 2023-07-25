// Simcenter STAR-CCM+ macro: Convergence.java
// Written by Simcenter STAR-CCM+ 16.06.007

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;
import star.common.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.graph.DataSet;

public class CreateConvergencePlots extends StarMacro {

    boolean _clean_up = true;
    boolean _createAsy = true;
    boolean _createOsc = true;

    String[] _reportNames = {"CD", "CL"};

    /* MONOTONIC CONVERGENCE PROPERTIES*/
    double _asymptotic_range = 5.0E-5;    //Relative asymptotic convergence crit
    int _n_samples = 50;

    /* OSCILATING CONVERGENCE PROPERTIES*/
    int[] _moving_avgs = new int[]{5000, 10000};
    double _oscill_range = 5.0e-4;

    /* DO NOT EDIT BELOW*/
    private final String _conv_crit_param_name_1 = "conv_crit_asympt";
    private final String _conv_crit_param_name_2 = "conv_crit_oscill";
    private final String _converged_report_name = "Converged";
    private final String _asymptotic_tag = "Asympt";
    private final String _oscillating_tag = "Oscill";
    private final String _convergence_tag = "Conv";
    private final String _max_tag = "Max";
    private final String _mean_tag = "Mean";
    private final String _min_tag = "Min";
    private final String _limit_tag = "Limit";
    private final String _expression_report_tag = "ER";
    
    /* Tag indicates an object was created by CreateConvergencePlots java macro*/
    private final String _tag_name = "CBCCPJAVA";
    HashMap<String, ReportMonitor> _monitors = new HashMap<>();
    Simulation _sim;
    TagManager _tagManager;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _tagManager = _sim.get(TagManager.class);

        try {
            if (_clean_up) {
                deleteTaggedObjects();
            }
            if (_createAsy) {
                createAsymConvCritParameter();
                for (String s : _reportNames) {
                    Report r = _sim.getReportManager().getReport(s);
                    createAsymStatisticsReports(r);
                    createAsymConvReport(r);
                    createAsymMonitorPlot(r);
                }
                createAsymConvReport();
                createAsymStoppingCriteria();
            }

            if (_createOsc) {
                createOscillConvCritParameter();
                for (String s : _reportNames) {
                    Report r = _sim.getReportManager().getReport(s);
                    for (int mai : _moving_avgs) {
                        createOscillMAStatisticsReports(r, mai);
                        createOscMAConvReport(r, mai);
                        createOscillMonitorPlot(r, mai);
                    }
                }
                for (int mai : _moving_avgs) {
                    createOscillMAConvReport(mai);
                    createOscillMAStoppingCriteria(mai);
                }
            }
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void createAsymConvCritParameter() {
        GlobalParameterManager man = _sim.get(GlobalParameterManager.class);
        for (ScalarGlobalParameter sgp : man.getObjectsOf(ScalarGlobalParameter.class)) {
            if (sgp.getPresentationName().equals(_conv_crit_param_name_1)) {
                sgp.getQuantity().setValue(_asymptotic_range);
                return;
            }
        }
        _sim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
        ScalarGlobalParameter convParam = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Scalar");
        convParam.setPresentationName(_conv_crit_param_name_1);
        convParam.getQuantity().setValue(_asymptotic_range);
        _tagManager.addTags(convParam, getTag());
    }

    private void createAsymStatisticsReports(Report r) {
        ReportMonitor rm = getReportMonitor(r);
        StatisticsReport maxxReport = _sim.getReportManager().createReport(StatisticsReport.class);
        StatisticsReport meanReport = _sim.getReportManager().createReport(StatisticsReport.class);
        StatisticsReport minnReport = _sim.getReportManager().createReport(StatisticsReport.class);

        maxxReport.setMonitor(rm);
        maxxReport.setPresentationName(r.getPresentationName() + " " + _asymptotic_tag + " " + _max_tag);
        maxxReport.setStatisticOption(StatisticOption.Maximum);
        maxxReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter maxxNSamplesFilter = (LastNSamplesFilter) maxxReport.getSampleFilterManager().getObject("Last N Samples");
        maxxNSamplesFilter.setNSamples(_n_samples);

        meanReport.setMonitor(rm);
        meanReport.setPresentationName(r.getPresentationName() + " " + _asymptotic_tag + " " + _mean_tag);
        meanReport.setStatisticOption(StatisticOption.Mean);
        meanReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter meanNSamplesFilter = (LastNSamplesFilter) meanReport.getSampleFilterManager().getObject("Last N Samples");
        meanNSamplesFilter.setNSamples(_n_samples);

        minnReport.setMonitor(rm);
        minnReport.setPresentationName(r.getPresentationName() + " " + _asymptotic_tag + " " + _min_tag);
        minnReport.setStatisticOption(StatisticOption.Minimum);
        minnReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter minnNSamplesFilter = (LastNSamplesFilter) minnReport.getSampleFilterManager().getObject("Last N Samples");
        minnNSamplesFilter.setNSamples(_n_samples);

        _tagManager.addTags(maxxReport, getTag());
        _tagManager.addTags(meanReport, getTag());
        _tagManager.addTags(minnReport, getTag());
        ReportMonitor maxxRM = maxxReport.createMonitor();
        ReportMonitor meanRM = meanReport.createMonitor();
        ReportMonitor minnRM = minnReport.createMonitor();
        _tagManager.addTags(maxxRM, getTag());
        _tagManager.addTags(meanRM, getTag());
        _tagManager.addTags(minnRM, getTag());
        _monitors.put(maxxReport.getPresentationName(), maxxRM);
        _monitors.put(meanReport.getPresentationName(), meanRM);
        _monitors.put(minnReport.getPresentationName(), minnRM);
    }

    private void createAsymConvReport(Report r) {
        ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
        er.setPresentationName(r.getPresentationName() + " " + _asymptotic_tag + " " + _convergence_tag);
        er.setDefinition("${Iteration} < 10 ? 100 "
                + " : (alternateValue(${" + r.getPresentationName() + " " + _asymptotic_tag + " " + _max_tag + "}, 100)"
                + " -  alternateValue(${" + r.getPresentationName() + " " + _asymptotic_tag + " " + _min_tag + "}, 0))"
                + " /  alternateValue(${" + r.getPresentationName() + " " + _asymptotic_tag + " " + _mean_tag + "}, 1)/${" + _conv_crit_param_name_1 + "}");
        ReportMonitor erRM = er.createMonitor();
        _tagManager.addTags(er, getTag());
        _tagManager.addTags(erRM, getTag());
        _monitors.put(er.getPresentationName(), erRM);

        for (ExpressionReport eri : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
            if (eri.getPresentationName().equals(_converged_report_name) && eri.getDefinition().equals("1")) {
                for (ReportMonitor rmi : _sim.getMonitorManager().getObjectsOf(ReportMonitor.class)) {
                    if (rmi.getReport().getPresentationName().equals(eri.getPresentationName())) {
                        _monitors.put(eri.getPresentationName(), rmi);
                        return;
                    }
                }
            }
        }

        ExpressionReport er2 = _sim.getReportManager().createReport(ExpressionReport.class);
        er2.setPresentationName(_converged_report_name);
        er2.setDefinition("1");
        ReportMonitor er2RM = er2.createMonitor();
        _tagManager.addTags(er2, getTag());
        _tagManager.addTags(er2RM, getTag());
        _monitors.put(er2.getPresentationName(), er2RM);
    }

    private void createAsymMonitorPlot(Report r) {
        String root = r.getPresentationName();

        Iterator<Entry<String, ReportMonitor>> iterator = _monitors.entrySet().iterator();
        Collection<Monitor> plotMonitors = new ArrayList<>();
        while (iterator.hasNext()) {
            Entry<String, ReportMonitor> entry = iterator.next();
            if (entry.getKey().contains(r.getPresentationName())) {
                plotMonitors.add(entry.getValue());
            } else if (entry.getKey().contains(_converged_report_name)) {
                plotMonitors.add(entry.getValue());
            }
        }

        if (!plotMonitors.contains(getReportMonitor(r))) {
            plotMonitors.add(getReportMonitor(r));
        }

        MonitorPlot mp = _sim.getPlotManager().createMonitorPlot(plotMonitors, root + " Asy Convergence");
        _tagManager.addTags(mp, getTag());

        Cartesian2DAxisManager axisManager = ((Cartesian2DAxisManager) mp.getAxisManager());
        Cartesian2DAxis leftAxis = ((Cartesian2DAxis) axisManager.getAxis("Left Axis"));
        AxisTitle leftAxisTitle = leftAxis.getTitle();
        leftAxisTitle.setText(r.getPresentationName());
        Cartesian2DAxis rightAxis = (Cartesian2DAxis) axisManager.createAxis(Cartesian2DAxis.Position.Right);
        AxisTitle rightAxisTitle = rightAxis.getTitle();
        rightAxisTitle.setText("Asymptote");
        rightAxis.setLogarithmic(true);
        rightAxis.getLabels().setGridVisible(false);

        axisManager.setAxesBounds(new Vector(Arrays.<AxisManager.AxisBounds>asList(new AxisManager.AxisBounds("Left Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Bottom Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Right Axis", 0.1, true, 1000000, true))));
        MonitorDataSet mainMonitor = null;
        MonitorDataSet maxMonitor = null;
        MonitorDataSet meanMonitor = null;
        MonitorDataSet minMonitor = null;
        MonitorDataSet convMonitor = null;
        MonitorDataSet convergedMonitor = null;

        for (DataSet mds : mp.getDataSetManager().getObjects()) {
            if (mds.getName().contains(_asymptotic_tag + " " + _max_tag)) {
                maxMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().contains(_asymptotic_tag + " " + _min_tag)) {
                minMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().contains(_asymptotic_tag + " " + _mean_tag)) {
                meanMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().contains(_asymptotic_tag + " " + _convergence_tag)) {
                convMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().contains(_converged_report_name)) {
                convergedMonitor = (MonitorDataSet) mds;
            } else {
                mainMonitor = (MonitorDataSet) mds;
            }
        }

        mp.setDataSeriesOrder(new NeoObjectVector(new Object[]{mainMonitor, maxMonitor, meanMonitor, minMonitor, convMonitor, convergedMonitor}));
        maxMonitor.setSeriesNameLocked(true);
        maxMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _max_tag);
        meanMonitor.setSeriesNameLocked(true);
        meanMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _mean_tag);
        minMonitor.setSeriesNameLocked(true);
        minMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _min_tag);
        mainMonitor.setSeriesNameLocked(true);
        mainMonitor.setSeriesName(root);
        convMonitor.setSeriesNameLocked(true);
        convMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _convergence_tag);
        convergedMonitor.setSeriesNameLocked(true);
        convergedMonitor.setSeriesName(_converged_report_name);

        convMonitor.setYAxis(rightAxis);
        convergedMonitor.setYAxis(rightAxis);
        MultiColLegend multiColLegend_0 = mp.getLegend();
        multiColLegend_0.setPositionInfo(0.8894736766815186, 0.7751479148864746, ChartPositionOption.Type.NORTH_EAST);

        LineStyle lineStyle_0 = maxMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.8899999856948853, 0.07000000029802322, 0.1899999976158142}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = meanMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.5, 0.5, 0.4099999964237213}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = minMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.2824000120162964, 0.23919999599456787, 0.5450999736785889}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = mainMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.0, 0.0, 0.0}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = convMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.2199999988079071, 0.3700000047683716, 0.05999999865889549}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = convergedMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.8980392217636108, 0.7529411911964417, 0.2980392277240753}));
        lineStyle_0.setLineWidth(2.0);
    }

    private void createAsymConvReport() {
        ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
        er.setPresentationName(_asymptotic_tag + " " + _convergence_tag);
        String erDef = "";

        int counter = 0;
        for (String name : _reportNames) {
            if (counter < _reportNames.length - 1) {
                erDef += "ceil(${" + name + " " + _asymptotic_tag + " " + _convergence_tag + "}) + ";
            } else {
                erDef += "ceil(${" + name + " " + _asymptotic_tag + " " + _convergence_tag + "})";
            }
            counter++;
        }
        er.setDefinition(erDef);
        ReportMonitor rm = er.createMonitor();
        _tagManager.addTags(er, getTag());
        _tagManager.addTags(rm, getTag());
        _monitors.put(er.getPresentationName(), rm);
    }

    private void createAsymStoppingCriteria() {
        double converged_min = _reportNames.length + 0.5;
        ReportMonitor rm = (ReportMonitor) _sim.getMonitorManager().getMonitor(_asymptotic_tag + " " + _convergence_tag + " Monitor");
        SteadySolver steadySolver = ((SteadySolver) _sim.getSolverManager().getSolver(SteadySolver.class));
        MonitorIterationStoppingCriterion minSC = steadySolver.getSolverStoppingCriterionManager().createIterationStoppingCriterion(rm);
        minSC.setPresentationName("Asymptotic Convergence");
        ((MonitorIterationStoppingCriterionOption) minSC.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.Type.MINIMUM);
        minSC.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.OR);
        MonitorIterationStoppingCriterionMinLimitType minLimitType = ((MonitorIterationStoppingCriterionMinLimitType) minSC.getCriterionType());
        minLimitType.getLimit().setValue(converged_min);
        _tagManager.addTags(minSC, getTag());
    }
    
    private void createOscillConvCritParameter() {
        GlobalParameterManager man = _sim.get(GlobalParameterManager.class);
        for (ScalarGlobalParameter sgp : man.getObjectsOf(ScalarGlobalParameter.class)) {
            if (sgp.getPresentationName().equals(_conv_crit_param_name_2)) {
                sgp.getQuantity().setValue(_oscill_range);
                return;
            }
        }
        _sim.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");
        ScalarGlobalParameter convParam = (ScalarGlobalParameter) _sim.get(GlobalParameterManager.class).getObject("Scalar");
        convParam.setPresentationName(_conv_crit_param_name_2);
        convParam.getQuantity().setValue(_oscill_range);
        _tagManager.addTags(convParam, getTag());
    }
    
    private void createOscillMAStatisticsReports(Report r, int ma) {
        String mas = Integer.toString(ma);
        ReportMonitor rm = getReportMonitor(r);
        StatisticsReport maxxReport = _sim.getReportManager().createReport(StatisticsReport.class);
        StatisticsReport meanReport = _sim.getReportManager().createReport(StatisticsReport.class);
        StatisticsReport minnReport = _sim.getReportManager().createReport(StatisticsReport.class);
        ExpressionReport meanExpRpt = _sim.getReportManager().createReport(ExpressionReport.class);
        
        meanReport.setMonitor(rm);
        meanReport.setPresentationName(r.getPresentationName() + " " + mas + " MA " + _mean_tag);
        meanReport.setStatisticOption(StatisticOption.Mean);
        meanReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter meanNSamplesFilter = (LastNSamplesFilter) meanReport.getSampleFilterManager().getObject("Last N Samples");
        meanNSamplesFilter.setNSamples(ma);
        
        meanExpRpt.setPresentationName(r.getPresentationName() + " " + mas + " MA " + _expression_report_tag);
        meanExpRpt.setDefinition("${" + meanReport.getPresentationName() + "}");
        ReportMonitor meanExpRM = meanExpRpt.createMonitor();
        _tagManager.addTags(meanExpRpt, getTag());
        _tagManager.addTags(meanExpRM, getTag());
        _monitors.put(meanExpRpt.getPresentationName(), meanExpRM);        

        maxxReport.setMonitor(meanExpRM);
        maxxReport.setPresentationName(r.getPresentationName() + " " + mas + " MA " + _max_tag);
        maxxReport.setStatisticOption(StatisticOption.Maximum);
        maxxReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter maxxNSamplesFilter = (LastNSamplesFilter) maxxReport.getSampleFilterManager().getObject("Last N Samples");
        maxxNSamplesFilter.setNSamples(ma);

        minnReport.setMonitor(meanExpRM);
        minnReport.setPresentationName(r.getPresentationName() + " " + mas + " MA " + _min_tag);
        minnReport.setStatisticOption(StatisticOption.Minimum);
        minnReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter minnNSamplesFilter = (LastNSamplesFilter) minnReport.getSampleFilterManager().getObject("Last N Samples");
        minnNSamplesFilter.setNSamples(ma);

        _tagManager.addTags(maxxReport, getTag());
        _tagManager.addTags(meanReport, getTag());
        _tagManager.addTags(minnReport, getTag());
        ReportMonitor maxxRM = maxxReport.createMonitor();
        ReportMonitor meanRM = meanReport.createMonitor();
        ReportMonitor minnRM = minnReport.createMonitor();
        _tagManager.addTags(maxxRM, getTag());
        _tagManager.addTags(meanRM, getTag());
        _tagManager.addTags(minnRM, getTag());
        _monitors.put(maxxReport.getPresentationName(), maxxRM);
        _monitors.put(meanReport.getPresentationName(), meanRM);
        _monitors.put(minnReport.getPresentationName(), minnRM);
    }
    
    private void createOscMAConvReport(Report r, int ma) {
        String mas = Integer.toString(ma);
        ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
        er.setPresentationName(r.getPresentationName() + " " + mas + " MA " + _asymptotic_tag + " " + _convergence_tag);
        er.setDefinition("${Iteration} < 10 ? 100 "
                + " : (alternateValue(${" + r.getPresentationName() + " " + mas + " MA " + _max_tag + "}, 100)"
                + " -  alternateValue(${" + r.getPresentationName() + " " + mas + " MA " + _min_tag + "}, 0))"
                + " /  alternateValue(${" + r.getPresentationName() + " " + mas + " MA " + _mean_tag + "}, 1)/${" + _conv_crit_param_name_2 + "}");
        ReportMonitor erRM = er.createMonitor();
        _tagManager.addTags(er, getTag());
        _tagManager.addTags(erRM, getTag());
        _monitors.put(er.getPresentationName(), erRM);

        for (ExpressionReport eri : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
            if (eri.getPresentationName().equals(_converged_report_name) && eri.getDefinition().equals("1")) {
                for (ReportMonitor rmi : _sim.getMonitorManager().getObjectsOf(ReportMonitor.class)) {
                    if (rmi.getReport().getPresentationName().equals(eri.getPresentationName())) {
                        _monitors.put(eri.getPresentationName(), rmi);
                        return;
                    }
                }
            }
        }

        ExpressionReport er2 = _sim.getReportManager().createReport(ExpressionReport.class);
        er2.setPresentationName(_converged_report_name);
        er2.setDefinition("1");
        ReportMonitor er2RM = er2.createMonitor();
        _tagManager.addTags(er2, getTag());
        _tagManager.addTags(er2RM, getTag());
        _monitors.put(er2.getPresentationName(), er2RM);
    }
    
    private void createOscillMonitorPlot(Report r, int ma) {
        String root = r.getPresentationName() + " " + Integer.toString(ma);

        Iterator<Entry<String, ReportMonitor>> iterator = _monitors.entrySet().iterator();
        Collection<Monitor> plotMonitors = new ArrayList<>();
        while (iterator.hasNext()) {
            Entry<String, ReportMonitor> entry = iterator.next();
            if (entry.getKey().contains(root)) {
                plotMonitors.add(entry.getValue());
            } else if (entry.getKey().contains(_converged_report_name)) {
                plotMonitors.add(entry.getValue());
            }
        }

        if (!plotMonitors.contains(getReportMonitor(r))) {
            plotMonitors.add(getReportMonitor(r));
        }

        MonitorPlot mp = _sim.getPlotManager().createMonitorPlot(plotMonitors, root + " Asy Convergence");
        _tagManager.addTags(mp, getTag());

        Cartesian2DAxisManager axisManager = ((Cartesian2DAxisManager) mp.getAxisManager());
        Cartesian2DAxis leftAxis = ((Cartesian2DAxis) axisManager.getAxis("Left Axis"));
        AxisTitle leftAxisTitle = leftAxis.getTitle();
        leftAxisTitle.setText(r.getPresentationName());
        Cartesian2DAxis rightAxis = (Cartesian2DAxis) axisManager.createAxis(Cartesian2DAxis.Position.Right);
        AxisTitle rightAxisTitle = rightAxis.getTitle();
        rightAxisTitle.setText("Asymptote");
        rightAxis.setLogarithmic(true);
        rightAxis.getLabels().setGridVisible(false);

        axisManager.setAxesBounds(new Vector(Arrays.<AxisManager.AxisBounds>asList(new AxisManager.AxisBounds("Left Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Bottom Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Right Axis", 0.1, true, 1000000, true))));
        MonitorDataSet mainMonitor = null;
        MonitorDataSet maxMonitor = null;
        MonitorDataSet meanMonitor = null;
        MonitorDataSet minMonitor = null;
        MonitorDataSet convMonitor = null;
        MonitorDataSet convergedMonitor = null;

        for (DataSet mds : mp.getDataSetManager().getObjects()) {
            if (mds.getName().endsWith(_max_tag + " Monitor")) {
                maxMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().endsWith(_min_tag + " Monitor")) {
                minMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().endsWith(_mean_tag + " Monitor")) {
                meanMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().endsWith(_convergence_tag + " Monitor")) {
                convMonitor = (MonitorDataSet) mds;
            } else if (mds.getName().contains(_converged_report_name)) {
                convergedMonitor = (MonitorDataSet) mds;
            } else {
                mainMonitor = (MonitorDataSet) mds;
            }
        }

        mp.setDataSeriesOrder(new NeoObjectVector(new Object[]{mainMonitor, maxMonitor, meanMonitor, minMonitor, convMonitor, convergedMonitor}));
        maxMonitor.setSeriesNameLocked(true);
        maxMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _max_tag);
        meanMonitor.setSeriesNameLocked(true);
        meanMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _mean_tag);
        minMonitor.setSeriesNameLocked(true);
        minMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _min_tag);
        mainMonitor.setSeriesNameLocked(true);
        mainMonitor.setSeriesName(root);
        convMonitor.setSeriesNameLocked(true);
        convMonitor.setSeriesName(root + " " + _asymptotic_tag + " " + _convergence_tag);
        convergedMonitor.setSeriesNameLocked(true);
        convergedMonitor.setSeriesName(_converged_report_name);

        convMonitor.setYAxis(rightAxis);
        convergedMonitor.setYAxis(rightAxis);
        MultiColLegend multiColLegend_0 = mp.getLegend();
        multiColLegend_0.setPositionInfo(0.8894736766815186, 0.7751479148864746, ChartPositionOption.Type.NORTH_EAST);

        LineStyle lineStyle_0 = maxMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.8899999856948853, 0.07000000029802322, 0.1899999976158142}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = meanMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.5, 0.5, 0.4099999964237213}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = minMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.2824000120162964, 0.23919999599456787, 0.5450999736785889}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = mainMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.0, 0.0, 0.0}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = convMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.2199999988079071, 0.3700000047683716, 0.05999999865889549}));
        lineStyle_0.setLineWidth(2.0);
        lineStyle_0 = convergedMonitor.getLineStyle();
        lineStyle_0.setColor(new DoubleVector(new double[]{0.8980392217636108, 0.7529411911964417, 0.2980392277240753}));
        lineStyle_0.setLineWidth(2.0);
    }
    
    private void createOscillMAConvReport(int ma) {
        String mas = Integer.toString(ma);
        ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
        er.setPresentationName(mas + " " + _oscillating_tag + " " + _convergence_tag);
        String erDef = "${Iteration} < " + Integer.toString(ma) + "? 100 :\n";

        int maxN = _reportNames.length - 1;
        int counter = 0;
        for (String name : _reportNames) {
            String namei = name + " " + mas + " MA";
            if (counter < maxN) {
                erDef += "ceil(${" + namei + " " + _asymptotic_tag + " " + _convergence_tag + "}) + ";
            } else {
                erDef += "ceil(${" + namei + " " + _asymptotic_tag + " " + _convergence_tag + "})";
            }
            counter++;
        }
        er.setDefinition(erDef);
        ReportMonitor rm = er.createMonitor();
        _tagManager.addTags(er, getTag());
        _tagManager.addTags(rm, getTag());
        _monitors.put(er.getPresentationName(), rm);
    }
    
    private void createOscillMAStoppingCriteria(int ma) {
        String root = Integer.toString(ma) + " " + _oscillating_tag + " " + _convergence_tag;
        double converged_min = _reportNames.length + 0.5;
        ReportMonitor rm = (ReportMonitor) _sim.getMonitorManager().getMonitor(root + " Monitor");
        SteadySolver steadySolver = ((SteadySolver) _sim.getSolverManager().getSolver(SteadySolver.class));
        MonitorIterationStoppingCriterion minSC = steadySolver.getSolverStoppingCriterionManager().createIterationStoppingCriterion(rm);
        minSC.setPresentationName(Integer.toString(ma) + " Oscillating MA Convergence");
        ((MonitorIterationStoppingCriterionOption) minSC.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.Type.MINIMUM);
        minSC.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.OR);
        MonitorIterationStoppingCriterionMinLimitType minLimitType = ((MonitorIterationStoppingCriterionMinLimitType) minSC.getCriterionType());
        minLimitType.getLimit().setValue(converged_min);
        _tagManager.addTags(minSC, getTag());
    }
    
    private ReportMonitor getReportMonitor(Report r) {
        for (Monitor m : _sim.getMonitorManager().getMonitors()) {
            if (m instanceof ReportMonitor) {
                ReportMonitor rm = (ReportMonitor) m;
                if (rm.getReport().getPresentationName().equals(r.getPresentationName())) {
                    return rm;
                }
            }
        }
        return r.createMonitor();
    }    

    private void groupReports() {
        for (String s : _reportNames) {
            Report r = _sim.getReportManager().getReport(s);
            Collection<ClientServerObjectManager<ClientServerObject>> groupsManager = _sim.getReportManager().getGroupsManager().getObjects();
            for (ClientServerObjectManager<ClientServerObject> csoMan : groupsManager) {
                if (csoMan instanceof ClientServerObjectGroup) {
                    ClientServerObjectGroup csoGroup = (ClientServerObjectGroup) csoMan;
                    for (ClientServerObject csoi : csoGroup.getGroupsManager().getObjects()) {
                        _sim.println(csoi.getPresentationName() + " : " + csoi.getClass().getSimpleName());
                    }
                }

            }
        }
    }
    
    /* Methods for identifying and deleting objects created by this macro*/
    private Collection<Tag> getTag() {
        Collection<Tag> tagCollection = new ArrayList<>();
        for (Tag ti : _sim.get(TagManager.class).getObjects()) {
            if (ti.getPresentationName().equals(_tag_name)) {
                tagCollection = new ArrayList<>();
                tagCollection.add(ti);
                return tagCollection;
            }
        }

        Tag createdBytag = _sim.get(TagManager.class).createNewUserTag(_tag_name);
        tagCollection.add(createdBytag);
        return tagCollection;
    }

    private Tag getTagAsTag() {
        return getTag().toArray(new Tag[1])[0];
    }

    private void deleteTaggedObjects() {
        //Delete Tagged Stopping Criteria
        SteadySolver steadySolver = _sim.getSolverManager().getSolver(SteadySolver.class);
        Collection<MonitorIterationStoppingCriterion> toDeleteSC = new ArrayList<>();
        Collection<MonitorIterationStoppingCriterion> criteria = steadySolver.getSolverStoppingCriterionManager().getObjectsOf(MonitorIterationStoppingCriterion.class);
        for (MonitorIterationStoppingCriterion misc : criteria) {
            if (misc.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteSC.add(misc);
            }
        }
        steadySolver.getSolverStoppingCriterionManager().deleteChildren(toDeleteSC);

        /*Delete Tagged Monitor Plots*/
        Collection<StarPlot> toDeleteSP = new ArrayList<>();
        for (StarPlot sp : _sim.getPlotManager().getPlots()) {
            if (sp.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteSP.add(sp);
            }
        }
        _sim.getPlotManager().deleteChildren(toDeleteSP);

        /*Delete Tagged Monitors*/
        boolean hasMonitorsToDelete = true;
        int count = 0;

        while (hasMonitorsToDelete && count < 10) {
            hasMonitorsToDelete = deleteMonitors();
            deleteExpressionANDStatisticsReports();
            count++;
        }

        /*Delete Tagged Reports*/
        Collection<ExpressionReport> toDeleteER = new ArrayList<>();
        for (ExpressionReport eri : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
            if (eri.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteER.add(eri);
            }
        }
        while (!toDeleteER.isEmpty()) {
            _sim.getReportManager().deleteChildren(deleteExpressionReports(toDeleteER));
            toDeleteER.clear();
            for (ExpressionReport eri : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
                if (eri.getTagGroup().getObjects().contains(getTagAsTag())) {
                    toDeleteER.add(eri);
                }
            }
        }

        Collection<Report> toDeleteR = new ArrayList<>();
        for (Report r : _sim.getReportManager().getObjects()) {
            if (r.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteR.add(r);
            }
        }
        _sim.getReportManager().deleteChildren(toDeleteR);


        /*Delete Tagged Parameters*/
        Collection<ScalarGlobalParameter> toDeleteSGP = new ArrayList<>();
        for (ScalarGlobalParameter sgp : _sim.get(GlobalParameterManager.class).getObjectsOf(ScalarGlobalParameter.class)) {
            if (sgp.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteSGP.add(sgp);
            }
        }
        _sim.get(GlobalParameterManager.class).deleteChildren(toDeleteSGP);
    }

    private Collection<ExpressionReport> deleteExpressionReports(Collection<ExpressionReport> expressionReports) {
        Collection<ExpressionReport> notReferenced = new ArrayList<>();

        for (ExpressionReport er : expressionReports) {
            String erName = er.getPresentationName();
            boolean found = false;
            for (ExpressionReport er2 : expressionReports) {
                if (er2.getDefinition().contains("${" + erName + "}")) {
                    found = true;
                }
            }
            if (!found) {
                notReferenced.add(er);
            }
        }

        return notReferenced;
    }

    private void deleteExpressionANDStatisticsReports() {
        Collection<StatisticsReport> toDeleteSR = new ArrayList<>();
        Collection<ExpressionReport> toDeleteER = new ArrayList<>();
        for (StatisticsReport sr : _sim.getReportManager().getObjectsOf(StatisticsReport.class)) {
            if (sr.getTagGroup().getObjects().contains(getTagAsTag())) {
                boolean inUse = false;
                for (ReportMonitor rm : _sim.getMonitorManager().getObjectsOf(ReportMonitor.class)) {
                    if (rm.getReport() != null) {
                        if (rm.getReport().equals(sr)) {
                            inUse = true;
                        }
                    }
                }
                for (ExpressionReport er : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
                    if (er.getDefinition().contains("${" + sr.getPresentationName() + "}")) {
                        inUse = true;
                    }
                }
                if (!inUse) {
                    toDeleteSR.add(sr);
                }
            }
        }
        for (ExpressionReport er : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
            if (er.getTagGroup().getObjects().contains(getTagAsTag())) {
                boolean inUse = false;
                for (ReportMonitor rm : _sim.getMonitorManager().getObjectsOf(ReportMonitor.class)) {
                    if (rm.getReport() != null) {
                        if (rm.getReport().equals(er)) {
                            inUse = true;
                        }
                    }
                }
                for (ExpressionReport eri : _sim.getReportManager().getObjectsOf(ExpressionReport.class)) {
                    if (eri.getDefinition().contains("${" + er.getPresentationName() + "}")) {
                        inUse = true;
                    }
                }
                if (!inUse) {
                    toDeleteER.add(er);
                }
            }
        }
        _sim.getReportManager().deleteChildren(toDeleteSR);
        _sim.getReportManager().deleteChildren(toDeleteER);
    }

    private boolean deleteMonitors() {
        Collection<Monitor> toDeleteM = new ArrayList<>();
        for (Monitor m : _sim.getMonitorManager().getMonitors()) {
            if (m.getTagGroup().getObjects().contains(getTagAsTag())) {
                boolean inUse = false;
                for (StatisticsReport sr : _sim.getReportManager().getObjectsOf(StatisticsReport.class)) {
                    if (sr.getMonitor() != null) {
                        if (sr.getMonitor().equals(m)) {
                            inUse = true;
                        }
                    }
                }
                if (!inUse) {
                    toDeleteM.add(m);
                }
            }
        }
        _sim.getMonitorManager().deleteChildren(toDeleteM);

        toDeleteM.clear();
        for (Monitor m : _sim.getMonitorManager().getMonitors()) {
            if (m.getTagGroup().getObjects().contains(getTagAsTag())) {
                toDeleteM.add(m);
            }
        }
        return !toDeleteM.isEmpty();
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }

    /* Old Methods for Oscill Conv */
    private void createOscStatisticsReport(Report r) {
        ReportMonitor rm = getReportMonitor(r);
//        StatisticsReport minReport = getMinOscStatisticsReport(r);
//        StatisticsReport maxReport = getMaxOscStatisticsReport(r);
//        StatisticsReport midReport = null;

        int counter = 1;
        for (int i : _moving_avgs) {
            StatisticsReport meanReport = _sim.getReportManager().createReport(StatisticsReport.class);
            ExpressionReport exprReport = _sim.getReportManager().createReport(ExpressionReport.class);
            meanReport.setMonitor(rm);
            meanReport.setPresentationName(r.getPresentationName() + " " + Integer.toString(i) + " MA");
            meanReport.setStatisticOption(StatisticOption.Mean);
            meanReport.setSampleFilterOption(SampleFilterOption.LastNSamples);
            LastNSamplesFilter meanNsamplesFilter = (LastNSamplesFilter) meanReport.getSampleFilterManager().getObject("Last N Samples");
            meanNsamplesFilter.setNSamples(i);
            exprReport.setPresentationName(meanReport.getPresentationName() + " " + _expression_report_tag);
            exprReport.setDefinition("${" + meanReport.getPresentationName() + "}");
            ReportMonitor meanMonitor = meanReport.createMonitor();
            ReportMonitor exprMonitor = exprReport.createMonitor();
//            if (counter == 1) {
//                midReport = meanReport;
//            }
            _tagManager.addTags(meanReport, getTag());
            _tagManager.addTags(meanMonitor, getTag());
            _tagManager.addTags(exprReport, getTag());
            _tagManager.addTags(exprMonitor, getTag());
            _monitors.put(meanReport.getPresentationName(), meanMonitor);
            _monitors.put(exprReport.getPresentationName(), exprMonitor);
            createOscillMAStatisticsReports(exprReport, i);
//            createOscMAConvReport(exprReport);
            createAsymMonitorPlot(exprReport);
            counter++;
        }

//        ExpressionReport minBoundReport = _sim.getReportManager().createReport(ExpressionReport.class);
//        minBoundReport.setPresentationName(minReport.getPresentationName() + " " + _limit_tag);
//        minBoundReport.setDefinition("${" + midReport.getPresentationName() + "} - (${" + midReport.getPresentationName() + "} - ${" + minReport.getPresentationName() + "})*0.75");
//        ReportMonitor minBndMonitor = minBoundReport.createMonitor();
//        _monitors.put(minBoundReport.getPresentationName(), minBndMonitor);
//        ExpressionReport maxBoundReport = _sim.getReportManager().createReport(ExpressionReport.class);
//        maxBoundReport.setDefinition("${" + midReport.getPresentationName() + "} + (${" + maxReport.getPresentationName() + "} - ${" + midReport.getPresentationName() + "})*0.75");
//        maxBoundReport.setPresentationName(maxReport.getPresentationName() + " " + _limit_tag);
//        ReportMonitor maxBndMonitor = maxBoundReport.createMonitor();
//        _monitors.put(maxBoundReport.getPresentationName(), maxBndMonitor);
//        _tagManager.addTags(minBoundReport, getTag());
//        _tagManager.addTags(maxBoundReport, getTag());
//        _tagManager.addTags(minBndMonitor, getTag());
//        _tagManager.addTags(maxBndMonitor, getTag());
    }

    private void createOscConvReport() {

        ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
        er.setPresentationName(_oscillating_tag + " " + _convergence_tag);
        String minIter4Oscill = Integer.toString(_moving_avgs[_moving_avgs.length - 1] + 100);

        String erDef = "($Iteration < " + minIter4Oscill + " ? 0 :\n";
        int linei = 0;
        for (String r : _reportNames) {
            String minReport = r + " " + _oscillating_tag + " " + _min_tag + " " + _limit_tag;
            String maxReport = r + " " + _oscillating_tag + " " + _max_tag + " " + _limit_tag;
            String[] meanReports = new String[_moving_avgs.length];
            for (int i = 0; i < _moving_avgs.length; i++) {
                meanReports[i] = r + " " + Integer.toString(_moving_avgs[i]) + " MA";
            }
            for (String s : meanReports) {
                if (linei == 0) {
                    erDef += "(${" + s + "} < ${" + maxReport + "} ? ${" + s + "} > ${" + minReport + "} ? 1 : 0 : 0)";
                } else {
                    erDef += " + \n(${" + s + "} < ${" + maxReport + "} ? ${" + s + "} > ${" + minReport + "} ? 1 : 0 : 0)";
                }
                linei++;
            }
        }
        erDef += ")";
        er.setDefinition(erDef);
        ReportMonitor rm = er.createMonitor();
        _monitors.put(er.getPresentationName(), rm);
        _tagManager.addTags(er, getTag());
        _tagManager.addTags(rm, getTag());
    }

    private StatisticsReport getMinOscStatisticsReport(Report r) {
        StatisticsReport toReturn;

        for (StatisticsReport sr : _sim.getReportManager().getObjectsOf(StatisticsReport.class)) {
            if (sr.getPresentationName().equals(r.getPresentationName() + " " + _oscillating_tag + " " + _min_tag)) {
                return sr;
            }
        }

        ReportMonitor rm = getReportMonitor(r);
        toReturn = _sim.getReportManager().createReport(StatisticsReport.class);
        toReturn.setMonitor(rm);
        toReturn.setPresentationName(r.getPresentationName() + " " + _oscillating_tag + " " + _min_tag);
        toReturn.setStatisticOption(StatisticOption.Minimum);
        toReturn.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter minNSamplesFilter = (LastNSamplesFilter) toReturn.getSampleFilterManager().getObject("Last N Samples");
        minNSamplesFilter.setNSamples(_moving_avgs[_moving_avgs.length - 1]);
        ReportMonitor minMonitor = toReturn.createMonitor();
        _tagManager.addTags(toReturn, getTag());
        _tagManager.addTags(minMonitor, getTag());
//        _monitors.put(toReturn.getPresentationName(), minMonitor);
        return toReturn;
    }

    private StatisticsReport getMaxOscStatisticsReport(Report r) {
        StatisticsReport toReturn;

        for (StatisticsReport sr : _sim.getReportManager().getObjectsOf(StatisticsReport.class)) {
            if (sr.getPresentationName().equals(r.getPresentationName() + " " + _oscillating_tag + " " + _max_tag)) {
                return sr;
            }
        }

        ReportMonitor rm = getReportMonitor(r);
        toReturn = _sim.getReportManager().createReport(StatisticsReport.class);
        toReturn.setMonitor(rm);
        toReturn.setPresentationName(r.getPresentationName() + " " + _oscillating_tag + " " + _max_tag);
        toReturn.setStatisticOption(StatisticOption.Maximum);
        toReturn.setSampleFilterOption(SampleFilterOption.LastNSamples);
        LastNSamplesFilter minNSamplesFilter = (LastNSamplesFilter) toReturn.getSampleFilterManager().getObject("Last N Samples");
        minNSamplesFilter.setNSamples(_moving_avgs[_moving_avgs.length - 1]);
        ReportMonitor minMonitor = toReturn.createMonitor();
        _tagManager.addTags(toReturn, getTag());
        _tagManager.addTags(minMonitor, getTag());
//        _monitors.put(toReturn.getPresentationName(), minMonitor);
        return toReturn;
    }

    private void createOscMonitorPlot(Report r) {
        String root = r.getPresentationName();

        Iterator<Entry<String, ReportMonitor>> iterator = _monitors.entrySet().iterator();
        Collection<Monitor> plotMonitors = new ArrayList<>();
        while (iterator.hasNext()) {
            Entry<String, ReportMonitor> entry = iterator.next();
            String keyi = entry.getKey();
            if (keyi.contains(r.getPresentationName())) {
                if (!keyi.contains(_asymptotic_tag)) {
                    plotMonitors.add(entry.getValue());
                }
            } else if (entry.getKey().contains(_oscillating_tag + " " + _convergence_tag)) {
                plotMonitors.add(entry.getValue());
            }
        }
        plotMonitors.add(getReportMonitor(r));

        MonitorPlot mp = _sim.getPlotManager().createMonitorPlot(plotMonitors, root + " Osc Convergence");
        _tagManager.addTags(mp, getTag());

        Cartesian2DAxisManager axisManager = ((Cartesian2DAxisManager) mp.getAxisManager());
        Cartesian2DAxis leftAxis = ((Cartesian2DAxis) axisManager.getAxis("Left Axis"));
        AxisTitle leftAxisTitle = leftAxis.getTitle();
        leftAxisTitle.setText(r.getPresentationName());
        Cartesian2DAxis rightAxis = (Cartesian2DAxis) axisManager.createAxis(Cartesian2DAxis.Position.Right);
        AxisTitle rightAxisTitle = rightAxis.getTitle();
        rightAxisTitle.setText("Number Moving Avg Convergence");
        rightAxis.getLabels().setGridVisible(false);

        int maxConv = 2 * _reportNames.length * _moving_avgs.length;
        axisManager.setAxesBounds(new Vector(Arrays.<AxisManager.AxisBounds>asList(new AxisManager.AxisBounds("Left Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Bottom Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Right Axis", 0.0, true, maxConv, true))));

        HashMap<String, MonitorDataSet> dataSets = new HashMap<>();

        final String MAXX = "max";
        final String MINN = "min";
        final String MAIN = "main";
        final String MEAN = "mean";
        final String CONV = "conv";
        ArrayList<Integer> orderedMAKeys = new ArrayList<>();

        for (DataSet mds : mp.getDataSetManager().getObjects()) {
            String monitorName = mds.getDataSourceName();
            if (monitorName.contains(_oscillating_tag + " " + _max_tag)) {
                dataSets.put(MAXX, (MonitorDataSet) mds);
            } else if (monitorName.contains(_oscillating_tag + " " + _min_tag)) {
                dataSets.put(MINN, (MonitorDataSet) mds);
            } else if (monitorName.contains(_oscillating_tag + " " + _convergence_tag)) {
                dataSets.put(CONV, (MonitorDataSet) mds);
            } else if (monitorName.contains("MA")) {
                String s = monitorName.replace("MA", "").replace("Monitor", "").replace(root, "").replace(" ", "");
                int n = Integer.parseInt(s);
                orderedMAKeys.add(n);
                dataSets.put(Integer.toString(n) + " " + MEAN, (MonitorDataSet) mds);
            } else {
                dataSets.put(MAIN, (MonitorDataSet) mds);
            }
        }

        ArrayList<MonitorDataSet> dataSeries = new ArrayList<>();
        dataSeries.add(dataSets.get(MAIN));
        Collections.sort(orderedMAKeys);
        for (int i = 0; i < orderedMAKeys.size(); i++) {
            dataSeries.add(dataSets.get(orderedMAKeys.get(i) + " " + MEAN));
        }
        dataSeries.add(dataSets.get(MAXX));
        dataSeries.add(dataSets.get(MINN));
        dataSeries.add(dataSets.get(CONV));

        mp.setDataSeriesOrder(new NeoObjectVector(dataSeries.toArray()));
        MonitorDataSet mds = dataSets.get(MAXX);
        mds.setSeriesNameLocked(true);
        mds.setSeriesName(root + " " + _oscillating_tag + " " + _max_tag);
        for (int i : orderedMAKeys) {
            mds = dataSets.get(i + " " + MEAN);
            mds.setSeriesNameLocked(true);
            mds.setSeriesName(root + " " + i + " MA");
        }
        mds = dataSets.get(MINN);
        mds.setSeriesNameLocked(true);
        mds.setSeriesName(root + " " + _oscillating_tag + " " + _min_tag);
        mds = dataSets.get(MAIN);
        mds.setSeriesNameLocked(true);
        mds.setSeriesName(root);
        mds = dataSets.get(CONV);
        mds.setSeriesNameLocked(true);
        mds.setSeriesName(root + " " + _oscillating_tag + " " + _convergence_tag);

        dataSets.get(CONV).setYAxis(rightAxis);
        MultiColLegend multiColLegend_0 = mp.getLegend();
        multiColLegend_0.setPositionInfo(0.8894736766815186, 0.7751479148864746, ChartPositionOption.Type.NORTH_EAST);

        LineStyle lineStyle = dataSets.get(MAXX).getLineStyle();
        lineStyle.setColor(new DoubleVector(new double[]{0.6980000138282776, 0.0, 0.0}));
        lineStyle.setLineWidth(1.0);
        lineStyle.getLinePatternOption().setSelected(LinePatternOption.Type.DASH);
        for (int i : orderedMAKeys) {
            lineStyle = dataSets.get(i + " " + MEAN).getLineStyle();
            lineStyle.setColor(new DoubleVector(new double[]{0.5, 0.5, 0.5}));
        }
        lineStyle = dataSets.get(MINN).getLineStyle();
        lineStyle.setColor(new DoubleVector(new double[]{0.07000000029802322, 0.03999999910593033, 0.5600000023841858}));
        lineStyle.setLineWidth(1.0);
        lineStyle.getLinePatternOption().setSelected(LinePatternOption.Type.DASH);
        lineStyle = dataSets.get(MAIN).getLineStyle();
        lineStyle.setColor(new DoubleVector(new double[]{0.0, 0.0, 0.0}));
        lineStyle.setLineWidth(1.0);
        lineStyle = dataSets.get(CONV).getLineStyle();
        lineStyle.setColor(new DoubleVector(new double[]{0.2199999988079071, 0.3700000047683716, 0.05999999865889549}));
        lineStyle.setLineWidth(2.0);
//        lineStyle_0 = convergedMonitor.getLineStyle();
//        lineStyle_0.setColor(new DoubleVector(new double[]{0.8980392217636108, 0.7529411911964417, 0.2980392277240753}));
//        lineStyle_0.setLineWidth(2.0);
    }

    private void createOscStoppingCriteria() {
        int converged_max = _reportNames.length * _moving_avgs.length;
        ReportMonitor rm = (ReportMonitor) _sim.getMonitorManager().getMonitor(_oscillating_tag + " " + _convergence_tag + " Monitor");
        SteadySolver steadySolver = (SteadySolver) _sim.getSolverManager().getSolver(SteadySolver.class);
        MonitorIterationStoppingCriterion maxStoppingCriterion = steadySolver.getSolverStoppingCriterionManager().createIterationStoppingCriterion(rm);
        maxStoppingCriterion.setPresentationName("Osc Convgergence");
        ((MonitorIterationStoppingCriterionOption) maxStoppingCriterion.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.Type.MAXIMUM);
        maxStoppingCriterion.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.OR);
        MonitorIterationStoppingCriterionMaxLimitType maxLimitType = (MonitorIterationStoppingCriterionMaxLimitType) maxStoppingCriterion.getCriterionType();
        maxLimitType.getLimit().setValue(converged_max - 0.5);
        _tagManager.addTags(maxStoppingCriterion, getTag());
    }
}
