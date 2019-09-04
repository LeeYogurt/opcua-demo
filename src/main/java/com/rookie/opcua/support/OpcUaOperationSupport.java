package com.rookie.opcua.support;


import com.rookie.opcua.client.ClientGen;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.nodes.Node;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.l;

/**
 * @author yugo
 */
@Component
@Slf4j
public class OpcUaOperationSupport {


    /**
     * 写入节点数据
     */
    public void writeNodeValue() throws Exception {
        try {
            log.info("准备写入");
            OpcUaClient client = ClientGen.opcUaClient;
            //创建连接
            client.connect().get();

            //创建变量节点
            NodeId nodeId = new NodeId(5, "Counter1");

            //创建Variant对象和DataValue对象
            Variant v = new Variant(90);
            DataValue dataValue = new DataValue(v, null, null);

            StatusCode statusCode = client.writeValue(nodeId, dataValue).get();

            System.out.println(statusCode.isGood());

        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 浏览节点
     */
    public void browseNode() {
        try {
            OpcUaClient client = ClientGen.opcUaClient;

            client.connect().get();

            List<Node> nodes = client.getAddressSpace().browse(Identifiers.ObjectsFolder).get();

            for (Node node : nodes) {
                System.out.println("Node= " + node.getBrowseName().get().getName());
            }
        } catch (Exception e) {
            log.error("Node browseNode error:" + e.getMessage());
        }

    }

    /**
     * 订阅变量
     */
    public void createSubscription() {
        try {
            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();

            //创建发布间隔1000ms的订阅对象
            UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

            //创建监控的参数
            MonitoringParameters parameters = new MonitoringParameters(
                    uint(1),
                    // 发布间隔
                    1000.0,
                    // filter, 空表示用默认值
                    null,
                    // 队列大小
                    uint(10),
                    //放弃旧配置
                    true
            );


            //创建订阅的变量
//            NodeId nodeId = new NodeId(5, "Counter1");
            NodeId nodeId = new NodeId(0, 1555944286);
            ReadValueId readValueId = new ReadValueId(nodeId, AttributeId.Value.uid(), null, null);

            //创建监控项请求
            //该请求最后用于创建订阅。
            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

            List<MonitoredItemCreateRequest> requests = new ArrayList<>();
            requests.add(request);

            //创建监控项，并且注册变量值改变时候的回调函数。
            BiConsumer<UaMonitoredItem, Integer> onItemCreated =
                    (item, id) -> {
                        item.setValueConsumer((item1, value) -> {
                            log.info("收到的订阅值: item={}, value={}", item1.getReadValueId().getNodeId(), value.getValue());
                        });
                    };

            List<UaMonitoredItem> items = subscription.createMonitoredItems(
                    TimestampsToReturn.Both,
                    newArrayList(request),
                    onItemCreated
            ).get();

            for (UaMonitoredItem item : items) {
                if (item.getStatusCode().isGood()) {
                    log.info("创建项目 nodeId={}", item.getReadValueId().getNodeId());
                } else {
                    log.warn("创建失败 nodeId={} (status={})", item.getReadValueId().getNodeId(), item.getStatusCode());
                }
            }
        } catch (Exception e) {
            log.error("订阅变量失败");
        }
    }

    /**
     * 查看变量历史记录
     *
     * @return List
     */
    public List<DataValue> historyRead() {
        try {
            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();

            HistoryReadDetails historyReadDetails = new ReadRawModifiedDetails(
                    false,
                    DateTime.MIN_VALUE,
                    DateTime.now(),
                    uint(0),
                    true
            );

            HistoryReadValueId historyReadValueId = new HistoryReadValueId(
                    new NodeId(5, "Counter1"),
                    null,
                    QualifiedName.NULL_VALUE,
                    ByteString.NULL_VALUE
            );

            List<HistoryReadValueId> nodesToRead = new ArrayList<>();
            nodesToRead.add(historyReadValueId);

            HistoryReadResponse historyReadResponse = client.historyRead(
                    historyReadDetails,
                    TimestampsToReturn.Both,
                    false,
                    nodesToRead
            ).get();


            HistoryReadResult[] historyReadResults = historyReadResponse.getResults();

            List<DataValue> dataValues = null;

            if (historyReadResults != null) {
                HistoryReadResult historyReadResult = historyReadResults[0];
                HistoryData historyData = (HistoryData) historyReadResult.getHistoryData().decode(
                        client.getSerializationContext()
                );
                dataValues = l(historyData.getDataValues());
            }
            return dataValues;
        } catch (Exception e) {
            log.error("查看变量历史记录失败" + e.getMessage());
            return null;
        }

    }
}
