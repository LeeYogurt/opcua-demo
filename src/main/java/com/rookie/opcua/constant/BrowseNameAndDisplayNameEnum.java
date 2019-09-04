package com.rookie.opcua.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yugo
 */
public enum BrowseNameAndDisplayNameEnum {

    VARIABLE_NAME("variableName", "变量名"),

    VARIABLE_PROJECT("variableProject", "变量项目"),

    VARIABLE_UNITS("variableUnits", "单位"),

    VARIABLE_VALUE("variableValue", "变量值"),

    VARIABLE_RANGE("variableRange", "值范围"),

    VARIABLE_ALARM_VALUE("variableAlarmValue", "报警值");

    private String browseName;
    private String displayName;

    public String getBrowseName() {
        return browseName;
    }

    public void setBrowseName(String browseName) {
        this.browseName = browseName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    static Map<String, BrowseNameAndDisplayNameEnum> enumMap = new HashMap<>();

    static {
        for (BrowseNameAndDisplayNameEnum type : BrowseNameAndDisplayNameEnum.values()) {
            enumMap.put(type.getBrowseName(), type);
        }
    }


    BrowseNameAndDisplayNameEnum(String browseName, String displayName) {
        this.browseName = browseName;
        this.displayName = displayName;
    }

    public static BrowseNameAndDisplayNameEnum getEnum(String browseName) {
        return enumMap.get(browseName);
    }
}
