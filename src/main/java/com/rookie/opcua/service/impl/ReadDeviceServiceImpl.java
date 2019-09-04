package com.rookie.opcua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rookie.opcua.client.ClientGen;
import com.rookie.opcua.dto.NodeTreeDTO;
import com.rookie.opcua.dto.ResponseDTO;
import com.rookie.opcua.entity.NodeStructure;
import com.rookie.opcua.mapper.NodeStructureMapper;
import com.rookie.opcua.service.ReadDeviceService;
import com.rookie.opcua.utils.BeanConvertUtils;
import com.rookie.opcua.utils.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.nodes.Node;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author yugo
 */
@Service
@AllArgsConstructor
@Slf4j
public class ReadDeviceServiceImpl implements ReadDeviceService {

    private final NodeStructureMapper nodeStructureMapper;

    /**
     * 初始化节点结构
     */
    @Override
    public void initNode() {
        try {
            log.info("初始化节点结构");
            OpcUaClient opcUaClient = ClientGen.opcUaClient;
            opcUaClient.connect().get();
            List<Node> objectNodeList = opcUaClient.getAddressSpace().browse(Identifiers.ObjectsFolder).get();
            List<NodeStructure> nodeStructureList = new ArrayList<>(16);
            for (Node objectNode : objectNodeList) {
                if (!"Server".equals(objectNode.getBrowseName().get().getName())) {
                    build(nodeStructureList, objectNode, String.valueOf(Identifiers.ObjectsFolder.getIdentifier()), 0);
                    browseNode(nodeStructureList, objectNode.getNodeId().get().getIdentifier().toString(), opcUaClient, objectNode.getNodeId().get(), 0);
                }
            }
            nodeStructureMapper.batchSave(nodeStructureList);
        } catch (Exception e) {
            log.error("初始化节点结构失败 {}", e.getMessage());
        }
    }

    /**
     * 构建节点结构实体
     *
     * @param nodeStructureList 节点结合
     * @param node              节点
     * @param parentIdentifier  父节点
     * @param level             层级
     */
    private void build(List<NodeStructure> nodeStructureList, Node node, String parentIdentifier, Integer level) {
        try {
            NodeStructure nodeStructure = new NodeStructure();
            nodeStructure.setId(IdGenerator.getInstance().nextId());
            nodeStructure.setParentIdentifier(parentIdentifier);
            nodeStructure.setBrowseName(node.getBrowseName().get().getName());
            nodeStructure.setDisplayName(node.getDisplayName().get().getText());
            nodeStructure.setNamespaceIndex(node.getNodeId().get().getNamespaceIndex().intValue());
            nodeStructure.setIdentifier(node.getNodeId().get().getIdentifier().toString());
            nodeStructure.setNodeClass(node.getNodeClass().get());
            nodeStructure.setLevel(level);
            nodeStructureList.add(nodeStructure);
        } catch (Exception e) {
            log.error("构建节点结构异常{}", e.getMessage());
        }
    }

    /**
     * 递归浏览节点
     *
     * @param nodeStructureList 节点结构
     * @param identifier        标识符
     * @param client            客户端
     * @param browseRoot        浏览根节点
     * @return List<NodeStructure> 节点结构树
     */
    private void browseNode(List<NodeStructure> nodeStructureList, String identifier, OpcUaClient client, NodeId browseRoot, Integer level) {
        try {
            List<Node> nodes = client.getAddressSpace().browse(browseRoot).get();
            ++level;
            for (Node node : nodes) {
                build(nodeStructureList, node, identifier, level);
                log.info("parentIdentifier={} Node={} level={}", identifier, node.getBrowseName().get().getName(), level);
                browseNode(nodeStructureList, node.getNodeId().get().getIdentifier().toString(), client, node.getNodeId().get(), level);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
        }
    }

    /**
     * 获取节点树结构
     *
     * @param groupName 设备组名称
     * @return ResponseDTO<List < NodeTreeDTO>>
     */
    @Override
    public ResponseDTO getNodeStructure(String groupName) {
        List<NodeTreeDTO> nodeStructureDTOList = BeanConvertUtils.tToV(nodeStructureMapper.selectAll(), NodeTreeDTO.class);
        QueryWrapper<NodeStructure> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("level", "0");
        if (StringUtils.isNotBlank(groupName)) {
            queryWrapper.eq("browse_name", groupName);
        }
        Map<String, NodeTreeDTO> nodeMap = nodeStructureDTOList.stream().collect(Collectors.toMap(NodeTreeDTO::getIdentifier, nodeTree -> nodeTree));
        getVariableNodeValue(nodeMap);
        List<NodeTreeDTO> rootNodeList = BeanConvertUtils.tToV(nodeStructureMapper.selectList(queryWrapper), NodeTreeDTO.class);
        if (rootNodeList == null) {
            return ResponseDTO.failed();
        }
        List<NodeTreeDTO> rootNodeTreeDTOList = new ArrayList<>();
        for (NodeTreeDTO currentNode : rootNodeList) {
            rootNodeTreeDTOList.add(getTree(nodeMap, currentNode));
        }
        return ResponseDTO.ok(rootNodeTreeDTOList);
    }

    /**
     * 递归获取树结构
     *
     * @param nodeMap     节点map
     * @param currentNode 当前节点
     */
    private NodeTreeDTO getTree(Map<String, NodeTreeDTO> nodeMap, NodeTreeDTO currentNode) {
        try {
            List<NodeTreeDTO> nodeTreeDTOList = new ArrayList<>();
            NodeTreeDTO nodeTreeDTO = new NodeTreeDTO();
            List<NodeTreeDTO> childNodeList = getChildNodeList(currentNode.getIdentifier(), nodeMap);
            nodeTreeDTO.setIdentifier(currentNode.getIdentifier());
            nodeTreeDTO.setNamespaceIndex(currentNode.getNamespaceIndex());
            nodeTreeDTO.setBrowseName(currentNode.getBrowseName());
            nodeTreeDTO.setLevel(currentNode.getLevel());
            nodeTreeDTO.setNodeClass(currentNode.getNodeClass());
            nodeTreeDTO.setParentIdentifier(currentNode.getParentIdentifier());
            nodeTreeDTO.setDisplayName(currentNode.getDisplayName());
            nodeTreeDTO.setValue(currentNode.getValue());
            for (NodeTreeDTO childNodeTree : childNodeList) {
                nodeTreeDTOList.add(getTree(nodeMap, childNodeTree));
            }
            nodeTreeDTO.setSubNodeList(nodeTreeDTOList);
            return nodeTreeDTO;
        } catch (Exception e) {
            log.error("递归节点{} 失败 异常信息:{}", currentNode.getIdentifier(), e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前节点下的子节点
     *
     * @param currentIdentifier 标识符
     * @param nodeStructureMap  节点结构集合
     * @return List<NodeStructure>
     */
    private List<NodeTreeDTO> getChildNodeList(String currentIdentifier, Map<String, NodeTreeDTO> nodeStructureMap) {
        List<NodeTreeDTO> list = new ArrayList<>(16);
        for (String key : nodeStructureMap.keySet()) {
            if (nodeStructureMap.get(key).getParentIdentifier().equals(currentIdentifier)) {
                list.add(nodeStructureMap.get(key));
            }
        }
        return list;
    }

    /**
     * 获取属性节点的属性值
     *
     * @param nodeTreeMap 节点map
     */
    private void getVariableNodeValue(Map<String, NodeTreeDTO> nodeTreeMap) {
        try {
            log.info("=======================================");
            long startTime = System.currentTimeMillis();
            QueryWrapper<NodeStructure> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("node_class", NodeClass.Variable);
            //查询属性节点集合
            List<NodeStructure> variableNodeList = nodeStructureMapper.selectList(queryWrapper);
            List<NodeTreeDTO> variableNodeTreeDTOList = BeanConvertUtils.tToV(variableNodeList, NodeTreeDTO.class);
            if (variableNodeTreeDTOList == null) {
                return;
            }
            OpcUaClient client = ClientGen.opcUaClient;
            client.connect().get();
            //确保顺序同属性节点集合相同
            List<NodeId> nodeIdList = variableNodeTreeDTOList.stream().map(node -> new NodeId(node.getNamespaceIndex(), Unsigned.uint(node.getIdentifier()))).collect(Collectors.toList());
            //方式1 通过nodeIdList 批量获取属性值
            List<DataValue> dataValueList = client.readValues(0, TimestampsToReturn.Both, nodeIdList).get();
            for (int i = 0; i < variableNodeTreeDTOList.size(); i++) {
                try {
                    variableNodeTreeDTOList.get(i).setValue(dataValueList.get(i).getValue().getValue() == null ? null : dataValueList.get(i).getValue().getValue().toString());
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            //给树节点赋值
            for (NodeTreeDTO nodeTreeDTO : variableNodeTreeDTOList) {
                NodeTreeDTO nodeTree = nodeTreeMap.get(nodeTreeDTO.getIdentifier());
                nodeTree.setValue(nodeTreeDTO.getValue());
            }

            long endTime = System.currentTimeMillis();
            float excTime = (float) (endTime - startTime) / 1000;
            log.info("===============耗时：" + excTime + "秒==============");
        } catch (Exception e) {
            log.error("或许节点属性值异常:{}", e.getMessage());
        }

    }
}
