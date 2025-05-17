package com.tests.oauth.dtos;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GoogleData {

    private final String authURL;
    private final String clientID;
    private final String responseType;
    private final String scopes;
    private final String access_type;

}
