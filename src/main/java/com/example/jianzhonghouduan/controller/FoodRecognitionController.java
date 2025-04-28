//package com.example.jianzhonghouduan.controller;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URLEncoder;
//import java.nio.file.Files;
//import java.util.Base64;
//
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.client.RestTemplate;
//
//
//@CrossOrigin(origins = "http://localhost:8082") // 允许这个域名的请求
//
//
//
//@RestController
//@RequestMapping("/api")
//public class FoodRecognitionController {
//
////    @Value("${api.baidu.apiKey}")
//    private String apiKey="4ce0LZTouA65gNZOIgckOLRY";
//
////    @Value("${api.baidu.secretKey}")
//    private String secretKey="5b7Tk0rrxeResVXdo5cziMf4I66bNBtr";
//
//    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
//    private static final String RECOGNITION_URL = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish";
//
//    @PostMapping("/recognize")
//    public ResponseEntity<String> recognizeFood(@RequestParam("file") MultipartFile file) {
//        try {
//            // 获取并验证Access Token
//            String accessToken = getAccessToken();
//            if (accessToken == null) {
//                return ResponseEntity.badRequest().body("Failed to retrieve access token.");
//            }
//
//            // 验证和编码图像
//            byte[] imageBytes = file.getBytes();
//            if (imageBytes.length > 4 * 1024 * 1024) {
//                return ResponseEntity.badRequest().body("Image size exceeds 4 MB");
//            }
//
//            // Base64编码
//            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//
//            // 请求API识别菜品
//            String response = callRecognitionApi(accessToken, base64Image);
//
//            return ResponseEntity.ok(response);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body("Error processing the image");
//        }
//    }
//
//    private String getAccessToken() {
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            String tokenUrl = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
//            return restTemplate.getForObject(tokenUrl, String.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private String callRecognitionApi(String accessToken, String base64Image) {
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//            String requestUrl = RECOGNITION_URL + "?access_token=" + accessToken;
//            String requestBody = "image=" + URLEncoder.encode(base64Image, "UTF-8");
//
//            return restTemplate.postForObject(URI.create(requestUrl), requestBody, String.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}