package com.rookie.opcua.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;

/**
 * entity
 *
 * @author yugo
 * 2019-07-03
 */
@Data
@ToString(callSuper = true)
@TableName("node_structure")
public class NodeStructure {

    /**
     * id
     */
    @TableField("id")
    private Long id;

    /**
     * 命名空间下标
     */
    @TableField("namespace_index")
    private Integer namespaceIndex;

    /**
     * 标识符
     */
    @TableField("identifier")
    private String identifier;
    /**
     * 节点类型
     */
    @TableField("node_class")
    private NodeClass nodeClass;

    /**
     * 浏览名称
     */
    @TableField("browse_name")
    private String browseName;

    /**
     * 显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 父级标识符
     */
    @TableField("parent_identifier")
    private String parentIdentifier;

    /**
     * 父级标识符
     */
    @TableField("level")
    private Integer level;
}
