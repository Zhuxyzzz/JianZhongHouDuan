package com.example.jianzhonghouduan.controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "饮食计划管理")
@RestController
@RequestMapping("/planFood")
public class PlanFoodController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ApiOperation("获取用户的饮食计划列表")
    @GetMapping("/list")
    public List<Map<String, Object>> getPlanFoodList() {
        // 编写SQL查询语句
//        String sql = "SELECT * FROM food_logs WHERE user_id = ?";
        String sql="SELECT " +
                "food_logs.sum, " +
                "food_logs.people_id," +
                "food_logs.food_id," +
                "food_logs.date," +
                "food_logs.meal_type," +
                "foods.food_name," +
                "foods.calories," +
                "foods.protein," +
                "foods.fat" +
                " FROM food_logs" +
                " JOIN foods ON food_logs.food_id = foods.food_id" +
                " WHERE food_logs.people_id = ?";
        System.out.print(sql);
        // 获取当前用户ID (这里假设为1,实际项目中需要从登录用户信息中获取)
        int memberId = 1;
        
        // 执行查询并返回结果
        return jdbcTemplate.queryForList(sql, memberId);
    }

    @ApiOperation("添加用户的饮食记录（修复 400 错误版）")
    @PostMapping("/add")
    public Map<String, Object> addFoodLog(
            @RequestBody Map<String, Object> params // 改使用 Map 接收显式 JSON 参数
    ) {
        // 打印完整请求参数
        System.out.println("Received params: " + params.toString());
        try {
            // 参数提取与校验
//            int foodId = Integer.parseInt(params.get("food_id").toString());
//            int foodId;
            // 参数拆分获取（原始方式）
            Object foodIdObj = params.get("food_id");
            Object sumObj = params.get("sum");
            Object mealTypeObj = params.get("meal_type");

            //--- 参数基础验证 ---------
            // 必填字段检查
            if (foodIdObj == null || sumObj == null || mealTypeObj == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "缺乏必要参数");
//                return Map.of("code", 400, "message", "缺乏必要参数");
                return response;
            }
//            int foodId = Integer.parseInt(params.get("food_id").toString());
//            int sum = Integer.parseInt(params.get("sum").toString());
//            int mealType = Integer.parseInt(params.get("meal_type").toString());
            if (mealTypeObj instanceof Map) {
                // 处理前端传递的 {value:4, label:"加餐"} 结构
                Map<?,?> mealTypeMap = (Map<?,?>) mealTypeObj;
                params.put("meal_type", mealTypeMap.get("label")); // 替换为实际值
            }
            int foodId = parseToInt(foodIdObj, "food_id");
            int sum = parseToInt(sumObj, "sum");
            String mealType = String.valueOf(params.get("meal_type").toString().charAt(0));
            String dateStr = params.get("date").toString();
            System.out.println("原始日期参数：" + dateStr);
            LocalDate date;
            try {
                // 方式 1：处理 ISO 带时区的时间戳（如 "2025-04-08T09:58:23.385Z"）
                Instant instant = Instant.parse(dateStr);
                // 使用时区明确转换为 UTC 日期（或根据业务需求转换为其他时区）
                date = instant.atZone(ZoneOffset.UTC).toLocalDate();
            } catch (DateTimeParseException e) {
                // 方式 2：处理纯日期格式（如 "2025-04-08"）
                date = LocalDate.parse(dateStr);
            }

            // 验证转换结果
            System.out.println("转换后日期：" + date.toString());
            // 强制参数验证
            if (sum <= 0 ) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "参数越界");
                return response;
//                return Map.of("code", 400, "message", "参数越界");
            }

            // 日期格式验证
//            java.sql.Date date;
//            try {
//                date = java.sql.Date.valueOf(LocalDate.parse(dateStr));
//            } catch (DateTimeParseException e) {
//                return Map.of("code", 400, "message", "日期格式必须为 yyyy-MM-dd");
//            }

            // 安全 SQL 构建（预编译）
            String sql = "INSERT INTO food_logs(people_id, food_id, sum, date, meal_type) VALUES (?, ?, ?, ?, ?)";
            Object[] args = {
                    1, // 实际应替换为真实用户 ID 获取方式
                    foodId,
                    sum,
                    date,
                    mealType
            };

            System.out.printf("Safe SQL: %s\nParams: %s\n", sql, Arrays.toString(args));

            int result = jdbcTemplate.update(sql, args);

            // 结果处理（与原逻辑保持风格统一）
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "记录添加成功");
                response.put("data",getLatestRecord());
                return response;


//                return Map.of(
//                        "code", 200,
//                        "message", "记录添加成功",
//                        "data", getLatestRecord()
//                );
            }
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "数据库操作失败");
            return response;
//            return Map.of("code", 500, "message", "数据库操作失败");

        } catch (NullPointerException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "必要参数缺失");
            return response;
//            return Map.of("code", 400, "message", "必要参数缺失");
        } catch (NumberFormatException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "数字类型参数格式错误");
            return response;
//            return Map.of("code", 400, "message", "数字类型参数格式错误");
        } catch (DataIntegrityViolationException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "食物不存在或外键约束失败");
            return response;
//            return Map.of("code", 400, "message", "食物不存在或外键约束失败");
        }
    }



    // 专用的数值转换方法
    private int parseToInt(Object value, String paramName) {
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(paramName + "必须是整数");
        }
    }
    // 保持与/list 相同的数据格式
    private Map<String, Object> getLatestRecord() {
        String query = "SELECT food_logs.*, foods.food_name, foods.calories " +
                "FROM food_logs " +
                "JOIN foods ON food_logs.food_id = foods.food_id " +
                "WHERE people_id = 1 ORDER BY fl_id DESC LIMIT 1";

        return jdbcTemplate.queryForList(query).get(0);
    }

}

