package com.rawneeded.dto.user;


import com.rawneeded.enummeration.Category;
import com.rawneeded.enummeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponseDto {
    private String id;
    private String name;
    private Role role;
    private String email;
    private String phoneNumber;
    private Category preferredCategory;

}
