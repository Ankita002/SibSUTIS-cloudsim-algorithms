package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;

import java.util.ArrayList;
import java.util.List;


public class ExtendedDatacenterBrocker extends PowerDatacenterBroker {
    public static final int VM_ALLOCATION_MODE_STANDART = 0;
    public static final int VM_ALLOCATION_MODE_LIST = 1;

    public static final int ALLOCATE_VM_LIST_TAG = 900;
    private int vmAllocationMode_;
    public ExtendedDatacenterBrocker(String name) throws Exception {
        super(name);
        vmAllocationMode_ = VM_ALLOCATION_MODE_STANDART;
    }
    public ExtendedDatacenterBrocker(String name, int vmAllocationMode) throws Exception {
        super(name);
        this.vmAllocationMode_ = vmAllocationMode;
    }

    @Override
    protected void createVmsInDatacenter(int datacenterId) {
        Log.printLine("Extended brocker: try to allocate vms for datacenter: "+datacenterId);
        if (vmAllocationMode_ == VM_ALLOCATION_MODE_LIST) {
            List<Vm> vmsToAllocate = new ArrayList<Vm>();
            for (Vm vm: getVmList()){
                if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                    vmsToAllocate.add(vm);
                }
            }
            if (vmsToAllocate.size() > 0) {
                Log.printLine("Extended brocker: send msg with vms to allocate: "+vmsToAllocate.size());
                sendNow(datacenterId, ALLOCATE_VM_LIST_TAG, vmsToAllocate);
                getDatacenterRequestedIdsList().add(datacenterId);

                setVmsRequested(vmsToAllocate.size());
                setVmsAcks(0);
            }
        } else {
            super.createVmsInDatacenter(datacenterId);
        }
    }

}
