package com.shopjoy.entity.converter;

import com.shopjoy.entity.PaymentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PaymentStatus enum to handle lowercase database values.
 */
@Converter(autoApply = true)
public class PaymentStatusConverter implements AttributeConverter<PaymentStatus, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStatus status) {
        if (status == null) {
            return null;
        }
        return status.name().toLowerCase();
    }

    @Override
    public PaymentStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return PaymentStatus.valueOf(dbValue.toUpperCase());
    }
}
