package industrialTestCases;


import star.common.Simulation;
import star.common.StarMacro;
import star.starcad2.RemoteCcmpProxySettings;
import star.starcad2.RemoteStarCadSettings;
import star.starcad2.StarCadDocument;
import star.starcad2.StarCadDocumentManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cd8unu
 */
public class UpdateCADClientSettings_bkup extends StarMacro {

    Simulation _sim;
    String p_docName = "Nacelle2.CATAnalysis";
    String p_cadHost = "10.104.121.60";
    String p_cadInstall = "C:\\Siemens\\16.01.040-R8\\STAR-CAD16.01.040";
    String p_proxyHost = "wvhpc02v01";
    String p_proxyNetwork = ".net.plm.eds.com";
    String p_sshCommand = "ssh -A -t " + p_proxyHost + " ssh -A -l cd8unu";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        StarCadDocument starCADDoc = _sim.get(StarCadDocumentManager.class).getDocument(p_docName);
        RemoteStarCadSettings remoteSettings = starCADDoc .getRemoteStarCadSettings();
        remoteSettings.setCadClientHost(p_cadHost);
        remoteSettings.setCadInstallDir(p_cadInstall);
        remoteSettings.setSshCmd(p_sshCommand);
        RemoteCcmpProxySettings remoteProxySettings = starCADDoc .getRemoteCcmpProxySettings();
        remoteProxySettings.setProxyHost(p_proxyHost);
        remoteProxySettings.setProxyPublicHost(p_proxyHost + p_proxyNetwork);
        
        _sim.println(remoteSettings.getCadClientHost());
        _sim.println(remoteSettings.getCadInstallDir());
        _sim.println(remoteSettings.getSshCmd());
        _sim.println(remoteProxySettings.getProxyHost());
        _sim.println(remoteProxySettings.getProxyPublicHost());
        
        starCADDoc.updateModel();
        
        _sim.saveState(_sim.getSessionDir() + "/" + _sim.getPresentationName() + ".sim");
    }
}
