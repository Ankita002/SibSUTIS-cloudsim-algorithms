package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class ExtendedDatacenterBrocker extends PowerDatacenterBroker {
    public static final int VM_ALLOCATION_MODE_STANDART = 0;
    public static final int VM_ALLOCATION_MODE_LIST = 1;

    public static final int ALLOCATE_VM_LIST_TAG = 900;
    private int vmAllocationMode_;
    Logger logger;
    private void printLogMsg(String msg) {
        if (logger == null) {
            logger = Logger.getLogger("ExtendedBrockerLog");
            FileHandler fh;

            try {
                fh = new FileHandler("/tmp/extended_brocker.log");
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("Extended_brocker: " + msg + "\n");
    }
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
        printLogMsg("Extended brocker: try to allocate vms for datacenter: "+datacenterId);
        printLogMsg("Trying to allocate: "+getVmList().size()+" vms");
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
