package com.webstart.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.webstart.Enums.FeatureTypeEnum;

import java.util.List;

public class FeatureSensor {
    private int id;
    private String identifier;
    private String description;
    @JsonIgnore
    private FeatureTypeEnum deviceType;
    @JsonProperty(value = "sensors")
    private List<Sensor> sensorList;

    public FeatureSensor() {
    }

    public FeatureSensor(int id, String identifier, String description, List<Sensor> sensorList) {
        this.id = id;
        this.identifier = identifier;
        this.description = description;
        this.sensorList = sensorList;
    }

    public FeatureSensor(int id, String identifier, String description) {
        this.id = id;
        this.identifier = identifier;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FeatureTypeEnum getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(FeatureTypeEnum deviceType) {
        this.deviceType = deviceType;
    }

    public List<Sensor> getSensorList() {
        return this.sensorList;
    }

    public void setSensorList(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }
}
