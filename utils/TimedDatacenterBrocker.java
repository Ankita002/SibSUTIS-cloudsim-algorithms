package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


public class TimedDatacenterBrocker extends PowerDatacenterBroker {
    TreeSet<MojosXmlParser.MojosTask> mojosTaskSet;
    public TimedDatacenterBrocker(String name) throws Exception {
        super(name);
        mojosTaskSet = new TreeSet<MojosXmlParser.MojosTask>();
    }
    @Override
    public void run() {
        super.run();
        if (mojosTaskSet.size() > 0) {
            if (mojosTaskSet.first().arrivalTime <= CloudSim.clock()) {
                while ((!mojosTaskSet.isEmpty()) && mojosTaskSet.first().arrivalTime <= CloudSim.clock()) {
                    launchVm(mojosTaskSet.first());
                    mojosTaskSet.remove(mojosTaskSet.first());
                }
            }
        }
        List<TimedVm> vmsToRemove = new ArrayList<TimedVm>();
        for (TimedVm vm: (List<TimedVm>)vmList) {
            if (vm.getTerminationTime() <= CloudSim.clock()) {
                Log.printLine("TIMEOUT: Remove vm");
                sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
                vmsToRemove.add(vm);
            }
        }
        if (vmsToRemove.size() != 0) {
            vmsCreatedList.removeAll(vmsToRemove);
            vmList.removeAll(vmsToRemove);
            Log.printLine("Vms left:"+vmList.size());
        }
        //Smolyak; Prevent simulation termination
        if (vmList.size() > 0 || mojosTaskSet.size() > 0) {
            if (CloudSim.globalLog)
                Log.printLine("TimedDCB: send keepAlive message");
            CloudSim.addKeepAliveEvent(60);
        }
    }
    public void launchVm(MojosXmlParser.MojosTask task) {
        int vmType = task.requstList.get(0).vmType;
        if (vmType > ExtendedConstants.VM_TYPES - 1) {
            vmType = (vmType % ExtendedConstants.VM_TYPES - 1);
        }
        Log.printLine("Launch new VM of type: "+vmType);
        TimedVm vm = new TimedVm(
                getVmList().size(),
                getId(),
                ExtendedConstants.VM_MIPS[vmType],
                ExtendedConstants.VM_PES[vmType],
                ExtendedConstants.VM_RAM[vmType],
                ExtendedConstants.VM_BW,
                ExtendedConstants.VM_SIZE,
                1,
                "Xen",
                new CloudletSchedulerDynamicWorkload(ExtendedConstants.VM_MIPS[vmType], ExtendedConstants.VM_PES[vmType]),
                ExtendedConstants.SCHEDULING_INTERVAL);
        double ttl = task.requstList.get(0).time;
        //Only multiple of hour
        if (ttl % 3600 != 0) {
            ttl += 3600 - (ttl % 3600);
        }
        vm.setTerminationTime(CloudSim.clock() + ttl);
        getVmList().add(vm);
        getVmsCreatedList().add(vm);

        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModelNull = new UtilizationModelNull();
        Cloudlet cloudlet = new Cloudlet(
                getCloudletList().size(),
                Cloudlet.INFINITE,
                Constants.CLOUDLET_PES,
                fileSize,
                outputSize,
                new UtilizationModelStochastic(),
                utilizationModelNull,
                utilizationModelNull);
        cloudlet.setUserId(getId());
        getCloudletList().add(cloudlet);
        bindCloudletToVm(cloudlet.getCloudletId(),vm.getId());

        //Smolyak; We're working with only one DC for now
        //TODO: Add multiple DC support
        createVmsInDatacenter(getDatacenterIdsList().get(0));
    }

    public void addMojosTask (MojosXmlParser.MojosTask task) {
        mojosTaskSet.add(task);
    }
    public void addMojosTaskList (List<MojosXmlParser.MojosTask> taskList) {
        mojosTaskSet.addAll(taskList);
    }

    //For debug purposes only
    public void printMojosTaskSet() {
        for (MojosXmlParser.MojosTask task : mojosTaskSet) {
            Log.printLine("MojosTask:"+task.guid+" arrival time:"+task.arrivalTime);
            for (MojosXmlParser.MojosRequest request:task.requstList) {
                Log.printLine("Request time:"+request.time);
            }
        }
    }
}
