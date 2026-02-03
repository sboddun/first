package com.insulet.practice.first.routes;

public record ErrorResponse(String message, String error, String code, Long timestamp) {}
