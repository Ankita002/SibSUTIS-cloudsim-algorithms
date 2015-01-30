package org.cloudbus.cloudsim.examples.SibSUTIS.utils;

/**
 * Created by andrey on 30.01.15.
 */
public class CharacteristicsVector {
    public CharacteristicsVector(int cpu, int ram, int hdd) {
        this.cpu = cpu;
        this.ram = ram;
        this.hdd = hdd;
    }
    public CharacteristicsVector() {
        cpu = ram = hdd = -1;
    }
    @Override
    public String toString(){
        return "CPU: "+cpu+" RAM: "+ram+" HDD:"+hdd;
    }

    public double cpu;
    public double ram;
    public double hdd;
};