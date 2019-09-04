package com.rookie.opcua.task;


import com.rookie.opcua.client.ClientGen;
import com.rookie.opcua.dto.ServerStatusDTO;
import com.rookie.opcua.dto.SimulationDTO;
import com.rookie.opcua.utils.JsonUtils;
import com.rookie.opcua.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.ServerStatusNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * @author yugo
 */
@Component
@Slf4j
public class OpcUaReadNodeTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Async
    @Scheduled(cron = "0/30 * * * * ?")
    public void setReadNodeSupport() {
        try {
            log.info("获取节点状态信息");
            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();

            // 获取对服务器对象的类型化引用 ：服务节点
            ServerNode serverNode = client.getAddressSpace().getObjectNode(
                    Identifiers.Server,
                    ServerNode.class
            ).get();

            // 读取服务器对象的属性
            String[] serverArray = serverNode.getServerArray().get();

            ServerStatusNode serverStatusNode = serverNode.getServerStatusNode().get();
            DateTime startTime = serverStatusNode.getStartTime().get();
            DateTime currentTime = serverStatusNode.getCurrentTime().get();
            ServerState state = serverStatusNode.getState().get();

            ServerStatusDTO serverStatusDTO = new ServerStatusDTO();
            serverStatusDTO.setState(state.name());
            serverStatusDTO.setStartTime(startTime.getJavaDate());
            serverStatusDTO.setCurrentTime(currentTime.getJavaDate());
            serverStatusDTO.setServerArray(serverArray[0]);

            redisTemplate.opsForValue().set("SERVER_NODE", JsonUtils.beanToJson(serverStatusDTO));
        } catch (Exception e) {
            log.error("获取服务节点失败,错误信息：{}",e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void setSimulation() {
        try {
            log.info("获取 Simulation 模拟信息模型数据");
            List<SimulationDTO> simulationList=null;

            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();

            NodeId counter1 = new NodeId(5, "Counter1");
            NodeId expression1 = new NodeId(5, "Expression1");
            NodeId random1 = new NodeId(5, "Random1");
            NodeId sawtooth1 = new NodeId(5, "Sawtooth1");
            NodeId sinusoid1 = new NodeId(5, "Sinusoid1");
            NodeId square1 = new NodeId(5, "Square1");
            NodeId triangle1 = new NodeId(5, "Triangle1");

            DataValue counter1value = client.readValue(0.0, TimestampsToReturn.Both, counter1).get();
            DataValue expression1value = client.readValue(0.0, TimestampsToReturn.Both, expression1).get();
            DataValue random1value = client.readValue(0.0, TimestampsToReturn.Both, random1).get();
            DataValue sawtooth1value = client.readValue(0.0, TimestampsToReturn.Both, sawtooth1).get();
            DataValue sinusoid1value = client.readValue(0.0, TimestampsToReturn.Both, sinusoid1).get();
            DataValue square1value = client.readValue(0.0, TimestampsToReturn.Both, square1).get();
            DataValue triangle1value = client.readValue(0.0, TimestampsToReturn.Both, triangle1).get();

            SimulationDTO simulationDTO = new SimulationDTO();
            simulationDTO.setCounter(counter1value.getValue().getValue().toString());
            simulationDTO.setExpression(expression1value.getValue().getValue().toString());
            simulationDTO.setRandom(random1value.getValue().getValue().toString());
            simulationDTO.setSawtooth(sawtooth1value.getValue().getValue().toString());
            simulationDTO.setSinusoid(sinusoid1value.getValue().getValue().toString());
            simulationDTO.setSquare(square1value.getValue().getValue().toString());
            simulationDTO.setTriangle(triangle1value.getValue().getValue().toString());


            Object simulationRecord = redisTemplate.opsForValue().get("SIMULATION");
            if (simulationRecord!=null){
                simulationList = JsonUtils.jsonToList(simulationRecord.toString(), SimulationDTO.class);
            }else {
                simulationList = new ArrayList<>();
            }
            simulationList.add(simulationDTO);

            redisTemplate.opsForValue().set("SIMULATION", JsonUtils.listToJson(simulationList));

            WebSocketServer.sendInfo(JsonUtils.listToJson(simulationList));
        } catch (Exception e) {
            log.error("获取模拟服务节点失败，错误信息{}",e.getMessage());
            e.printStackTrace();
        }
    }


}
