package com.github.walkvoid.zone.system.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形菜单节点 VO
 * 适配前端 Vue Vben Admin RouteRecordStringComponent 结构
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
public class MenuTreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 路由名称（必填，前端 require name） */
    private String name;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 重定向 */
    private String redirect;

    /** 元信息 */
    private MenuMeta meta;

    /** 子节点 */
    private List<MenuTreeNode> children;

    /**
     * 添加子节点
     */
    public void addChild(MenuTreeNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    @Data
    public static class MenuMeta implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 标题 */
        private String title;

        /** 图标 */
        private String icon;

        /** 排序 */
        private Integer order;

        /** 是否在菜单中隐藏 */
        private Boolean hideInMenu;

        /** 权限标识 */
        private List<String> authority;

        /** 是否固定标签页 */
        private Boolean affixTab;

        /** 是否保持缓存 */
        private Boolean keepAlive;

        /** 外链地址 */
        private String link;

        /** iframe 地址 */
        private String iframeSrc;
    }
}
