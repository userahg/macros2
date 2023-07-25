// Simcenter STAR-CCM+ macro: MakeMappedContactInterfaces.java
// Written by Simcenter STAR-CCM+ 16.04.007
package eCoolFESS;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import star.base.neo.NeoObjectVector;

import star.common.*;
import star.meshing.InPlacePartSurfaceContact;

public class MakeMappedContactInterfaces extends StarMacro {

    Simulation _sim;
    boolean _isTest = false;
    boolean _deleteInterfaces = true;
    Mode _mode = Mode.BOTH;

    @Override
    public void execute() {
        _sim = getActiveSimulation();

        try {
            deleteInterfaces();

            MappedInterface mappedInterface = (MappedInterface) _sim.get(ConditionTypeManager.class).get(MappedInterface.class);
            Collection<PartContact> partContacts = _sim.get(PartContactManager.class).getObjectsOf(PartContact.class);

            for (InterfaceBoundaryDefinition bndDefinitioni : populateInterfaceDefinitions()) {
                if (bndDefinitioni.debug()) {
                    _sim.println("Debugging " + bndDefinitioni.getName());
                }

                ArrayList<InPlacePartSurfaceContact> makeInterface = new ArrayList<>();

                for (PartContact pci : partContacts) {
                    Collection<Object> objs = pci.getChildren();
                    InPlacePartSurfaceContact[] partSurfaceContacts = objs.toArray(new InPlacePartSurfaceContact[objs.size()]);

                    for (InPlacePartSurfaceContact contact : partSurfaceContacts) {
                        GeometryPart gp0 = contact.getPartSurface0().getPart();
                        GeometryPart gp1 = contact.getPartSurface1().getPart();

                        if (bndDefinitioni.include(gp0.getPresentationName(), gp1.getPresentationName())) {
                            makeInterface.add(contact);
                        }
                    }
                }

                if (makeInterface.size() > 0) {

                    if (_isTest) {
                        _sim.println(bndDefinitioni.getName() + " found the following contact pairings:");
                        for (InPlacePartSurfaceContact contacti : makeInterface) {
                            _sim.println("\t" + contacti.getPresentationName());
                        }
                    }

                    switch (bndDefinitioni.getName()) {
                        default:
                            BoundaryInterface bndInterface = _sim.getInterfaceManager().createBoundaryInterface(new NeoObjectVector(makeInterface.toArray()), bndDefinitioni.getName());
                            if (bndDefinitioni.isMappedInterface()) {
                                bndInterface.setInterfaceType(mappedInterface);
                            }
                    }

                } else {
                    _sim.println("Interface Boundary Definition " + bndDefinitioni.getName() + " found no matching contacts for boundary pairings:");
                    for (String[] predicates : bndDefinitioni.getBoundaryPairings()) {
                        _sim.println("\t" + predicates[0] + " and " + predicates[1]);
                    }
                }
            }
        } catch (Exception ex) {
            print(ex);
        }
    }

    private void deleteInterfaces() {
        if (!_deleteInterfaces) {
            return;
        }
        _sim.getInterfaceManager().deleteChildren(_sim.getInterfaceManager().getObjects());

    }

    private Interface interfaceExists(String name) {
        for (Interface i : _sim.getInterfaceManager().getObjects()) {
            if (i.getPresentationName().matches(name + ".*")) {
                return i;
            }
        }
        return null;
    }

    private ArrayList<InterfaceBoundaryDefinition> populateInterfaceDefinitions() {
        ArrayList<InterfaceBoundaryDefinition> bndDefinitions = new ArrayList<>();

        switch (_mode) {
            case FLUID:
                bndDefinitions.addAll(populateCFDInterfaceDefinitions(0));
                break;
            case SOLID:
                bndDefinitions.addAll(populateSolidInterfaceDefinitions(0));
                break;
            case BOTH:
                bndDefinitions.addAll(populateSolidInterfaceDefinitions(0));
                bndDefinitions.addAll(populateCFDInterfaceDefinitions(bndDefinitions.size()));
                break;
        }
        return bndDefinitions;
    }

    private ArrayList<InterfaceBoundaryDefinition> populateSolidInterfaceDefinitions(int bnd_counter) {
        ArrayList<InterfaceBoundaryDefinition> names = new ArrayList<>();

        bnd_counter++;
        String bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition casing2LowerPlate = new InterfaceBoundaryDefinition(bnd_prefix + "_Casing/LowerPlate", true);
        casing2LowerPlate.addBoundaryPairing("Casing.*", "LOWER PLATE");
        names.add(casing2LowerPlate);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition sub12lowerPlate = new InterfaceBoundaryDefinition(bnd_prefix + "_Sub1/LowerPlate", true);
        sub12lowerPlate.addBoundaryPairing("Substrate layer 1 - solder.*", "LOWER PLATE");
        names.add(sub12lowerPlate);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition sub2sub1 = new InterfaceBoundaryDefinition(bnd_prefix + "_Sub2/Sub1", true);
        sub2sub1.addBoundaryPairing("Substrate layer 2 - copper.*", "Substrate layer 1 - solder.*");
        names.add(sub2sub1);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition sub32sub2 = new InterfaceBoundaryDefinition(bnd_prefix + "_Sub3/Sub2", true);
        sub32sub2.addBoundaryPairing("Substrate layer 3 - ceramic.*", "Substrate layer 2 - copper.*");
        names.add(sub32sub2);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition trace2Sub3 = new InterfaceBoundaryDefinition(bnd_prefix + "_Trace/Sub3 1", true);
        trace2Sub3.addBoundaryPairing("Trace 1", "Substrate layer 3 - ceramic");
        trace2Sub3.addBoundaryPairing("Trace 2", "Substrate layer 3 - ceramic");
        trace2Sub3.addBoundaryPairing("Trace 3", "Substrate layer 3 - ceramic");
        trace2Sub3.addBoundaryPairing("Trace 5", "Substrate layer 3 - ceramic");
        names.add(trace2Sub3);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition trace2Sub3b = new InterfaceBoundaryDefinition(bnd_prefix + "_Trace/Sub3 2", true);
        trace2Sub3b.addBoundaryPairing("Trace 4", "Substrate layer 3 - ceramic");
        trace2Sub3b.addBoundaryPairing("Trace 6", "Substrate layer 3 - ceramic");
        trace2Sub3b.addBoundaryPairing("Trace 7", "Substrate layer 3 - ceramic");
        trace2Sub3b.addBoundaryPairing("Trace 8", "Substrate layer 3 - ceramic");
        names.add(trace2Sub3b);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition casing2SubsTraces = new InterfaceBoundaryDefinition(bnd_prefix + "_Casing/Substrates", true);
        casing2SubsTraces.addBoundaryPairing("Substrate layer 1 - solder", "Casing");
        casing2SubsTraces.addBoundaryPairing("Substrate layer 2 - copper", "Casing");
        casing2SubsTraces.addBoundaryPairing("Substrate layer 3 - ceramic", "Casing");
        names.add(casing2SubsTraces);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition casing2Subs1 = new InterfaceBoundaryDefinition(bnd_prefix + "_Casing/Traces 1", true);
        casing2Subs1.addBoundaryPairing("Trace 1", "Casing");
        casing2Subs1.addBoundaryPairing("Trace 2", "Casing");
        casing2Subs1.addBoundaryPairing("Trace 3", "Casing");
        casing2Subs1.addBoundaryPairing("Trace 5", "Casing");
        names.add(casing2Subs1);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition casing2Subs2 = new InterfaceBoundaryDefinition(bnd_prefix + "_Casing/Traces 2", true);
        casing2Subs2.addBoundaryPairing("Trace 4", "Casing.*");
        casing2Subs2.addBoundaryPairing("Trace 6", "Casing.*");
        casing2Subs2.addBoundaryPairing("Trace 7", "Casing.*");
        casing2Subs2.addBoundaryPairing("Trace 8", "Casing.*");
        names.add(casing2Subs2);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition dieAttach2Trace = new InterfaceBoundaryDefinition(bnd_prefix + "_DieAttach/Trace 1", true);
        dieAttach2Trace.addBoundaryPairing("DieAttach.*", "Trace 1.*");
        dieAttach2Trace.addBoundaryPairing("DieAttach.*", "Trace 2.*");
        dieAttach2Trace.addBoundaryPairing("DieAttach.*", "Trace 3.*");
        dieAttach2Trace.addBoundaryPairing("DieAttach.*", "Trace 5.*");
        names.add(dieAttach2Trace);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition diode2diodeAttach = new InterfaceBoundaryDefinition(bnd_prefix + "_Diode/DieAttach", true);
        diode2diodeAttach.addBoundaryPairing("Die_Diode.*", "DieAttach_Diode.*");
        names.add(diode2diodeAttach);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition igbtDie2DieAttach = new InterfaceBoundaryDefinition(bnd_prefix + "_IGBTDie/DieAttach", true);
        igbtDie2DieAttach.addBoundaryPairing("Die_IGBT.*", "DieAttach_IGBT.*");
        names.add(igbtDie2DieAttach);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition igbt2Die = new InterfaceBoundaryDefinition(bnd_prefix + "_ActiveIGBT/IGBTDie", true);
        igbt2Die.addBoundaryPairing("Active_IGBT.*", "Die_IGBT.*");
        igbt2Die.addExcludePairing("Active_IGBT2-2", "Die_IGBT2-2");
        names.add(igbt2Die);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition singleIGBT2Die = new InterfaceBoundaryDefinition(bnd_prefix + "_SingleActiveIGBT/SingleActiveIBGTDie", true);
        singleIGBT2Die.addBoundaryPairing("Active_IGBT2-2", "Die_IGBT2-2");
        names.add(singleIGBT2Die);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition pin2Trace = new InterfaceBoundaryDefinition(bnd_prefix + "_Pin/Trace 1", true);
        pin2Trace.addBoundaryPairing("Pin -.*", "Trace 1.*");
        pin2Trace.addBoundaryPairing("Pin -.*", "Trace 2.*");
        pin2Trace.addBoundaryPairing("Pin -.*", "Trace 3.*");
        pin2Trace.addBoundaryPairing("Pin -.*", "Trace 5.*");
        names.add(pin2Trace);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition trace2BondWires = new InterfaceBoundaryDefinition(bnd_prefix + "_Trace/BondWires 1", true);
        trace2BondWires.addBoundaryPairing("Trace 1.*", "Body.*");
        trace2BondWires.addBoundaryPairing("Trace 2.*", "Body.*");
        trace2BondWires.addBoundaryPairing("Trace 3.*", "Body.*");
        trace2BondWires.addBoundaryPairing("Trace 5.*", "Body.*");
        names.add(trace2BondWires);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition trace2BondWires2 = new InterfaceBoundaryDefinition(bnd_prefix + "_Trace/BondWires 2", true);
        trace2BondWires2.addBoundaryPairing("Trace 4.*", "Body.*");
        trace2BondWires2.addBoundaryPairing("Trace 6.*", "Body.*");
        trace2BondWires2.addBoundaryPairing("Trace 7.*", "Body.*");
        trace2BondWires2.addBoundaryPairing("Trace 8.*", "Body.*");
        names.add(trace2BondWires2);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition die2BondWires = new InterfaceBoundaryDefinition(bnd_prefix + "_Diode/BondWires", true);
        die2BondWires.addBoundaryPairing("Die_Diode.*", "Body.*");
        names.add(die2BondWires);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition igbt2BondWires = new InterfaceBoundaryDefinition(bnd_prefix + "_ActiveIGBT/BondWires", true);
        igbt2BondWires.addBoundaryPairing("Active_IGBT.*", "Body.*");
        igbt2BondWires.addExcludePairing("Active_IGBT2-2", "Body.*");
        names.add(igbt2BondWires);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition singleIBGT2BondWires = new InterfaceBoundaryDefinition(bnd_prefix + "_SingleActiveIGBT/BondWires", true);
        singleIBGT2BondWires.addBoundaryPairing("Active_IGBT2-2", "Body.*");
        names.add(singleIBGT2BondWires);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2Diode = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/Diode", true);
        siGel2Diode.addBoundaryPairing("Si Gel.*", "Die_Diode.*");
        names.add(siGel2Diode);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2IGBT = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/IGBT", true);
        siGel2IGBT.addBoundaryPairing("Si Gel.*", "Active_IGBT.*");
        siGel2IGBT.addExcludePairing("Si Gel.*", "Active_IGBT2-2");
        names.add(siGel2IGBT);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2SingleIGBT = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/SingleActiveIGBT", true);
        siGel2SingleIGBT.addBoundaryPairing("Si Gel.*", "Active_IGBT2-2");
        names.add(siGel2SingleIGBT);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2BondWires = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/BondWires", true);
        siGel2BondWires.addBoundaryPairing("Si Gel.*", "Body.*");
        names.add(siGel2BondWires);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition casing2Pins = new InterfaceBoundaryDefinition(bnd_prefix + "_Casing/Pins", true);
        casing2Pins.addBoundaryPairing("Casing", "Pin -.*");
        names.add(casing2Pins);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2Solids = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/DieAttach/Substrate/Casing/Pins/TopPlate", true);
        siGel2Solids.addBoundaryPairing("Si Gel", "Die_IGBT.*");
        siGel2Solids.addBoundaryPairing("Si Gel", "DieAttach_IGBT.*");
        siGel2Solids.addBoundaryPairing("Si Gel", "DieAttach_Diode.*");
        siGel2Solids.addBoundaryPairing("Si Gel", "Pin -.*");
        siGel2Solids.addBoundaryPairing("Si Gel", "Casing");
        siGel2Solids.addBoundaryPairing("Si Gel", "UPPER PLATE");
        siGel2Solids.addBoundaryPairing("Si Gel", "Substrate layer 3 - ceramic");
        names.add(siGel2Solids);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2Trace = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/Trace 1", true);
        siGel2Trace.addBoundaryPairing("Si Gel.*", "Trace 1.*");
        siGel2Trace.addBoundaryPairing("Si Gel.*", "Trace 2.*");
        siGel2Trace.addBoundaryPairing("Si Gel.*", "Trace 3.*");
        siGel2Trace.addBoundaryPairing("Si Gel.*", "Trace 5.*");
        names.add(siGel2Trace);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition siGel2Trace2 = new InterfaceBoundaryDefinition(bnd_prefix + "_SiGel/Trace 2", true);
        siGel2Trace2.addBoundaryPairing("Si Gel.*", "Trace 4.*");
        siGel2Trace2.addBoundaryPairing("Si Gel.*", "Trace 6.*");
        siGel2Trace2.addBoundaryPairing("Si Gel.*", "Trace 7.*");
        siGel2Trace2.addBoundaryPairing("Si Gel.*", "Trace 8.*");
        names.add(siGel2Trace2);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition casing2UpperPlate = new InterfaceBoundaryDefinition(bnd_prefix + "_Casing/UpperPlate", true);
        casing2UpperPlate.addBoundaryPairing("Casing", "UPPER PLATE");
        names.add(casing2UpperPlate);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition upperPlate2Pins = new InterfaceBoundaryDefinition(bnd_prefix + "_Pins/UpperPlate", true);
        upperPlate2Pins.addBoundaryPairing("Pin -.*", "UPPER PLATE");
        names.add(upperPlate2Pins);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition lowerPlate2ProxyBolts = new InterfaceBoundaryDefinition(bnd_prefix + "_LowerPlate/ProxyBolts", true);
        lowerPlate2ProxyBolts.addBoundaryPairing("LOWER PLATE", "Proxy_Bolt1");
        lowerPlate2ProxyBolts.addBoundaryPairing("LOWER PLATE", "Proxy_Bolt2");
        lowerPlate2ProxyBolts.addBoundaryPairing("LOWER PLATE", "Proxy_Bolt3");
        lowerPlate2ProxyBolts.addBoundaryPairing("LOWER PLATE", "Proxy_Bolt4");
        names.add(lowerPlate2ProxyBolts);

        return names;
    }

    private ArrayList<InterfaceBoundaryDefinition> populateCFDInterfaceDefinitions(int bnd_counter) {
        ArrayList<InterfaceBoundaryDefinition> names = new ArrayList<>();
        
        bnd_counter++;
        String bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition lowerPlate2ColdPlate = new InterfaceBoundaryDefinition(bnd_prefix + "_LowerPlate/ColdPlate", true);
        lowerPlate2ColdPlate.addBoundaryPairing("COLD_PLATE", "LOWER PLATE");
        names.add(lowerPlate2ColdPlate);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition coldPlate2Fins = new InterfaceBoundaryDefinition(bnd_prefix + "_ColdPlate/Fins", true);
        coldPlate2Fins.addBoundaryPairing("COLD_PLATE", "FIN");
        names.add(coldPlate2Fins);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition coldPlate2Coolant = new InterfaceBoundaryDefinition(bnd_prefix + "_ColdPlate/Coolant", true);
        coldPlate2Coolant.addBoundaryPairing("COLD_PLATE", "COOLANT");
        names.add(coldPlate2Coolant);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition fins2Coolant = new InterfaceBoundaryDefinition(bnd_prefix + "_Fin/Coolant", true);
        fins2Coolant.addBoundaryPairing("FIN", "COOLANT");
        names.add(fins2Coolant);

        bnd_counter++;
        bnd_prefix = String.format("%02d", bnd_counter);
        InterfaceBoundaryDefinition coolant2Extruded = new InterfaceBoundaryDefinition(bnd_prefix + "_Coolant/Extruded", false);
        coolant2Extruded.addBoundaryPairing("COOLANT", "EXTRUDED OUTLET");
        coolant2Extruded.addBoundaryPairing("COOLANT", "EXTRUDED INLET");
        names.add(coolant2Extruded);
        
        return names;
    }

    private enum Mode {
        SOLID, FLUID, BOTH
    }

    private void print(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _sim.println(sw.toString());
    }

    private class InterfaceBoundaryDefinition {

        private final String _name;
        private final ArrayList<String[]> _boundaryPairings;
        private final ArrayList<String[]> _excludePairings;
        private final boolean _isMappedInterface;
        private final boolean _debug;

        InterfaceBoundaryDefinition(String name, boolean isMappedInterface) {
            _name = name;
            _isMappedInterface = isMappedInterface;
            _boundaryPairings = new ArrayList<>();
            _excludePairings = new ArrayList<>();
            _debug = false;
        }

        public InterfaceBoundaryDefinition(String name, boolean isMappedInterface, boolean debug) {
            this._name = name;
            this._boundaryPairings = new ArrayList<>();
            this._excludePairings = new ArrayList<>();
            this._isMappedInterface = isMappedInterface;
            this._debug = debug;
        }

        public void addBoundaryPairing(String bnd0, String bnd1) {
            _boundaryPairings.add(new String[]{bnd0, bnd1});
        }

        public void addExcludePairing(String bnd0, String bnd1) {
            _excludePairings.add(new String[]{bnd0, bnd1});
        }

        public ArrayList<String[]> getBoundaryPairings() {
            return _boundaryPairings;
        }

        public boolean include(String part0, String part1) {
            boolean bnd0match;
            boolean bnd1match;
            for (String[] predicates : getBoundaryPairings()) {
                bnd0match = false;
                bnd1match = false;
                for (String regex : predicates) {
                    if (part0.matches(regex)) {
                        bnd0match = true;
                    }
                    if (part1.matches(regex)) {
                        bnd1match = true;
                    }
                }

                if (bnd0match && bnd1match) {
                    return !exclude(part0, part1);
                }
            }
            return false;
        }

        public boolean exclude(String part0, String part1) {
            for (String[] predicates : _excludePairings) {
                boolean part0match = false;
                boolean part1match = false;

                for (String regex : predicates) {
                    if (part0.matches(regex)) {
                        part0match = true;
                    }
                    if (part1.matches(regex)) {
                        part1match = true;
                    }
                }

                return part0match && part1match;
            }
            return false;
        }

        public String getName() {
            return _name;
        }

        public boolean isMappedInterface() {
            return _isMappedInterface;
        }

        boolean debug() {
            return this._debug;
        }
    }
}
