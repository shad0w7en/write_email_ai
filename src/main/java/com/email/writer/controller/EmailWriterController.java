package com.email.writer.controller;


import com.email.writer.model.EmailModel;
import com.email.writer.service.EmailWriterService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailWriterController {

    @Autowired
    private EmailWriterService emailWriterService;


    @GetMapping("/test")
    public String test(){
        System.out.println("Enter test");
        return "success";
    }
    @PostMapping("/generateResponse")
    public ResponseEntity<String> generateResponse(@RequestBody EmailModel emailModel){
        try {
            String response = emailWriterService.getEmailResponse(emailModel);;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

}
