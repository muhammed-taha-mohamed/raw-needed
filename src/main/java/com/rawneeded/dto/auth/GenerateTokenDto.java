package com.rawneeded.dto.auth;

import com.rawneeded.enummeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GenerateTokenDto {
    private String id;
    private String name;
    private String email;
    private Role role;

}
