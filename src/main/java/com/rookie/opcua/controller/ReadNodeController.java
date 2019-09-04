package com.rookie.opcua.controller;

import com.rookie.opcua.dto.ResponseDTO;
import com.rookie.opcua.service.ReadDeviceService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yugo
 */
@RestController
@RequestMapping("/read")
@AllArgsConstructor
public class ReadNodeController {

    private final ReadDeviceService readDeviceService;

    @GetMapping("/init")
    public void init() {
        readDeviceService.initNode();
    }

    @GetMapping("/node")
    public ResponseDTO getNode(String groupName) {
        return readDeviceService.getNodeStructure(groupName);
    }
}
