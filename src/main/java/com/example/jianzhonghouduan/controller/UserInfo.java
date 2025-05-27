package com.example.jianzhonghouduan.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "用户信息管理")
    @RestController
    @RequestMapping("/user")
    public class UserInfo {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @ApiOperation("获取用户信息")
    @GetMapping("/getUserInfo")
    public List<Map<String, Object>> getPlanFoodList() {
        // 编写SQL查询语句
//        String sql = "SELECT * FROM food_logs WHERE user_id = ?";
        String sql = "SELECT * FROM users WHERE people_id = ? ";
        System.out.print(sql);
        // 获取当前用户ID (这里假设为1,实际项目中需要从登录用户信息中获取)
        int memberId = 1;

        // 执行查询并返回结果
        return jdbcTemplate.queryForList(sql, memberId);
    }


    @ApiOperation("修改用户信息")
    @PostMapping("/update")
    public Map<String, Object> UserInfo(
            @RequestBody Map<String, Object> params // 改使用 Map 接收显式 JSON 参数
    ) {
        // 打印完整请求参数
        System.out.println("Received params: " + params.toString());
        try {

            Object username = params.get("username");
            Object phone = params.get("phone");
            Object height = params.get("height");
            Object currentweight = params.get("currentweight");
            // 安全 SQL 构建（预编译）
            String sql = "UPDATE users SET username=?,phone=?,height=?,currentweight=? WHERE people_id=?";

            Object[] args = {

                    username,
                    phone,
                    height,
                    currentweight,
                    1 // 实际应替换为真实用户 ID 获取方式
            };

            System.out.printf("Safe SQL: %s\nParams: %s\n", sql, Arrays.toString(args));

            int result = jdbcTemplate.update(sql, args);

            // 结果处理（与原逻辑保持风格统一）
            if (result > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "记录添加成功");
                response.put("data", getLatestRecord());
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
    // 保持与/list 相同的数据格式
    private Map<String, Object> getLatestRecord() {
        String query = "SELECT * FROM users WHERE people_id = 1 ";

        return jdbcTemplate.queryForList(query).get(0);
    }
}