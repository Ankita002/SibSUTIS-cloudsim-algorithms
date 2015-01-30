package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

/**
 * Created by andrey on 30.01.15.
 */
public class ExtendedDatacenter extends Datacenter {
    public ExtendedDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == ExtendedDetecenterBrocker.ALLOCATE_VM_LIST_TAG) {
            getVmAllocationPolicy();
        } else {
            super.processEvent(ev);
        }
    }
}
