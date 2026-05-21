package com.hdl.soar.module.system.controller.admin.oauth2;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Backend - OAuth2 Client")
@Validated
@RestController
@RequestMapping("/system/oauth2-client")
public class OAuth2ClientController {
}
