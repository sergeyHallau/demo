package com.svaps.demo.models;


import lombok.Value;

@Value
public class ShareRequest {
    public String email;
    public String fileId;
}
