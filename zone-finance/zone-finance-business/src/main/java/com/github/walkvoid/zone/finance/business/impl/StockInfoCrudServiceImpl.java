package com.github.walkvoid.zone.finance.business.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.walkvoid.wvframework.utils.BeanCopyUtils;
import com.github.walkvoid.zone.finance.api.service.StockInfoCrudService;
import com.github.walkvoid.zone.finance.business.db.dao.StockInfoDAO;
import com.github.walkvoid.zone.finance.model.dto.StockInfoDTO;
import com.github.walkvoid.zone.finance.model.dto.StockInfoQueryDTO;
import com.github.walkvoid.zone.finance.model.entity.StockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockInfoCrudServiceImpl implements StockInfoCrudService {

    @Autowired
    private StockInfoDAO stockInfoDAO;

    @Override
    public Map<String, Object> listPage(StockInfoQueryDTO query) {
        QueryWrapper<StockInfo> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(query.getStockCode())) {
            wrapper.like("stock_code", query.getStockCode());
        }
        if (StringUtils.hasText(query.getStockName())) {
            wrapper.like("stock_name", query.getStockName());
        }
        if (StringUtils.hasText(query.getMarket())) {
            wrapper.eq("market", query.getMarket());
        }
        if (query.getStatus() != null) {
            wrapper.eq("status", query.getStatus());
        }
        wrapper.orderByAsc("stock_code");

        int page = query.getPage() != null ? query.getPage() : 1;
        int size = query.getSize() != null ? query.getSize() : 10;
        IPage<StockInfo> iPage = stockInfoDAO.selectPage(new Page<>(page, size), wrapper);

        List<StockInfoDTO> dtoList = iPage.getRecords().stream()
                .map(this::toDTO)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("records", dtoList);
        result.put("total", iPage.getTotal());
        result.put("page", iPage.getCurrent());
        result.put("size", iPage.getSize());
        return result;
    }

    @Override
    public StockInfoDTO getById(Long id) {
        StockInfo entity = stockInfoDAO.selectById(id);
        return entity != null ? toDTO(entity) : null;
    }

    @Override
    public StockInfoDTO getByCode(String stockCode) {
        StockInfo entity = stockInfoDAO.selectByCode(stockCode);
        return entity != null ? toDTO(entity) : null;
    }

    @Override
    public void create(StockInfoDTO dto) {
        StockInfo entity = BeanCopyUtils.copyBean(dto, StockInfo.class);
        if (!StringUtils.hasText(entity.getMarket())) {
            entity.setMarket(deriveMarket(entity.getStockCode()));
        }
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        stockInfoDAO.insert(entity);
    }

    private String deriveMarket(String stockCode) {
        if (stockCode == null || stockCode.length() < 3) {
            return "SZ";
        }
        String prefix = stockCode.substring(0, 3);
        return switch (prefix) {
            case "600", "601", "603", "605", "688" -> "SH";
            default -> "SZ";
        };
    }

    @Override
    public void update(Long id, StockInfoDTO dto) {
        StockInfo entity = stockInfoDAO.selectById(id);
        if (entity == null) {
            throw new RuntimeException("股票不存在: " + id);
        }
        StockInfo updated = BeanCopyUtils.copyBean(dto, StockInfo.class);
        updated.setId(id);
        updated.setCreateId(entity.getCreateId());
        updated.setCreateTime(entity.getCreateTime());
        updated.setUpdateTime(LocalDateTime.now());
        stockInfoDAO.updateById(updated);
    }

    @Override
    public void delete(Long id) {
        stockInfoDAO.deleteById(id);
    }

    private StockInfoDTO toDTO(StockInfo entity) {
        return BeanCopyUtils.copyBean(entity, StockInfoDTO.class);
    }
}
