package com.shopjoy.entity.converter;

import com.shopjoy.entity.AddressType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for AddressType enum to handle lowercase database values.
 */
@Converter(autoApply = true)
public class AddressTypeConverter implements AttributeConverter<AddressType, String> {

    @Override
    public String convertToDatabaseColumn(AddressType addressType) {
        if (addressType == null) {
            return null;
        }
        return addressType.name().toLowerCase();
    }

    @Override
    public AddressType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return AddressType.valueOf(dbValue.toUpperCase());
    }
}
