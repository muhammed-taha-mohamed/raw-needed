package com.rawneeded.dto;

import com.rawneeded.enummeration.TemplateName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MailDto {
    Map<String, Object> model;
    TemplateName templateName;
    String toEmail;
    String subject;
}
