package org.cloudbus.cloudsim.examples.SibSUTIS.vmAllocationPolicyes;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import java.util.*;

/**
 * Created by andrey on 1/8/15.
 */
public class VmAllocationPolicyRandom extends VmAllocationPolicy {


    /** The vm table. */
    private Map<String, Host> vmTable;
    int lastAllocatedHost;

    /**
     * Creates the new VmAllocationRoundRobin object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    public VmAllocationPolicyRandom(List<? extends Host> list) {
        super(list);
        lastAllocatedHost = -1; //We want to start from 0 idx
        setVmTable(new HashMap<String, Host>());
    }

    /**
     * Sets the vm table.
     *
     * @param vmTable the vm table
     */
    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }
    /**
     * Gets the vm table.
     *
     * @return the vm table
     */
    public Map<String, Host> getVmTable() {
        return vmTable;
    }
    private void printLogMsg(String msg) {
        Log.print("RAND_Allocator: " + msg + "\n");
    }
    boolean tryToAllocateVmToHost(Host host, Vm vm) {
        if(host.isSuitableForVm(vm)) {
            boolean result = host.vmCreate(vm);
            if(result) {
                printLogMsg("Vm created successfuly");
                getVmTable().put(vm.getUid(), host);
                return true;
            } else {
                printLogMsg("Vm creation failed");
            }
        }
        return false;
    }
    @Override
    public boolean allocateHostForVm(Vm vm) {
        printLogMsg("Allocate host for vm");
        if(getHostList().size() == 0) {
            return tryToAllocateVmToHost(getHostList().get(0),vm);
        }

        Random randomGenerator = new Random();
        int visitedHosts = 0;
        boolean visited[] = new boolean[getHostList().size()];
        Arrays.fill(visited,0,getHostList().size(),false);
        while (visitedHosts < getHostList().size()) {
            int randomHostNum = randomGenerator.nextInt(getHostList().size());
            //We don't want to check one host twice
            if (visited[randomHostNum])
                continue;
            printLogMsg("Try to allocate host: "+randomHostNum);
            boolean res = tryToAllocateVmToHost(getHostList().get(randomHostNum), vm);
            if(res) {
                return true;
            } else {
                visitedHosts++;
                visited[randomHostNum] = true;
            }
        }
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        printLogMsg("Allocate specified host for vm");
        return false;
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
