package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.examples.SibSUTIS.ListAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerDatacenter;

import java.util.List;

/**
 * Created by andrey on 30.01.15.
 */
public class ExtendedDatacenter extends PowerDatacenter {
    public ExtendedDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    @Override
    public void processEvent(SimEvent ev) {
        Log.printLine("Extended datacenter: process event: "+ev.getTag());
        if (ev.getTag() == ExtendedDatacenterBrocker.ALLOCATE_VM_LIST_TAG) {
            ListAllocationPolicy policy = (ListAllocationPolicy)getVmAllocationPolicy();
            List<Vm> vmList = (List<Vm>)ev.getData();
            boolean result = policy.allocateHostForVmList(vmList);
            if (result) {
                for (Vm vm: vmList){
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = vm.getId();

                    if (result) {
                        data[2] = CloudSimTags.TRUE;
                    } else {
                        data[2] = CloudSimTags.FALSE;
                    }
                    send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
                }

                getVmList().addAll(vmList);
                for (Vm vm: vmList) {
                    if (vm.isBeingInstantiated()) {
                        vm.setBeingInstantiated(false);
                    }
                    vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                            .getAllocatedMipsForVm(vm));
                }
            }
        } else {
            super.processEvent(ev);
        }
    }
}
