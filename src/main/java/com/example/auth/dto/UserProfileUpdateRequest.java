package com.example.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String name;    // nick name
    private String profileImage;

    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String address1;
    private String address2;
    private String bgImage;
}
