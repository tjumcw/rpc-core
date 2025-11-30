package com.mcw.distributed.enums;

public enum RegistryOperationEnum {

    REGISTER("register"),

    DISCOVER("discover");

    private final String operation;
    RegistryOperationEnum(String operation) {
        this.operation = operation;
    }
    public String getOperation() {
        return operation;
    }
}
