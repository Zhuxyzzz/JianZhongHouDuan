package com.example.jianzhonghouduan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Api(tags = "获取饮食建议")
@RestController
@RequestMapping("/getAdvice")

public class GetAdvice {

    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ApiOperation("获取建议列表")
    @GetMapping("/list")
    public ResponseEntity<?> GetAdvice(
            @ApiParam(value = "建议时间", required = true)
            @RequestParam String date) {
//        org.apache.logging.log4j.Logger log = null;
        logger.info("收到请求，日期 ID={}，类型={}", date, date.getClass());
        try {
            //▼ 精确匹配查询（添加 LIMIT 1 保证唯一性）
//            String sql = "SELECT * FROM doctor_advice WHERE convert(varchar(50),doctor_advice.date,120) = ?";
            String sql ="SELECT * FROM doctor_advice WHERE CAST(doctor_advice.date AS DATE) = CAST(? AS DATE) ";
                    logger.info("Executing SQL: {} with param: {}", sql, date);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, date);

            if (results.isEmpty()) {
                //▼ 返回结构化错误信息
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "date不存在: " + date));
            } else {
                //▼ 提取并格式化结果
                System.out.println("results:" + results);
                List<Map<String, Object>> results1 = new ArrayList<>();
                for (Map<String, Object> advice : results) {
                    Map<String, Object> initializedAdvice = new HashMap<>();
                    initializedAdvice.put("advice_id", advice.get("advice_id"));
                    initializedAdvice.put("people_id", advice.get("people_id"));
                    initializedAdvice.put("user_id", advice.get("user_id"));
                    initializedAdvice.put("advice_type", advice.get("advice_type"));
                    initializedAdvice.put("advice_content", advice.get("advice_content"));
                    initializedAdvice.put("date", advice.get("date"));

                    // 将初始化后的建议存储到 results1 中
                    results1.add(initializedAdvice);
                }
                System.out.println("results1:" + results1);
                Map<String, Object> advicelist = results.get(0);
                System.out.println("advicelist:" + advicelist);
                Map<String, Object> result = new HashMap<>();

                result.put("code", 200);
                result.put("message", "success");
//                result.put("data", Map.of(
//                        "advice_id", advicelist.get("advice_id") != null ? advicelist.get("advice_id") : "",
//                        "people_id", advicelist.get("people_id") != null ? advicelist.get("people_id") : "",
//                        "advice_type", advicelist.get("advice_type") != null ? advicelist.get("advice_type") : "",
//                        "advice_content", advicelist.get("advice_content") != null ? advicelist.get("advice_content") : 0,
//                        "date", advicelist.get("date") != null ? advicelist.get("date") : 0
//                ));
//                result.put("data", List.of(results));
//                return ResponseEntity.ok(result);

                // 使用 HashMap 构建 data 部分
                Map<String, Object> data = new HashMap<>();
                data.put("advice_id", advicelist.get("advice_id") != null ? advicelist.get("advice_id") : "");
                data.put("people_id", advicelist.get("people_id") != null ? advicelist.get("people_id") : "");
                data.put("advice_type", advicelist.get("advice_type") != null ? advicelist.get("advice_type") : "");
                data.put("advice_content", advicelist.get("advice_content") != null ? advicelist.get("advice_content") : 0);
                data.put("date", advicelist.get("date") != null ? advicelist.get("date") : 0);

                // 将构建的数据放入结果中
//                result.put("data", data);
                result.put("data", results1);
                return ResponseEntity.ok(result);



            }

        } catch (EmptyResultDataAccessException e) {
            //▼ 捕捉无结果异常
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "未找到指定食物"));
        } catch (DataAccessException e) {
            //▼ 记录详细数据库错误日志
            logger.error("数据库访问异常: ", e);
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "数据库查询异常"));
        }


    }
}

