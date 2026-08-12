package com.boltblazers.rkbrothers.core.masters.operator.dto;

import com.boltblazers.rkbrothers.core.masters.operator.Operator;

import java.time.LocalDate;

public record OperatorResponse(
        Long id,
        String name,
        String phone,
        String licenceNo,
        LocalDate licenceExpiry,
        String category
) {
    public static OperatorResponse from(Operator operator) {
        return new OperatorResponse(
                operator.getId(),
                operator.getName(),
                operator.getPhone(),
                operator.getLicenceNo(),
                operator.getLicenceExpiry(),
                operator.getCategory()
        );
    }
}
