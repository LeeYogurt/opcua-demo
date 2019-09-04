package com.rookie.opcua.task;

import com.rookie.opcua.client.ClientGen;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

/**
 * @author yugo
 */
@Component
@Slf4j
public class OpcUaSubscriptionTask {

    private final AtomicLong clientHandles = new AtomicLong(1L);

    @Scheduled(cron = "0/30 * * * * ?")
    public void subscription() {
        try {
            log.info("开始监听");
            OpcUaClient client = ClientGen.opcUaClient;
            //创建发布间隔1000ms的订阅对象
            UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

            // 监听服务当前时间节点
            ReadValueId readValueId = new ReadValueId(
                    Identifiers.Server_ServerStatus_CurrentTime,
                    AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

            // 每个项目的客户端句柄必须是唯一的
            UInteger clientHandle = uint(clientHandles.getAndIncrement());

            MonitoringParameters parameters = new MonitoringParameters(
                    clientHandle,
                    // 间隔
                    1000.0,
                    // 过滤器空表示使用默认值
                    null,
                    // 队列大小
                    uint(10),
                    // 是否丢弃旧配置
                    true
            );

            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                    readValueId, MonitoringMode.Reporting, parameters);

            // 创建监控项，并且注册变量值改变时候的回调函数。
            BiConsumer<UaMonitoredItem, Integer> onItemCreated =
                    (item, id) -> item.setValueConsumer(this::onSubscriptionValue);

            List<UaMonitoredItem> items = subscription.createMonitoredItems(
                    TimestampsToReturn.Both,
                    newArrayList(request),
                    onItemCreated
            ).get();

            for (UaMonitoredItem item : items) {
                if (item.getStatusCode().isGood()) {
                    log.info("item created for nodeId={}", item.getReadValueId().getNodeId());
                } else {
                    log.warn(
                            "failed to create item for nodeId={} (status={})",
                            item.getReadValueId().getNodeId(), item.getStatusCode());
                }
            }

            // 运行五秒然后停止
            Thread.sleep(5000);
        } catch (Exception e) {
            log.error("订阅失败" + e.getMessage());
        }
    }

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        log.info(
                "subscription value received: item={}, value={}",
                item.getReadValueId().getNodeId(), value.getValue());
    }
}
