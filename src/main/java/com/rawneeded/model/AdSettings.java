package com.rawneeded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Ad settings (single document): featured ad price set by admin.
 */
@Document(collection = "adSettings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdSettings {
    @Id
    private String id; // e.g. "default"
    private BigDecimal featuredPrice; // extra price for featuring ad at top
}
