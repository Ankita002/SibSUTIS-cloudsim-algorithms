package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.List;


public class ExtendedDetecenterBrocker extends DatacenterBroker {
    public static final int VM_ALLOCATION_MODE_STANDART = 0;
    public static final int VM_ALLOCATION_MODE_LIST = 1;

    public static final int ALLOCATE_VM_LIST_TAG = 900;
    private int vmAllocationMode_;
    public ExtendedDetecenterBrocker(String name) throws Exception {
        super(name);
        vmAllocationMode_ = VM_ALLOCATION_MODE_STANDART;
    }
    public ExtendedDetecenterBrocker(String name, int vmAllocationMode) throws Exception {
        super(name);
        this.vmAllocationMode_ = vmAllocationMode;
    }

    @Override
    protected void createVmsInDatacenter(int datacenterId) {
        if (vmAllocationMode_ == VM_ALLOCATION_MODE_LIST) {
            List<Vm> vmsToAllocate = new ArrayList<Vm>();
            for (Vm vm: getVmList()){
                if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                    vmsToAllocate.add(vm);
                }
            }
            if (vmsToAllocate.size() > 0) {
                sendNow(datacenterId, ALLOCATE_VM_LIST_TAG, vmsToAllocate);
            }
        } else {
            super.createVmsInDatacenter(datacenterId);
        }
    }
}
