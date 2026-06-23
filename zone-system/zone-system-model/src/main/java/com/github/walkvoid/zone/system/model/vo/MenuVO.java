package com.github.walkvoid.zone.system.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 菜单 VO（匹配前端菜单格式）
 *
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 */
@Data
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String type;
    private Long pid;
    private String path;
    private String component;
    private String redirect;
    private String authCode;
    private Integer status;
    private MenuMeta meta;
    private List<MenuVO> children;

    @Data
    public static class MenuMeta implements Serializable {
        private static final long serialVersionUID = 1L;
        private String icon;
        private String title;
        private Integer order;
        private String activeIcon;
        private String activePath;
        private Boolean affixTab;
        private Integer affixTabOrder;
        private String badge;
        private String badgeType;
        private String badgeVariants;
        private Boolean hideChildrenInMenu;
        private Boolean hideInBreadcrumb;
        private Boolean hideInMenu;
        private Boolean hideInTab;
        private String iframeSrc;
        private Boolean keepAlive;
        private String link;
        private Integer maxNumOfOpenTab;
        private Map<String, Object> query;
    }
}
