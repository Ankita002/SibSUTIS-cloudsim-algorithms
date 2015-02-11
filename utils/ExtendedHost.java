package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;


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
    public boolean isUsed(){
        if(getRam() != getRamProvisioner().getAvailableRam() ||
                getInitialStorage() != getStorage() ||
                getNumberOfFreePes() != getNumberOfPes())
            return true;
        return false;
    }
    public long getInitialStorage(){
        return initialStorage;
    }
}
