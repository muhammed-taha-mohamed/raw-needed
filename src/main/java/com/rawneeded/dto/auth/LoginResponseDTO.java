package com.rawneeded.dto.auth;
import com.rawneeded.enummeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LoginResponseDTO {
    private String userId;
    private String token;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;


}
