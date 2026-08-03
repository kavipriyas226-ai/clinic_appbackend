package com.devsclinic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** A login account — either the clinic Admin or a staff User created by the Admin. */
@Document(collection = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    private String id;

    private String username;

    @JsonIgnore
    private String passwordHash;

    /** "ADMIN" or "USER". */
    @Builder.Default
    private String role = "USER";

    @Builder.Default
    private boolean enabled = true;
}
