
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
public class UpdateRemoteCADClientSettings extends StarMacro {

    Simulation _sim;
    String docName = "Schablone_x_t5.prt";
    String cadHost = "10.104.121.60";
    String cadInstall = "C:\\Siemens\\16.01.040-R8\\STAR-CAD16.01.040";
    //String proxyDomainName = "seahawkvis01";
    String proxyDomainName = "wvhpc02v01";
    //String proxyPublicNetwork = ".sunnyvale.cd-adapco.com";
    String proxyPublicNetwork = ".net.plm.eds.com";
    String sshCommand = "ssh -A -t " + proxyDomainName + " ssh -A -l cd8unu";

    @Override
    public void execute() {
        _sim = getActiveSimulation();
        StarCadDocument starCADDoc = _sim.get(StarCadDocumentManager.class).getDocument(docName);
        RemoteStarCadSettings remoteSettings = starCADDoc .getRemoteStarCadSettings();
        remoteSettings.setCadClientHost(cadHost);
        remoteSettings.setCadInstallDir(cadInstall);
        remoteSettings.setSshCmd(sshCommand);
        RemoteCcmpProxySettings remoteProxySettings = starCADDoc .getRemoteCcmpProxySettings();
        remoteProxySettings.setProxyHost(proxyDomainName);
        remoteProxySettings.setProxyPublicHost(proxyDomainName + proxyPublicNetwork);
        
        _sim.println(remoteSettings.getCadClientHost());
        _sim.println(remoteSettings.getCadInstallDir());
        _sim.println(remoteSettings.getSshCmd());
        _sim.println(remoteProxySettings.getProxyHost());
        _sim.println(remoteProxySettings.getProxyPublicHost());
        
        starCADDoc.updateModel();
        
        _sim.saveState(_sim.getSessionDir() + "/" + _sim.getPresentationName() + ".sim");
    }
}