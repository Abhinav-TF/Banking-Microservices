package com.tnf.api_gateway.controller;

import java.util.HashMap;
import java.util.Map;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/fallback")
public class FallbackController {
	
	@GetMapping("/products")
	public ResponseEntity<Map<String, String>> productServiceFallback(){
		Map<String, String> response = new HashMap<>();
		response.put("message", "Payment service is currently unavailable. please reachoit later");
		response.put("status", "503");
		return ResponseEntity.status(503).body(response);
		
	}
	@GetMapping("/payment")
	public ResponseEntity<Map<String, String>> paymentServiceFallback(){
		Map<String, String> response = new HashMap<>();
		response.put("message", "Payment service is currently unavailable. please reachoit later");
		response.put("status", "503");
		return ResponseEntity.status(503).body(response);
 
	}
	@GetMapping("/orders")
	public ResponseEntity<Map<String, String>> ordersServiceFallback(){
		Map<String, String> response = new HashMap<>();
		response.put("message", "Payment service is currently unavailable. please reachoit later");
		response.put("status", "503");
		return ResponseEntity.status(503).body(response);
}
}