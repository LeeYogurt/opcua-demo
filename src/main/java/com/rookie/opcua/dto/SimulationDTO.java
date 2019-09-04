package com.rookie.opcua.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yugo
 */
@Data
public class SimulationDTO implements Serializable {

    private String counter;

    private String expression;

    private String random;

    private String sawtooth;

    private String sinusoid;

    private String square;

    private String triangle;

}
