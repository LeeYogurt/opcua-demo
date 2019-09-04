package com.rookie.opcua.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yugo
 */
@Data
public class ServerStatusDTO implements Serializable {

    private String serverArray;

    private Date startTime;

    private Date currentTime;

    private String state;


}
