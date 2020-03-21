package com.svaps.demo.models.responses;

import lombok.Value;

import java.util.Set;

@Value
public class UserFilesResponse {

    public Set<String> owned;
    public Set<String> shared;
}
