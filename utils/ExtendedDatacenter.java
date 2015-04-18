package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.examples.SibSUTIS.ListAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerDatacenter;

import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ExtendedDatacenter extends PowerDatacenter {
    Logger logger;
    private int maximumUsedHostsCount = 0;

    public int getMaximumUsedHostsCount() {
        return maximumUsedHostsCount;
    }
    private void printLogMsg(String msg) {
        if (logger == null) {
            logger = Logger.getLogger("ExtendedDatacenterLog");
            FileHandler fh;

            try {
                fh = new FileHandler("/tmp/extended_datacenter.log");
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("Extended_datacenter: " + msg + "\n");
    }
    public ExtendedDatacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == ExtendedDatacenterBrocker.ALLOCATE_VM_LIST_TAG) {
            ListAllocationPolicy policy = (ListAllocationPolicy)getVmAllocationPolicy();
            List<Vm> vmList = (List<Vm>)ev.getData();
            printLogMsg("Before allocate: "+vmList.size()+" vms used hosts: "+ (getHostList().size() - getUnusedHostsCount()));
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
            int used = getHostList().size() - getUnusedHostsCount();
            if (used > maximumUsedHostsCount) {
                maximumUsedHostsCount = used;
            }
//            printLogMsg("Used hosts: "+ used);
        } else {
            super.processEvent(ev);
            int used = getHostList().size() - getUnusedHostsCount();
            if (used > maximumUsedHostsCount) {
                maximumUsedHostsCount = used;
            }
//            printLogMsg("Used hosts: "+ used);
        }

    }
    int getUnusedHostsCount() {
        int count = 0;
        List<ExtendedHost> hostList= getHostList();
        for (ExtendedHost host: hostList) {
            if(!host.isUsed()) {
                count++;
            }
        }
        return count;
    }
}
