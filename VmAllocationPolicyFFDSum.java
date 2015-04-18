package org.cloudbus.cloudsim.examples.SibSUTIS;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.CharacteristicsVector;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHost;

import java.util.*;


public class VmAllocationPolicyFFDSum extends VmAllocationPolicy implements ListAllocationPolicy{
    /** The vm table. */
    private Map<String, Host> vmTable;
    public VmAllocationPolicyFFDSum(List<? extends Host> list) {
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
        Log.print("FFDSum_Allocator: " + msg + "\n");
    }

    private Vector<Pair<Vm,Double>> calculateWeightsForVms(List<Vm> vmList, CharacteristicsVector coefficients) {
        Vector<Pair<Vm,Double>> vector = new Vector<Pair<Vm, Double>>(vmList.size());
        Double weight;
        for(Vm vm: vmList) {
            weight = 1.0d*vm.getNumberOfPes()*coefficients.cpu;
            weight += vm.getSize()*coefficients.hdd;
            weight += vm.getRam()*coefficients.ram;
            vector.add(new Pair<Vm, Double>(vm,weight));

        }
        return vector;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        //Do not even try to do this!
        throw new RuntimeException("Not implemented!", null);
    }
    private CharacteristicsVector calculateCoefficientsForHost(List<Vm> vmList, ExtendedHost host){
        CharacteristicsVector vector = new CharacteristicsVector(0.0d,0.0d,0.0d);
        for (Vm vm:vmList) {
//            printLogMsg("vm: "+vm.getNumberOfPes() + " : "+vm.getRam() + " : " + vm.getSize());
            vector.cpu += 1.0d*vm.getNumberOfPes() / host.getRam();
            vector.ram += 1.0d*vm.getRam() / host.getRam();
            vector.hdd += 1.0d*vm.getSize() / host.getInitialStorage();
        }
        return vector;
    }

    @Override
    public boolean allocateHostForVmList(List<Vm> vmsToAllocate) {
        //Bin-centric


        //We don't need to side-effect here
        List<Vm> vmList = new ArrayList<Vm>(vmsToAllocate);
        printLogMsg("Allocate host for vmList: "+vmsToAllocate.size());

        Vector<Pair<Vm, Double>> vmsVector = null;


        long iterationCount = 0;
        long iterationThreshold = getHostList().size() * vmList.size();
        for (Host host: getHostList()) {
            //All items allocated
            printLogMsg("Look at host: " + host.getId() + " Vms: "+vmList.size());
            if(vmList.size() <= 0) {
                printLogMsg("We're allocate all requested vms");
                break;
            }
            CharacteristicsVector coefficients = calculateCoefficientsForHost(vmList, (ExtendedHost)host);
            printLogMsg("Coefs; cpu: " + coefficients.cpu +
            " ram: " + coefficients.ram +
            " hdd: " + coefficients.hdd);
            vmsVector = calculateWeightsForVms(vmList, coefficients);
            Collections.sort(vmsVector, new Comparator<Pair<Vm, Double>>() {
                @Override
                public int compare(Pair<Vm, Double> o1, Pair<Vm, Double> o2) {
                    return (int)(o1.getValue() - o2.getValue());
                }
            });
            Collections.reverse(vmsVector);

            //first item is largest
            for (int i = 0; i < vmsVector.size(); i++) {
                //I want to avoid cycling
                iterationCount++;
                assert(iterationCount<=iterationThreshold);
                Vm vm = vmsVector.get(i).getKey();
                if (host.isSuitableForVm(vm)) {
                    host.vmCreate(vm);
                    getVmTable().put(vm.getUid(), host);
                    int vmId = vm.getId();
                    for (int j = 0; j<vmList.size(); j++) {
                        if (vmList.get(j).getId() == vmId) {
                            vmList.remove(j);
                            break;
                        }
                    }
                    vmsVector.remove(i);
                    printLogMsg("Allocate vm: "+vm.getId() + " on host: "+host.getId());
                    i -= 1;
                }
            }

        }
        if(vmList.size() == 0) {
            printLogMsg("All vms successfully allocated!");
            return true;
        } else {
            //TODO: deallocate vms
            printLogMsg("ERROR! Vms doesn't allocated; Remaining size: "+vmList.size());
            return false;
        }
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
