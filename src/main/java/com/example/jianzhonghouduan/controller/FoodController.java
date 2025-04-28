package com.example.jianzhonghouduan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;

//import static org.yaml.snakeyaml.TypeDescription.log;

@Api(tags = "食物卡路里查询")
@RestController
@RequestMapping("/food")
public class FoodController {

    private static final Logger logger = LoggerFactory.getLogger(FoodController.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ApiOperation("根据关键词搜索食物")
    @GetMapping("/search")
    public ResponseEntity<?> searchFood(
            @ApiParam(value = "搜索关键词", required = true)
            @RequestParam String keyword) {
        try {
            String sql = "SELECT * FROM foods WHERE food_name LIKE ? OR details LIKE ?";
            String searchPattern = "%" + keyword + "%";
            logger.info("执行SQL查询: {}, 参数: {}", sql, searchPattern);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, searchPattern, searchPattern);
            
            if (results.isEmpty()) {
                return ResponseEntity.ok().body("未找到相关食物信息");
            }
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("搜索食物时发生错误: ", e);
            return ResponseEntity.internalServerError().body("搜索食物时发生错误: " + e.getMessage());
        }
    }

    @ApiOperation("获取推荐食物")
    @PostMapping(value = "/recommend", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> recommendFoods(@RequestBody Map<String, Object> params) {
        try {
            Double currentCalories = Double.valueOf(params.get("currentCalories").toString());
            Double targetCalories = Double.valueOf(params.get("targetCalories").toString());
            
            // 计算卡路里占比
            double caloriePercentage = (currentCalories / targetCalories) * 100;
            
            // 构建推荐查询SQL
            String sql;
            if (caloriePercentage > 90) {
                // 如果已接近目标卡路里，推荐低卡食物
                sql = "SELECT food_id, food_name, calories, image FROM foods WHERE calories < 100 ORDER BY RAND() LIMIT 5";
            } else if (caloriePercentage < 50) {
                // 如果摄入较少，推荐中等卡路里的食物
                sql = "SELECT food_id, food_name, calories, image FROM foods WHERE calories BETWEEN 100 AND 300 ORDER BY RAND() LIMIT 5";
            } else {
                // 其他情况推荐均衡的食物
                sql = "SELECT food_id, food_name, calories, image FROM foods ORDER BY RAND() LIMIT 5";
            }

            List<Map<String, Object>> recommendFoods = jdbcTemplate.queryForList(sql);
            
            // 格式化返回数据
            recommendFoods.forEach(food -> {
                food.put("calories", food.get("calories") + "");  // 确保卡路里以字符串形式返回
                if (food.get("image") == null) {
                    food.put("image", ""); // 确保图片字段不为null
                }
            });

            return ResponseEntity.ok(recommendFoods);
            
        } catch (Exception e) {
            logger.error("获取推荐食物时发生错误: ", e);
            return ResponseEntity.internalServerError().body("获取推荐食物失败: " + e.getMessage());
        }
    }

    @ApiOperation("根据食物名称查询对应 ID")
    @GetMapping("/byname")
    public ResponseEntity<?> getFoodIdByName(
            @ApiParam(value = "食物名称", required = true)
            @RequestParam String name) {

        try {
            //▼ 精确匹配查询（添加 LIMIT 1 保证唯一性）
            String sql = "SELECT food_id, food_name FROM foods WHERE food_name = ? LIMIT 1";
            logger.info("Executing SQL: {} with param: {}", sql, name);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, name);

            if (results.isEmpty()) {
                //▼ 返回结构化错误信息
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "食物不存在: " + name));
            } else {
                //▼ 提取并格式化结果
                Map<String, Object> foodData = results.get(0);
                System.out.println("foodData:"+foodData);
//                return ResponseEntity.ok(Map.of(
//                        "foodId", foodData.get("food_id"),
//                        "foodName", foodData.get("food_name")
//                ));
                // ▼ 构建标准响应结构
                Map<String, Object> result = new HashMap<>();
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", Map.of(
                        "foodId", foodData.get("food_id"),
                        "foodName", foodData.get("food_name")
                ));
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

    @ApiOperation("根据食物ID查询对应信息")
    @GetMapping("/byID")
    public ResponseEntity<?> getFoodByID(
            @ApiParam(value = "食物ID", required = true)
            @RequestParam String id) {
//        org.apache.logging.log4j.Logger log = null;
        logger.info("收到请求，食物 ID={}，类型={}", id, id.getClass());
        try {
            //▼ 精确匹配查询（添加 LIMIT 1 保证唯一性）
            String sql = "SELECT * FROM foods WHERE food_id = ? LIMIT 1";
            logger.info("Executing SQL: {} with param: {}", sql, id);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);

            if (results.isEmpty()) {
                //▼ 返回结构化错误信息
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", "id不存在: " + id));
            } else {
                //▼ 提取并格式化结果
                Map<String, Object> foodData = results.get(0);
                System.out.println("foodData:"+foodData);
//                return ResponseEntity.ok(Map.of(
//                        "foodId", foodData.get("food_id"),
//                        "foodName", foodData.get("food_name")
//                ));
                // ▼ 构建标准响应结构
                Map<String, Object> result = new HashMap<>();
//                result.put("foodId", foodData.get("food_id"));
//                result.put("foodName", foodData.get("food_name"));
//                result.put("category", foodData.get("category"));
//                result.put("calories", foodData.get("calories"));
//                result.put("protein", foodData.get("protein"));
//                result.put("fat", foodData.get("fat"));
//                result.put("unit", foodData.get("unit"));
//                result.put("image", foodData.get("image"));
//                result.put("details", foodData.get("details"));

                result.put("code", 200);
                result.put("message", "success");
                result.put("data", Map.of(
                        "foodId", foodData.get("food_id") != null ? foodData.get("food_id") : "",
                        "foodName", foodData.get("food_name") != null ? foodData.get("food_name") : "",
                        "category", foodData.get("category") != null ? foodData.get("category") : "",
                        "calories", foodData.get("calories") != null ? foodData.get("calories") : 0,
                        "protein", foodData.get("protein") != null ? foodData.get("protein") : 0,
                        "fat", foodData.get("fat") != null ? foodData.get("fat") : 0,
                        "unit", foodData.get("unit") != null ? foodData.get("unit") : "",
                        "image", foodData.get("image") != null ? foodData.get("image") : "",
                        "details", foodData.get("details") != null ? foodData.get("details") : ""
                ));
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
