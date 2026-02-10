package com.shopjoy.entity.converter;

import com.shopjoy.entity.UserType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for UserType enum to handle lowercase database values.
 */
@Converter(autoApply = true)
public class UserTypeConverter implements AttributeConverter<UserType, String> {

    @Override
    public String convertToDatabaseColumn(UserType userType) {
        if (userType == null) {
            return null;
        }
        return userType.name().toLowerCase();
    }

    @Override
    public UserType convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return UserType.valueOf(dbValue.toUpperCase());
    }
}
