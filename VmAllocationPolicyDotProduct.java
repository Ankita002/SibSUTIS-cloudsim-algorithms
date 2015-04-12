package org.cloudbus.cloudsim.examples.SibSUTIS;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.CharacteristicsVector;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHost;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by andrey on 1/8/15.
 */
public class VmAllocationPolicyDotProduct extends VmAllocationPolicy {


    /** The vm table. */
    private Map<String, Host> vmTable;
    Logger logger;
    public VmAllocationPolicyDotProduct(List<? extends Host> list) {
        super(list);
        setVmTable(new HashMap<String, Host>());
    }


    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }

    public Map<String, Host> getVmTable() {
        return vmTable;
    }
    private void printLogMsg(String msg) {
        if (logger == null) {
            logger = Logger.getLogger("DotProdLog");
            FileHandler fh;

            try {
                fh = new FileHandler("/tmp/dot_prod_allocator.log");
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("Dot_Prod_Allocator: " + msg + "\n");
    }

    private double calculateDotProdForVmOnHost(Vm vm, ExtendedHost host, CharacteristicsVector alphaVector) {
        double dotprod = 0.0d;
        CharacteristicsVector vmVector = new CharacteristicsVector();
        CharacteristicsVector hostVector = new CharacteristicsVector();
        if (host.isSuitableForVm(vm)) {
            vmVector.cpu = 1.0d*vm.getNumberOfPes() / host.getNumberOfPes();
            vmVector.ram = 1.0d*vm.getRam() / host.getRam();
            vmVector.hdd = 1.0d*vm.getSize() / host.getStorage();
        } else {
            printLogMsg("Vm not siutable for this host");
            return dotprod;
        }

        hostVector.cpu = 1.0d*host.getNumberOfFreePes()/host.getNumberOfPes();
        hostVector.ram = 1.0d*host.getRamProvisioner().getAvailableRam() / host.getRam();
        hostVector.hdd = 1.0d*host.getStorage() /host.getInitialStorage();
//        printLogMsg("VM_VECTOR: "+vmVector.toString());
//        printLogMsg("HOST_VECTOR: "+hostVector.toString());
        dotprod = vmVector.cpu * hostVector.cpu;
        dotprod += vmVector.ram * hostVector.ram;
        dotprod += vmVector.hdd * hostVector.hdd;
        return dotprod;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        Vector<Pair<ExtendedHost, Double>> dotProds = new Vector<Pair<ExtendedHost, Double>>();
        CharacteristicsVector alphaVector = new CharacteristicsVector(1,1,1);

        List<Host> unusedHostList = new ArrayList<Host>();

        for (Host host: getHostList()) {
            //On this step we're working with powered on hosts only
            if (!host.isSuitableForVm(vm)) {
                continue;
            }
            if (!((ExtendedHost)host).isUsed()) {
                unusedHostList.add(host);
                continue;
            }
            printLogMsg("Host class: "+host.getClass().toString());

            Pair<ExtendedHost, Double> dotProd = new Pair<ExtendedHost, Double>((ExtendedHost)host, calculateDotProdForVmOnHost(vm,
                    (ExtendedHost) host, alphaVector));
            if (dotProd.getValue() > 0) {
                printLogMsg("Adding vector  dot prod: "+dotProd.getValue());
                dotProds.add(dotProd);
            }
        }
        if (!dotProds.isEmpty()) {
            Collections.sort(dotProds, new Comparator<Pair<ExtendedHost, Double>>() {
                @Override
                public int compare(Pair<ExtendedHost, Double> o1, Pair<ExtendedHost, Double> o2) {
                    return (int) (o1.getValue() - o2.getValue());
                }
            });
            if (dotProds.isEmpty()) {
                printLogMsg("Can't found apropriate host for vm. Vm list is empty!");
                return false;

            }
            Double dotProd = dotProds.firstElement().getValue();
            ExtendedHost host = dotProds.firstElement().getKey();
            printLogMsg("Allocate on host: " + host.getId() + "with CPU: " + host.getNumberOfFreePes() +
                    " RAM: " + host.getRamProvisioner().getAvailableRam() +
                    " HDD: " + host.getStorage());
            printLogMsg("Allocate vm on host with dotProd: " + dotProd);

            dotProds.firstElement().getKey().vmCreate(vm);
            getVmTable().put(vm.getUid(), host);
            return true;
        }
        if(!unusedHostList.isEmpty()) {
            //Only suitable vms in this list
//            Collections.sort(unusedHostList, new Comparator<Host>() {
//                @Override
//                public int compare(Host o1, Host o2) {
//                    return (int) (((ExtendedHost)o1).getMaxPower() -((ExtendedHost)o2).getMaxPower() );
//                }
//            });
            ExtendedHost host = (ExtendedHost)unusedHostList.get(0);
            printLogMsg("Turning on new host: "+host.getId());
            printLogMsg("Allocate on host: " + host.getId() +
                    "with CPU: " + host.getNumberOfFreePes() +
                    " RAM: " + host.getRamProvisioner().getAvailableRam() +
                    " HDD: " + host.getStorage());
            //Allocate vm on host with lowest power consumption.
            boolean res = host.vmCreate(vm);
            if (!res)
                throw new RuntimeException("No way!");
            getVmTable().put(vm.getUid(), host);
            return true;
        }
        printLogMsg("ERROR! Cannot allocate vm");
        throw new RuntimeException();
//        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        //Do not even try to do this!
        throw new RuntimeException("Not implemented!", null);
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    @Override
    public Host getHost(Vm vm){
        return getVmTable().get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Vm.getUid(userId, vmId));
    }
}
