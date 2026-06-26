package com.github.walkvoid.zone.finance.business.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.walkvoid.zone.ai.api.PromptTemplateApi;
import com.github.walkvoid.zone.finance.api.service.StockDailyService;
import com.github.walkvoid.zone.finance.business.db.dao.StockDailyDAO;
import com.github.walkvoid.zone.finance.model.entity.StockDaily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockDailyServiceImpl implements StockDailyService {

    private static final Logger log = LoggerFactory.getLogger(StockDailyServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private StockDailyDAO stockDailyDAO;

    @Autowired
    private PromptTemplateApi promptTemplateApi;

    @Override
    public StockDaily getByCodeAndDate(String stockCode, LocalDate tradeDate) {
        return stockDailyDAO.selectByCodeAndDate(stockCode, tradeDate);
    }

    @Override
    public List<StockDaily> listByDateRange(String stockCode, LocalDate start, LocalDate end) {
        return stockDailyDAO.selectByCodeAndDateRange(stockCode, start, end);
    }

    @Override
    public StockDaily getLatest(String stockCode) {
        return stockDailyDAO.selectLatest(stockCode);
    }

    @Override
    public int batchSave(List<StockDaily> list) {
        int count = 0;
        for (StockDaily entity : list) {
            stockDailyDAO.insert(entity);
            count++;
        }
        return count;
    }

    @Override
    public List<StockDaily> fetchAndSaveDaily(String stockCode, LocalDate startDate, LocalDate endDate) {
        log.info("获取日线数据: stock={}, {}~{}", stockCode, startDate, endDate);

        // 调用 PromptTemplateApi 执行大模型查询
        Map<String, String> variables = new HashMap<>();
        variables.put("stock_code", stockCode);
        variables.put("start_date", startDate.format(DF));
        variables.put("end_date", endDate.format(DF));

        String llmResponse = promptTemplateApi.executePrompt("STOCK_DAILY_QUERY", variables);

        // 解析并保存
        List<StockDaily> result = parseResponse(llmResponse, stockCode);
        if (!result.isEmpty()) {
            batchSave(result);
            log.info("保存日线数据成功: stock={}, count={}", stockCode, result.size());
        } else {
            log.warn("大模型未返回有效日线数据: stock={}", stockCode);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<StockDaily> parseResponse(String llmResponse, String stockCode) {
        List<StockDaily> list = new ArrayList<>();
        try {
            String json = llmResponse;
            if (json.contains("```json")) {
                json = json.substring(json.indexOf("```json") + 7);
                if (json.contains("```")) json = json.substring(0, json.indexOf("```"));
            } else if (json.contains("```")) {
                json = json.substring(json.indexOf("```") + 3);
                if (json.contains("```")) json = json.substring(0, json.indexOf("```"));
            }
            json = json.trim();

            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) root.get("data");
            if (dataList == null) return list;

            for (Map<String, Object> item : dataList) {
                StockDaily entity = new StockDaily();
                entity.setStockCode(stockCode);
                entity.setTradeDate(LocalDate.parse((String) item.get("trade_date"), DF));
                entity.setOpen(toDecimal(item, "open"));
                entity.setHigh(toDecimal(item, "high"));
                entity.setLow(toDecimal(item, "low"));
                entity.setClose(toDecimal(item, "close"));
                entity.setPreClose(toDecimal(item, "pre_close"));
                entity.setVolume(toLong(item, "volume"));
                entity.setAmount(toDecimal(item, "amount"));
                entity.setChangePct(toDecimal(item, "change_pct"));
                entity.setTurnoverRate(toDecimal(item, "turnover_rate"));
                entity.setAmplitude(toDecimal(item, "amplitude"));
                list.add(entity);
            }
        } catch (Exception e) {
            log.error("解析大模型响应失败: {}", e.getMessage());
            log.error("原始响应: {}", llmResponse);
        }
        return list;
    }

    private BigDecimal toDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return new BigDecimal(val.toString());
    }

    private Long toLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        return Long.valueOf(val.toString());
    }
}
