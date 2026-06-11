package com.srb.backend.ai;

public record DeepSeekMessage(String role, String content) {

    public static DeepSeekMessage system(String content) {
        return new DeepSeekMessage("system", content);
    }

    public static DeepSeekMessage user(String content) {
        return new DeepSeekMessage("user", content);
    }

    public static DeepSeekMessage assistant(String content) {
        return new DeepSeekMessage("assistant", content);
    }
}
