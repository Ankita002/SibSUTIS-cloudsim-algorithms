package org.cloudbus.cloudsim.examples.SibSUTIS;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;


import java.util.*;

/**
 * Created by andrey on 30.01.15.
 */
public class VmAllocationPolicyFFDProd extends VmAllocationPolicy implements ListAllocationPolicy{
    /** The vm table. */
    private Map<String, Host> vmTable;
    public VmAllocationPolicyFFDProd(List<? extends Host> list) {
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
        Log.print("FFDProd_Allocator: " + msg + "\n");
    }

    private Vector<Pair<Vm,Double>> calculateWeightsForVms(List<Vm> vmList) {
        Vector<Pair<Vm,Double>> vector = new Vector<Pair<Vm, Double>>(vmList.size());
        Double weight;
        for(Vm vm: vmList) {
            weight = 1.0d*vm.getNumberOfPes();
            weight *= vm.getSize();
            weight *= vm.getRam();
            vector.add(new Pair<Vm, Double>(vm,weight));

        }
        return vector;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        //Do not even try to do this!
        throw new RuntimeException("Not implemented!", null);
    }

    @Override
    public boolean allocateHostForVmList(List<Vm> vmsToAllocate) {
        //Bin-centric

        printLogMsg("Allocate host for vmList: "+vmsToAllocate.size());

        Vector<Pair<Vm, Double>> vmsVector = calculateWeightsForVms(vmsToAllocate);
        Collections.sort(vmsVector, new Comparator<Pair<Vm, Double>>() {
            @Override
            public int compare(Pair<Vm, Double> o1, Pair<Vm, Double> o2) {
                return (int)(o1.getValue() - o2.getValue());
            }
        });
        Collections.reverse(vmsVector);
        long iterationCount = 0;
        long iterationThreshold = getHostList().size() * vmsVector.size();
        for (Host host: getHostList()) {
            //All items allocated
            printLogMsg("Look at host: "+host.getId());
            if(vmsVector.size() <= 0) {
                printLogMsg("We're allocate all requested vms");
                break;
            }
            //first item is largest
            for (int i = 0; i < vmsVector.size(); i++) {
                //I want to avoid cycling
                iterationCount++;
                assert(iterationCount<=iterationThreshold);
                Vm vm = vmsVector.get(i).getKey();
                if (host.isSuitableForVm(vm)) {
                    host.vmCreate(vm);
                    getVmTable().put(vm.getUid(), host);
                    vmsVector.remove(i);
                    printLogMsg("Allocate vm: "+vm.getId() + " on host: "+host.getId());
                    i = 0;
                    continue;
                }
            }

        }
        if(vmsVector.size() == 0) {
            printLogMsg("All vms successfully allocated!");
            return true;
        } else {
            //TODO: deallocate vms
            printLogMsg("ERROR! Vms doesn't allocated");
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
