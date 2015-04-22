package org.cloudbus.cloudsim.examples.SibSUTIS;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.CharacteristicsVector;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHost;

import java.util.*;

public class VmAllocationPolicyNBG extends VmAllocationPolicy implements ListAllocationPolicy {


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
        double norm = 0;
        CharacteristicsVector hostVector = new CharacteristicsVector();
        hostVector.cpu = 1.0d*host.getNumberOfFreePes() - host.getNumberOfPes();
        hostVector.ram = 1.0d*host.getRamProvisioner().getAvailableRam() - host.getRam();
        hostVector.hdd = 1.0d*host.getStorage() - host.getInitialStorage();

        norm = alphaVector.cpu * Math.pow((vm.getNumberOfPes() - host.getNumberOfFreePes()), 2);
        norm += alphaVector.ram * Math.pow((vm.getRam() - host.getRamProvisioner().getAvailableRam()), 2);
        norm += alphaVector.hdd * Math.pow((vm.getSize() - host.getStorage()),2);
        return norm;
    }
    public CharacteristicsVector calculateAlphaVector(List<Vm> vmList) {
        CharacteristicsVector vector = new CharacteristicsVector(0, 0, 0);
        for (Vm vm: vmList) {
            vector.ram += vm.getRam();
            vector.cpu += vm.getNumberOfPes();
            vector.hdd += vm.getSize();
        }
        vector.hdd /= vmList.size();
        vector.ram /= vmList.size();
        vector.cpu /= vmList.size();
        return vector;
    }
    public boolean allocateHostForVmWithAlpha(Vm vm,CharacteristicsVector alphaVector) {
        Vector<Pair<ExtendedHost, Double>> normalizedHosts = new Vector<Pair<ExtendedHost, Double>>();
        Vector<Pair<ExtendedHost, Double>> normalizedUnusedHosts = new Vector<Pair<ExtendedHost, Double>>();
        for (Host host : getHostList()) {
            if (!host.isSuitableForVm(vm)) {
                continue;
            }
            Pair<ExtendedHost, Double> norm = new Pair<ExtendedHost, Double>((ExtendedHost) host, calculateNormForVmOnHost(vm,
                    (ExtendedHost) host, alphaVector));
            if (norm.getValue() > 0) {
                if (host instanceof  ExtendedHost) {
                    if (((ExtendedHost) host).isUsed()) {
                        Log.printLine("Host "+host.getId() + " is suitable and used");
                        normalizedHosts.add(norm);
                    } else {
                        normalizedUnusedHosts.add(norm);
                    }
                } else {
                    normalizedHosts.add(norm);
                }
            }
        }
        Pair<ExtendedHost,Double> norm = null;
        Comparator<Pair<ExtendedHost, Double>> comparator = new Comparator<Pair<ExtendedHost, Double>>() {
            @Override
            public int compare(Pair<ExtendedHost, Double> o1, Pair<ExtendedHost, Double> o2) {
                if (o1.getValue().equals(o2.getValue())) {
                    return 0;
                } else {
                    return o1.getValue() > o2.getValue() ? 1 : -1;
                }
            }
        };
        Log.printLine("Used size: "+normalizedHosts.size()+" Unused: "+normalizedUnusedHosts.size());
        if (!normalizedHosts.isEmpty()) {
//                Log.printLine("Choose one of used hosts");
            Collections.sort(normalizedHosts, comparator);
            norm = normalizedHosts.firstElement();
        } else if (!normalizedUnusedHosts.isEmpty()) {
//                Log.printLine("Choose one of unused hosts");
            Collections.sort(normalizedUnusedHosts, comparator);
            norm = normalizedUnusedHosts.firstElement();
        } else {
            printLogMsg("Can't found appropriate host for vm. Vm list is empty!");
            throw new RuntimeException("Cannot found appropriate host for vm: " + vm.getId());
        }
        ExtendedHost host = norm.getKey();
        printLogMsg("Allocate on host with CPU: " + host.getNumberOfFreePes() +
                " RAM: " + host.getRamProvisioner().getAvailableRam() +
                " HDD: " + host.getStorage() +
                " norm: "+norm.getValue());

        host.vmCreate(vm);
        getVmTable().put(vm.getUid(), host);
        return true;
    }
    @Override
    public boolean allocateHostForVm(Vm vm) {
        return allocateHostForVmWithAlpha(vm, new CharacteristicsVector(1,1,1));
    }

    @Override
    public boolean allocateHostForVmList(List<Vm> vmsToAllocate) {
        int allocatedVms = 0;
        CharacteristicsVector alphaVector = calculateAlphaVector(vmsToAllocate);
        for (Vm vm: vmsToAllocate) {
            if (allocateHostForVmWithAlpha(vm,alphaVector))
                allocatedVms++;
            else
                break;
        }
        if (allocatedVms == vmsToAllocate.size())
            return true;
        return false;
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
