
import java.util.Vector;
import star.base.report.ExpressionReport;
import star.base.report.Report;
import star.common.Dimensions;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class MakeExpressionReports extends StarMacro {

    Vector<ExpressionReport> _expReports;
    Simulation _sim;

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        _expReports = _sim.getReportManager().getObjectsOf(ExpressionReport.class);

        for (ScalarGlobalParameter sgp : _sim.get(GlobalParameterManager.class).getObjectsOf(ScalarGlobalParameter.class)) {
            if (sgp.getPresentationName().startsWith("Q_")) {
                if (!hasReport(sgp)) {
                    ExpressionReport er = _sim.getReportManager().createReport(ExpressionReport.class);
                    Dimensions dim = sgp.getDimensions();
                    er.setDimensions(dim);
                    er.setUnits(sgp.getQuantity().getUnits());
                    er.setDefinition(getDefinitionForExpression(sgp));
                    er.setPresentationName(sgp.getPresentationName() + "_Report");
                }
            }
        }
    }

    private boolean hasReport(ScalarGlobalParameter param) {
        boolean response = false;
        for (ExpressionReport exr : _expReports) {
            if (exr.getDefinition().equals(getDefinitionForExpression(param))) {
                response = true;
            }
        }
        return response;
    }

    private String getDefinitionForExpression(ScalarGlobalParameter param) {
        return "${" + param.getPresentationName() + "}";
    }

}
