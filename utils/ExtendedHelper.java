package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 30.01.15.
 */
public class ExtendedHelper extends org.cloudbus.cloudsim.examples.power.Helper {
    public static List<PowerHost> createHostList(int hostsNumber) {
        List<PowerHost> hostList = new ArrayList<PowerHost>();
        for (int i = 0; i < hostsNumber; i++) {
            int hostType = i % Constants.HOST_TYPES;

            List<Pe> peList = new ArrayList<Pe>();
            for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
            }
            //NOTE: Use our own implementation of powerHost!
            hostList.add(new ExtendedHost(
                    i,
                    new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
                    new BwProvisionerSimple(Constants.HOST_BW),
                    Constants.HOST_STORAGE,
                    peList,
                    new VmSchedulerTimeSharedOverSubscription(peList),
                    Constants.HOST_POWER[hostType]));
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


}
