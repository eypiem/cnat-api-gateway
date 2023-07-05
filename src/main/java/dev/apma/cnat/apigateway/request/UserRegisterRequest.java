package dev.apma.cnat.apigateway.request;


public record UserRegisterRequest(String email, String password, String firstName, String lastName) {
}
