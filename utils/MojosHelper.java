package org.cloudbus.cloudsim.examples.SibSUTIS.utils;


import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerVm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MojosHelper extends ExtendedHelper {
    private static boolean enableBatchNodes = true;
    public static List<Vm> createVmList(int brokerId, int vmsNumber, String inputFile) {
        List<Vm> vms = new ArrayList<Vm>();
        List<MojosXmlParser.MojosTask> taskList = MojosXmlParser.parse(inputFile);
        if (taskList.size() < vmsNumber) {
            throw new RuntimeException("Mojos taskList size less than vms to create!");
        }
        int vmsCreated = 0;
        int taskNumber = 0;
        if (enableBatchNodes) {
            while (vmsCreated < vmsNumber) {
                int vmType = taskList.get(taskNumber).requstList.get(0).vmType;
                Log.print("Vm type from list: " + vmType + " nodes:" + taskList.get(taskNumber).requstList.get(0).nodes + "\n");
                if (vmType > ExtendedConstants.VM_TYPES - 1) {
                    vmType = (vmType % ExtendedConstants.VM_TYPES - 1);
                }
                //Vm count in batch == nodes count in request
                for (int i = 0; vmsCreated < vmsNumber && i < taskList.get(taskNumber).requstList.get(0).nodes; i++) {
                    Log.print("Create task of type: " + vmType + "\n");
                    vms.add(new PowerVm(
                            vmsCreated,
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
                    vmsCreated++;
                }
                taskNumber++;
            }
            Log.printLine("Mojos helper created " + vmsCreated + " number of vms\n");
        } else {
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
        }


        return vms;
    }
}
