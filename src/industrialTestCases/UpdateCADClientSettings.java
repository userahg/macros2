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
public class UpdateCADClientSettings extends StarMacro {

    Simulation _sim;
    String p_docName = "staticMixer.CATAnalysis";
    String p_docLocation = "E:\\OneDrive - Siemens AG\\mdx\\prep\\ITC\\2023.2";
    String p_cadHost = "10.104.156.3";
    String p_cadInstall = "E:\\Siemens\\STARCCM\\18.04.008-R8\\STAR-CAD18.04.008";
    String p_proxyHost = "wvhpc02v01";
    String p_proxyNetwork = ".net.plm.eds.com";
    String p_sshCommand = "ssh -A -t " + p_proxyHost + " ssh -A -l cd8unu";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        StarCadDocument starCADDoc = _sim.get(StarCadDocumentManager.class).getDocument(p_docName);
        starCADDoc.setCadFileDir(p_docLocation);
        RemoteStarCadSettings remoteSettings = starCADDoc.getRemoteStarCadSettings();
        remoteSettings.setCadClientHost(p_cadHost);
        remoteSettings.setCadInstallDir(p_cadInstall);
        remoteSettings.setSshCmd(p_sshCommand);
        RemoteCcmpProxySettings remoteProxySettings = starCADDoc.getRemoteCcmpProxySettings();
        remoteProxySettings.setProxyHost(p_proxyHost);
        remoteProxySettings.setProxyPublicHost(p_proxyHost + p_proxyNetwork);
        
        _sim.println(remoteSettings.getCadClientHost());
        _sim.println(remoteSettings.getCadInstallDir());
        _sim.println(remoteSettings.getSshCmd());
        _sim.println(remoteProxySettings.getProxyHost());
        _sim.println(remoteProxySettings.getProxyPublicHost());

        _sim.saveState(_sim.getSessionDir() + "/" + _sim.getPresentationName() + ".sim");
    }
}
