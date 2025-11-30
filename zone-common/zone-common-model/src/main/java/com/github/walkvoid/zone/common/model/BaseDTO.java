package com.github.walkvoid.zone.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author walkvoid
 * @version 1.0
 * @date 2025/11/30
 * @desc todo
 */
public class BaseDTO implements Serializable {

    /**
     * 创建者id
     */
    private Long createId;

    /**
     * 创建者id
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新id
     */
    private String updateName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
