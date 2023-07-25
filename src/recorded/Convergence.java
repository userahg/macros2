// Simcenter STAR-CCM+ macro: Convergence.java
// Written by Simcenter STAR-CCM+ 16.06.007
package recorded;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.base.report.*;
import star.vis.*;

public class Convergence extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    simulation_0.get(GlobalParameterManager.class).createGlobalParameter(ScalarGlobalParameter.class, "Scalar");

    ScalarGlobalParameter scalarGlobalParameter_0 = 
      ((ScalarGlobalParameter) simulation_0.get(GlobalParameterManager.class).getObject("Scalar"));

    scalarGlobalParameter_0.setPresentationName("conv_crit");

    scalarGlobalParameter_0.getQuantity().setValue(1.0E-4);

    Units units_0 = 
      ((Units) simulation_0.getUnitsManager().getObject(""));

    scalarGlobalParameter_0.getQuantity().setUnits(units_0);

    StatisticsReport statisticsReport_0 = 
      simulation_0.getReportManager().createReport(StatisticsReport.class);

    statisticsReport_0.setPresentationName("Cd Max");

    statisticsReport_0.setStatisticOption(StatisticOption.Maximum);

    ReportMonitor reportMonitor_0 = 
      ((ReportMonitor) simulation_0.getMonitorManager().getMonitor("Cd Monitor"));

    statisticsReport_0.setMonitor(reportMonitor_0);

    StatisticsReport statisticsReport_1 = 
      simulation_0.getReportManager().createReport(StatisticsReport.class);

    statisticsReport_1.copyProperties(statisticsReport_0);

    statisticsReport_1.setPresentationName("Copy of Cd Max");

    statisticsReport_1.setPresentationName("Cd Mean");

    statisticsReport_1.setStatisticOption(StatisticOption.Mean);

    StatisticsReport statisticsReport_2 = 
      simulation_0.getReportManager().createReport(StatisticsReport.class);

    statisticsReport_2.copyProperties(statisticsReport_1);

    statisticsReport_2.setPresentationName("Copy of Cd Mean");

    statisticsReport_2.setPresentationName("Cd Min");

    statisticsReport_2.setStatisticOption(StatisticOption.Minimum);

    ReportMonitor reportMonitor_1 = 
      statisticsReport_2.createMonitor();

    ReportMonitor reportMonitor_2 = 
      statisticsReport_1.createMonitor();

    ReportMonitor reportMonitor_3 = 
      statisticsReport_0.createMonitor();

    ExpressionReport expressionReport_0 = 
      simulation_0.getReportManager().createReport(ExpressionReport.class);

    expressionReport_0.setPresentationName("Cd Conv");

    expressionReport_0.setDefinition("${Iteration} < 10 ? 100 : (alternateValue(${Cd Max}, 100) - alternateValue(${Cd Min}, 0))/alternateValue(${Cd Mean}, 1)/${conv_crit}");

    ReportMonitor reportMonitor_4 = 
      expressionReport_0.createMonitor();

    ExpressionReport expressionReport_1 = 
      simulation_0.getReportManager().createReport(ExpressionReport.class);

    expressionReport_1.setPresentationName("Converged");

    expressionReport_1.setDefinition("1");

    ReportMonitor reportMonitor_5 = 
      expressionReport_1.createMonitor();

    MonitorPlot monitorPlot_3 = 
      simulation_0.getPlotManager().createMonitorPlot(new NeoObjectVector(new Object[] {reportMonitor_4, reportMonitor_3, reportMonitor_2, reportMonitor_1, reportMonitor_0, reportMonitor_5}), "Monitors Plot");

    monitorPlot_3.open();

    PlotUpdate plotUpdate_5 = 
      monitorPlot_3.getPlotUpdate();

    HardcopyProperties hardcopyProperties_6 = 
      plotUpdate_5.getHardcopyProperties();

    hardcopyProperties_6.setCurrentResolutionWidth(25);

    hardcopyProperties_6.setCurrentResolutionHeight(25);

    ResidualPlot residualPlot_0 = 
      ((ResidualPlot) simulation_0.getPlotManager().getPlot("Residuals"));

    PlotUpdate plotUpdate_0 = 
      residualPlot_0.getPlotUpdate();

    HardcopyProperties hardcopyProperties_1 = 
      plotUpdate_0.getHardcopyProperties();

    hardcopyProperties_1.setOutputWidth(1280);

    hardcopyProperties_1.setOutputWidth(1280);

    hardcopyProperties_1.setOutputHeight(960);

    hardcopyProperties_1.setOutputHeight(960);

    hardcopyProperties_1.setOutputWidth(1280);

    hardcopyProperties_1.setOutputHeight(960);

    hardcopyProperties_6.setCurrentResolutionWidth(1330);

    hardcopyProperties_6.setCurrentResolutionHeight(676);

    Cartesian2DAxisManager cartesian2DAxisManager_0 = 
      ((Cartesian2DAxisManager) monitorPlot_3.getAxisManager());

    Cartesian2DAxis cartesian2DAxis_0 = 
      ((Cartesian2DAxis) cartesian2DAxisManager_0.getAxis("Left Axis"));

    AxisTitle axisTitle_0 = 
      cartesian2DAxis_0.getTitle();

    axisTitle_0.setText("Cd");

    Cartesian2DAxis cartesian2DAxis_1 = 
      (Cartesian2DAxis) cartesian2DAxisManager_0.createAxis(Cartesian2DAxis.Position.Right);

    AxisTitle axisTitle_1 = 
      cartesian2DAxis_1.getTitle();

    axisTitle_1.setText("Asymptote");

    cartesian2DAxis_1.setLogarithmic(true);

    MonitorDataSet monitorDataSet_0 = 
      ((MonitorDataSet) monitorPlot_3.getDataSetManager().getDataSet("Cd Conv Monitor"));

    monitorDataSet_0.setYAxis(cartesian2DAxis_1);

    MonitorDataSet monitorDataSet_1 = 
      ((MonitorDataSet) monitorPlot_3.getDataSetManager().getDataSet("Converged Monitor"));

    monitorDataSet_1.setYAxis(cartesian2DAxis_1);

    MultiColLegend multiColLegend_0 = 
      monitorPlot_3.getLegend();

    multiColLegend_0.setPositionInfo(0.8894736766815186, 0.7751479148864746, ChartPositionOption.Type.NORTH_EAST);

    cartesian2DAxisManager_0.setAxesBounds(new Vector(Arrays.<AxisManager.AxisBounds>asList(new AxisManager.AxisBounds("Left Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Bottom Axis", 0.0, false, 1.0, false), new AxisManager.AxisBounds("Right Axis", 1.0, false, 100.0, false))));

    MonitorDataSet monitorDataSet_2 = 
      ((MonitorDataSet) monitorPlot_3.getDataSetManager().getDataSet("Cd Max Monitor"));

    MonitorDataSet monitorDataSet_3 = 
      ((MonitorDataSet) monitorPlot_3.getDataSetManager().getDataSet("Cd Mean Monitor"));

    MonitorDataSet monitorDataSet_4 = 
      ((MonitorDataSet) monitorPlot_3.getDataSetManager().getDataSet("Cd Min Monitor"));

    MonitorDataSet monitorDataSet_5 = 
      ((MonitorDataSet) monitorPlot_3.getDataSetManager().getDataSet("Cd Monitor"));

    monitorPlot_3.setDataSeriesOrder(new NeoObjectVector(new Object[] {monitorDataSet_2, monitorDataSet_3, monitorDataSet_4, monitorDataSet_5, monitorDataSet_0, monitorDataSet_1}));

    monitorDataSet_2.setSeriesNameLocked(true);

    monitorDataSet_2.setSeriesName("Cd Max");

    monitorDataSet_3.setSeriesNameLocked(true);

    monitorDataSet_3.setSeriesName("Cd Mean");

    monitorDataSet_4.setSeriesNameLocked(true);

    monitorDataSet_4.setSeriesName("Cd Min");

    monitorDataSet_5.setSeriesNameLocked(true);

    monitorDataSet_5.setSeriesName("Cd");

    monitorDataSet_0.setSeriesNameLocked(true);

    monitorDataSet_0.setSeriesName("Cd Conv");

    monitorDataSet_1.setSeriesNameLocked(true);

    monitorDataSet_1.setSeriesName("Converged");

    SteadySolver steadySolver_0 = 
      ((SteadySolver) simulation_0.getSolverManager().getSolver(SteadySolver.class));

    MonitorIterationStoppingCriterion monitorIterationStoppingCriterion_0 = 
      steadySolver_0.getSolverStoppingCriterionManager().createIterationStoppingCriterion(reportMonitor_0);

    ((MonitorIterationStoppingCriterionOption) monitorIterationStoppingCriterion_0.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.Type.ASYMPTOTIC);

    monitorIterationStoppingCriterion_0.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.AND);

    MonitorIterationStoppingCriterionAsymptoticType monitorIterationStoppingCriterionAsymptoticType_0 = 
      ((MonitorIterationStoppingCriterionAsymptoticType) monitorIterationStoppingCriterion_0.getCriterionType());

    monitorIterationStoppingCriterionAsymptoticType_0.setNumberSamples(100);

    monitorIterationStoppingCriterionAsymptoticType_0.getMaxWidth().setDefinition("${conv_crit}");

    monitorIterationStoppingCriterionAsymptoticType_0.getMaxWidth().setUnits(units_0);

    statisticsReport_0.setSampleFilterOption(SampleFilterOption.LastNSamples);

    statisticsReport_1.setSampleFilterOption(SampleFilterOption.LastNSamples);

    statisticsReport_2.setSampleFilterOption(SampleFilterOption.LastNSamples);

    LastNSamplesFilter lastNSamplesFilter_0 = 
      ((LastNSamplesFilter) statisticsReport_0.getSampleFilterManager().getObject("Last N Samples"));

    lastNSamplesFilter_0.setNSamples(100);

    LastNSamplesFilter lastNSamplesFilter_1 = 
      ((LastNSamplesFilter) statisticsReport_1.getSampleFilterManager().getObject("Last N Samples"));

    lastNSamplesFilter_1.setNSamples(100);

    LastNSamplesFilter lastNSamplesFilter_2 = 
      ((LastNSamplesFilter) statisticsReport_2.getSampleFilterManager().getObject("Last N Samples"));

    lastNSamplesFilter_2.setNSamples(100);

    LineStyle lineStyle_0 = 
      monitorDataSet_0.getLineStyle();

    lineStyle_0.setColor(new DoubleVector(new double[] {0.23000000417232513, 0.3700000047683716, 0.17000000178813934}));

    lineStyle_0.setColor(new DoubleVector(new double[] {0.2199999988079071, 0.3700000047683716, 0.05999999865889549}));

    LineStyle lineStyle_1 = 
      monitorDataSet_2.getLineStyle();

    lineStyle_1.setColor(new DoubleVector(new double[] {0.8899999856948853, 0.07000000029802322, 0.1899999976158142}));

    lineStyle_1.setColor(new DoubleVector(new double[] {0.8299999833106995, 0.10000000149011612, 0.11999999731779099}));

    LineStyle lineStyle_2 = 
      monitorDataSet_3.getLineStyle();

    lineStyle_2.setColor(new DoubleVector(new double[] {0.5, 0.5, 0.4099999964237213}));

    LineStyle lineStyle_3 = 
      monitorDataSet_4.getLineStyle();

    lineStyle_3.setColor(new DoubleVector(new double[] {0.2824000120162964, 0.23919999599456787, 0.5450999736785889}));

    LineStyle lineStyle_4 = 
      monitorDataSet_5.getLineStyle();

    lineStyle_4.setColor(new DoubleVector(new double[] {0.0, 0.0, 0.0}));

    LineStyle lineStyle_5 = 
      monitorDataSet_1.getLineStyle();

    lineStyle_5.setColor(new DoubleVector(new double[] {0.8980392217636108, 0.7529411911964417, 0.2980392277240753}));

    lineStyle_0.setLineWidth(2.0);

    lineStyle_1.setLineWidth(2.0);

    lineStyle_2.setLineWidth(2.0);

    lineStyle_3.setLineWidth(2.0);

    lineStyle_4.setLineWidth(2.0);

    lineStyle_5.setLineWidth(2.0);
  }
}
