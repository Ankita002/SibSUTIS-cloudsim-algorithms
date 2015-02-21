package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.List;


public class ExtendedHelper extends org.cloudbus.cloudsim.examples.power.Helper {
    public static List<PowerHost> createHostList(int hostsNumber) {
        List<PowerHost> hostList = new ArrayList<PowerHost>();
        for (int i = 0; i < hostsNumber; i++) {
            int hostType = i % ExtendedConstants.HOST_TYPES;

            List<Pe> peList = new ArrayList<Pe>();
            for (int j = 0; j < ExtendedConstants.HOST_PES[hostType]; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(ExtendedConstants.HOST_MIPS[hostType])));
            }
            //NOTE: Use our own implementation of powerHost!
            hostList.add(new ExtendedHost(
                    i,
                    new RamProvisionerSimple(ExtendedConstants.HOST_RAM[hostType]),
                    new BwProvisionerSimple(ExtendedConstants.HOST_BW),
                    ExtendedConstants.HOST_STORAGE,
                    peList,
                    new VmSchedulerTimeSharedOverSubscription(peList),
                    ExtendedConstants.HOST_POWER[hostType]));
        }
        return hostList;
    }
    public static ExtendedDatacenterBrocker createExtendedBrocker(int vmAllocationMode) {
        ExtendedDatacenterBrocker broker = null;
        try {
            broker = new ExtendedDatacenterBrocker("Broker",vmAllocationMode);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return broker;
    }
    public static List<Vm> createVmList(int brokerId, int vmsNumber) {
        List<Vm> vms = new ArrayList<Vm>();
        for (int i = 0; i < vmsNumber; i++) {
            int vmType = i / (int) Math.ceil((double) vmsNumber / ExtendedConstants.VM_TYPES);
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
