package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

/**
 * Created by andrey on 30.01.15.
 */
public class ExtendedHost extends PowerHostUtilizationHistory {
    private long initialStorage;
    public ExtendedHost(int id,
                        RamProvisioner ramProvisioner,
                        BwProvisioner bwProvisioner,
                        long storage,
                        List<? extends Pe> peList,
                        VmScheduler vmScheduler,
                        PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        initialStorage = storage;
    }
    public long getInitialStorage(){
        return initialStorage;
    }
}
