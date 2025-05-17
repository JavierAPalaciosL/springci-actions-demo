package com.tests.oauth.controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class GoogleAuthController {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";


    public GoogleAuthController(RestTemplateBuilder restTemplate){
        this.restTemplate = restTemplate.build();
    }

    @GetMapping("/signInGoogle")
    public ResponseEntity<Map<String,String>> signInGoogle() {

        String url = UriComponentsBuilder
                .fromHttpUrl(AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("access_type", "offline")
                .build()
                .toUriString();

        return ResponseEntity.ok(Collections.singletonMap("url", url));
    }


    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        // 1) Intercambiar code por tokens
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type",    "authorization_code");
        form.add("code",          code);
        form.add("redirect_uri",  redirectUri);
        form.add("client_id",     clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String,String>> tokenRequest = new HttpEntity<>(form, headers);

        ResponseEntity<Map<String, Object>> response =
                restTemplate.exchange(
                        "https://oauth2.googleapis.com/token",
                        HttpMethod.POST,
                        tokenRequest,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );



        Map<String, Object> tokenResponse = response.getBody();
        String idToken     = (String) tokenResponse.get("id_token");
        String accessToken = (String) tokenResponse.get("access_token");

        // 2) (Opcional) validar id_token con la librería de Google
        // 3) Extraer datos del payload y/o llamar userinfo
        // 4) Generar tu propio JWT
        //String jwtPropio = jwtService.createTokenDesdeIdToken(idToken);

        // 5) Devolver al cliente tu JWT o info de usuario
        Map<String,String> resp = new HashMap<>();
        resp.put("tokenType",   "Bearer");
        resp.put("accessToken", accessToken);
        return ResponseEntity.ok(resp);
    }


}
