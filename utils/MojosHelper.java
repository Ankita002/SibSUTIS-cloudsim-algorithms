package org.cloudbus.cloudsim.examples.SibSUTIS.utils;


import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerVm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MojosHelper extends ExtendedHelper {
    public static List<Vm> createVmList(int brokerId, int vmsNumber) {
        //Do you know better place?
        List<MojosXmlParser.MojosTask> taskList = MojosXmlParser.parse("/tmp/mojos/test.xml");
        if (taskList.size() < vmsNumber) {
            throw new RuntimeException("Mojos taskList size less than vms to create!");
        }

        List<Vm> vms = new ArrayList<Vm>();
        //At this moment only one request to task
        for (int i = 0; i < vmsNumber; i++) {
            int vmType = taskList.get(i).requstList.get(0).vmType;
            if (vmType > ExtendedConstants.VM_TYPES - 1) {
                vmType = (vmType % ExtendedConstants.VM_TYPES - 1);
            }
            Log.print("Create task of type: " + vmType);
            vms.add(new PowerVm(
                    i,
                    brokerId,
                    ExtendedConstants.VM_MIPS[vmType],
                    ExtendedConstants.VM_PES[vmType],
                    ExtendedConstants.VM_RAM[vmType],
                    ExtendedConstants.VM_BW,
                    ExtendedConstants.VM_SIZE,
                    1,
                    "Xen",
                    new CloudletSchedulerDynamicWorkload(ExtendedConstants.VM_MIPS[vmType], ExtendedConstants.VM_PES[vmType]),
                    ExtendedConstants.SCHEDULING_INTERVAL));
        }
        return vms;
    }

}
