package com.rookie.opcua.websocket.encoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.util.List;

/**
 * @author yugo
 */
public class EncoderClassDTO implements Encoder.Text<List<Object>> {

    @Override
    public void init(EndpointConfig config) {
        // TODO Auto-generated method stub

    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public String encode(List<Object> list) throws EncodeException {
        ObjectMapper mapMapper = new ObjectMapper();
        try {
            String json = "";
            json = mapMapper.writeValueAsString(list);
            return json;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "false";
        }
    }

}