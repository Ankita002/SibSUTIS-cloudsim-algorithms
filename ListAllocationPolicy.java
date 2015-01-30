package org.cloudbus.cloudsim.examples.SibSUTIS;

import org.cloudbus.cloudsim.Vm;

import java.util.List;

/**
 * Created by andrey on 30.01.15.
 */
public interface ListAllocationPolicy {
    public boolean allocateHostForVmList(List<Vm> vmsToAllocate);
}
