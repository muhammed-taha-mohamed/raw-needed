package com.rawneeded.dto.user;


import com.rawneeded.enummeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRequestDto {
    private String email;
    private String phoneNumber;
    private Role role;
}
