/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.SibSUTIS;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.sun.istack.internal.Nullable;
import javafx.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedDatacenter;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedDatacenterBrocker;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.ExtendedHelper;
import org.cloudbus.cloudsim.examples.SibSUTIS.utils.MojosHelper;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.random.RandomHelper;
import org.cloudbus.cloudsim.power.*;


/*TODO
1.Random
2.First FIt
3.Round RObin
4. FFD Prod
5. FFD Sum
6. Dot-Product
7. Norm-based Greedy
*/
public class SimulationRunner {

    public static class SimulationConfiguration {
        //Number of hosts in your simulation
        public int numberOfHosts;
        public int minVmsInSimulation;
        public int maxVmsInSimulation;
        public int simulationStep;
        public boolean useMojosData = false;
        public String outputPath = "/tmp/cloudsim/results";
        public List <Class<? extends VmAllocationPolicy>> vmAllocationPolicies;
        public List <String> inputFiles;
        public boolean isValid() {
            return (numberOfHosts > 0
                    && minVmsInSimulation < maxVmsInSimulation
                    && maxVmsInSimulation > 0
                    && simulationStep > 0
                    && vmAllocationPolicies != null
                    && vmAllocationPolicies.size() > 0
                    && inputFiles != null
                    && inputFiles.size() > 0
                    && outputPath.length() > 0
            );
        }

    }
    public static void doSimulationStep(@Nullable String inputFile, int stepNumber, SimulationConfiguration config) {
        File outputFolder = new File(config.outputPath + "/simulation_"+stepNumber);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        for (Class<? extends VmAllocationPolicy> vmallocationPolicyClass : config.vmAllocationPolicies) {
            List<Pair<Double, Integer>> resultList = new ArrayList<Pair<Double, Integer>>();
            PrintWriter logFileWriter = null;
            try {
                logFileWriter = new PrintWriter(
                        outputFolder.getAbsolutePath() + "/" +
                                vmallocationPolicyClass.getSimpleName() + ".txt");
            } catch (FileNotFoundException e) {
                new RuntimeException("Cannot open log file: " + e.getMessage());
            }
            for (int i = config.minVmsInSimulation;
                 i <= config.maxVmsInSimulation;
                 i += config.simulationStep) {
                //Simulation body
                VmAllocationPolicy policy = null;
                List<PowerHost> hostList = null;
                ExtendedDatacenterBrocker broker = null;
                List<Cloudlet> cloudletList = null;
                List<Vm> vmList = null;
                try {
                    int numberOfVms = i;
                    CloudSim.init(1, Calendar.getInstance(), false);
                    if (vmallocationPolicyClass == VmAllocationPolicyFFDProd.class
                            || vmallocationPolicyClass == VmAllocationPolicyFFDSum.class) {
                        broker = ExtendedHelper.createExtendedBrocker(ExtendedDatacenterBrocker.VM_ALLOCATION_MODE_LIST);
                    } else {
                        broker = ExtendedHelper.createExtendedBrocker(ExtendedDatacenterBrocker.VM_ALLOCATION_MODE_STANDART);
                    }
                    int brokerId = broker.getId();

                    cloudletList = RandomHelper.createCloudletList(
                            brokerId,
                            numberOfVms);
                    if (inputFile == null) {
                        vmList = ExtendedHelper.createVmList(brokerId, cloudletList.size());
                    } else {
                        vmList = MojosHelper.createVmList(brokerId, cloudletList.size(), inputFile);
                    }

                    hostList = ExtendedHelper.createHostList(config.numberOfHosts);

                    Constructor<?> cons = vmallocationPolicyClass.getConstructor(List.class);
                    policy = (VmAllocationPolicy) cons.newInstance(hostList);

                    ExtendedDatacenter datacenter = (ExtendedDatacenter) ExtendedHelper.createDatacenter(
                            "Datacenter",
                            ExtendedDatacenter.class,
                            hostList,
                            policy);
                    datacenter.setDisableMigrations(true);

                    broker.submitVmList(vmList);
                    broker.submitCloudletList(cloudletList);

                    CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
                    double lastClock = CloudSim.startSimulation();

                    double energy = datacenter.getPower() / (3600 * 1000);
                    resultList.add(new Pair<Double, Integer>(energy, datacenter.getMaximumUsedHostsCount()));

                } catch (Exception e) {
                    Log.printLine("Sumulation has been terminated due to unexpected exception: " +
                            e.getMessage());
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            for (int i = config.minVmsInSimulation, j = 0;
                 i <= config.maxVmsInSimulation && j < resultList.size();
                 i += config.simulationStep, j++) {
                Log.printLine("Res for vms[" + i + "]: \n" +
                        "Energy consumption:  " + resultList.get(j).getKey() + " kWh\n" +
                        "Used hosts: " + resultList.get(j).getValue() + "\n");
                logFileWriter.println("["+i+"]: "+resultList.get(j).getKey()+" KWh;"
                + " hosts: "+resultList.get(j).getValue());
            }
            logFileWriter.close();
        }
    }
    public static void startSimulation (SimulationConfiguration config) {
        if (!config.isValid()) {
            throw new RuntimeException("Simulation config isn't valid");
        }
        if (config.useMojosData) {
            for (int i = 0; i < config.inputFiles.size(); i++)
                doSimulationStep(config.inputFiles.get(i), i, config);
        } else {
            doSimulationStep(null, 0, config);
        }

    }

    public static void main(String[] args) throws IOException {
        SimulationConfiguration config = new SimulationConfiguration();
        config.numberOfHosts = 250;
        config.minVmsInSimulation = 100;
        config.simulationStep = 100;
        config.maxVmsInSimulation = 1000;

        //Specify your input files here
        config.useMojosData = true;
        config.inputFiles = Arrays.asList(
                "/tmp/mojos/test1.xml",
                "/tmp/mojos/test2.xml",
                "/tmp/mojos/test3.xml"
        );
        //Specify vm allocation policies which you're want to use
        config.vmAllocationPolicies = Arrays.asList(
                VmAllocationPolicyRandom.class,
                VmAllocationPolicyRoundRobin.class,
                VmAllocationPolicyFirstFit.class,
                VmAllocationPolicyNBG.class,
                VmAllocationPolicyFFDProd.class,
                VmAllocationPolicyFFDSum.class,
                VmAllocationPolicyDotProduct.class
        );
        startSimulation(config);
    }
}
