package com.rookie.opcua.dto;

import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

import java.io.Serializable;
import java.util.List;

/**
 * @author yugo
 */
@Data
public class NodeTreeDTO implements Serializable {
    /**
     * 命名空间
     */
    private Integer namespaceIndex;
    /**
     * 标识符
     */
    private String identifier;
    /**
     * 节点类型
     */
    private NodeClass nodeClass;
    /**
     * 浏览名称
     */
    private String browseName;
    /**
     * 显示名称
     */
    private String displayName;
    /**
     * 父级标识符
     */
    private String parentIdentifier;
    /**
     * 节点值
     */
    private String value;
    /**
     * 层级
     */
    private Integer level;
    /**
     * 子节点
     */
    List<NodeTreeDTO> subNodeList;
}
