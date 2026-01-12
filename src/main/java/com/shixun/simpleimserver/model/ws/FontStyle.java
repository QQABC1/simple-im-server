package com.shixun.simpleimserver.model.ws;

import lombok.Data;

@Data
public class FontStyle {
    private String color;       // e.g. "#000000"
    private Integer size;       // e.g. 14
    private Boolean bold;       // e.g. true
    private Boolean italic;     // e.g. false
}