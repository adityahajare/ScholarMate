package com.scholarmate.form.controller;

import com.scholarmate.form.service.FormConfigService;
import com.scholarmate.form.service.FormConfigService.FormDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/form")
@RequiredArgsConstructor
@CrossOrigin
public class FormConfigController {

    private final FormConfigService formService;

    @GetMapping("/config")
    public ResponseEntity<FormDefinition> getFormConfig() {
        return ResponseEntity.ok(formService.getFormDefinition());
    }
}
