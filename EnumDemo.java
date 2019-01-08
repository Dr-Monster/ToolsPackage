package com.example.demo;

/**
 * 枚举类demo
 */
public enum EnumDemo {
    TestA(111 , "TestA" , "typeA" , true , 211 , "aliNameA"),
    TestB(222 , "TestB" , "typeB" , true , 221 , "aliNameB"),
    TestC(333 , "TestC" , "typeC" , true , 231 , "aliNameC"),
    TestD(444 , "TestD" , "typeD" , true , 241 , "aliNameD");
    int tCode;
    String name;
    String type;
    boolean turnOn;
    int pCode;
    String aliName;

    EnumDemo(int tCode, String name, String type, boolean turnOn, int pCode, String aliName) {
        this.tCode = tCode;
        this.name = name;
        this.type = type;
        this.turnOn = turnOn;
        this.pCode = pCode;
        this.aliName = aliName;
    }

    public int gettCode() {
        return tCode;
    }

    public void settCode(int tCode) {
        this.tCode = tCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTurnOn() {
        return turnOn;
    }

    public void setTurnOn(boolean turnOn) {
        this.turnOn = turnOn;
    }

    public int getpCode() {
        return pCode;
    }

    public void setpCode(int pCode) {
        this.pCode = pCode;
    }

    public String getAliName() {
        return aliName;
    }

    public void setAliName(String aliName) {
        this.aliName = aliName;
    }
}

class callDemo{
    /**
     * 遍历,加入到集合或者其他地方都可以
     */
    EnumDemo[] enumDemo = EnumDemo.values();
}
