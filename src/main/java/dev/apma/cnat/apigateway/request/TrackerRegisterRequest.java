package dev.apma.cnat.apigateway.request;


import jakarta.validation.constraints.NotBlank;

/**
 * This request class defines the body for a <i>tracker register</i> request.
 *
 * @author Amir Parsa Mahdian
 */
public record TrackerRegisterRequest(@NotBlank String name) {
}
