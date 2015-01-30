package org.cloudbus.cloudsim.examples.SibSUTIS;

import javafx.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.CharacteristicsVector;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHost;
import org.cloudbus.cloudsim.examples.power.ExtendedHelper;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.*;

/**
 * Created by andrey on 1/8/15.
 */
public class VmAllocationPolicyNBG extends VmAllocationPolicy {


    /** The vm table. */
    private Map<String, Host> vmTable;
    public VmAllocationPolicyNBG(List<? extends Host> list) {
        super(list);
        setVmTable(new HashMap<String, Host>());
    }


    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }

    public Map<String, Host> getVmTable() {
        return vmTable;
    }
    private void printLogMsg(String msg) {
        Log.print("NBG_Allocator: " + msg + "\n");
    }

    private double calculateNormForVmOnHost(Vm vm, ExtendedHost host, CharacteristicsVector alphaVector) {
        double norm = -1.0d;
        CharacteristicsVector vmVector = new CharacteristicsVector();
        CharacteristicsVector hostVector = new CharacteristicsVector();
        if (host.isSuitableForVm(vm)) {
            vmVector.cpu = 1.0d*vm.getNumberOfPes() / host.getNumberOfPes();
            vmVector.ram = 1.0d*vm.getRam() / host.getRam();
            vmVector.hdd = 1.0d*vm.getSize() / host.getStorage();
        } else {
            printLogMsg("Vm not siutable for this host");
            return norm;
        }

        hostVector.cpu = 1.0d*host.getNumberOfFreePes()/host.getNumberOfPes();
        hostVector.ram = 1.0d*host.getRamProvisioner().getAvailableRam() / host.getRam();
        hostVector.hdd = 1.0d*host.getStorage() /host.getInitialStorage();
        printLogMsg("VM_VECTOR: "+vmVector.toString());
        printLogMsg("HOST_VECTOR: "+hostVector.toString());
        norm = alphaVector.cpu * Math.pow((vmVector.cpu - hostVector.cpu), 2);
        norm += alphaVector.ram * Math.pow((vmVector.ram - hostVector.ram), 2);
        norm += alphaVector.hdd * Math.pow((vmVector.hdd - hostVector.hdd),2);
        return norm;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        Vector<Pair<ExtendedHost, Double>> normalizedHosts = new Vector<Pair<ExtendedHost, Double>>();
        CharacteristicsVector alphaVector = new CharacteristicsVector(1,1,1);
        for (Host host: getHostList()) {
            printLogMsg("Host class: "+host.getClass().toString());
            Pair<ExtendedHost, Double> norm = new Pair<ExtendedHost, Double>((ExtendedHost)host, calculateNormForVmOnHost(vm,
                    (ExtendedHost)host,alphaVector));
            if (norm.getValue() > 0) {
                printLogMsg("Adding vector norm: "+norm.getValue());
                normalizedHosts.add(norm);
            }
        }
        Collections.sort(normalizedHosts, new Comparator<Pair<ExtendedHost, Double>>() {
            @Override
            public int compare(Pair<ExtendedHost, Double> o1, Pair<ExtendedHost, Double> o2) {
                return (int)(o1.getValue() - o2.getValue());
            }
        });
        if (normalizedHosts.isEmpty()) {
            printLogMsg("Can't found apropriate host for vm. Vm list is empty!");
            return false;

        }
        Double norm = normalizedHosts.firstElement().getValue();
        ExtendedHost host = normalizedHosts.firstElement().getKey();
        printLogMsg("Allocate on host with CPU: "+host.getNumberOfFreePes() +
                " RAM: "+host.getRamProvisioner().getAvailableRam() +
                " HDD: "+host.getStorage());
        printLogMsg("Allocate vm on host with norm: "+norm);

        normalizedHosts.firstElement().getKey().vmCreate(vm);
        getVmTable().put(vm.getUid(), host);
        return true;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        //Do not even try to do this!
        throw new RuntimeException("Not implemented!", null);
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    @Override
    public Host getHost(Vm vm){
        return getVmTable().get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Vm.getUid(userId, vmId));
    }
}
