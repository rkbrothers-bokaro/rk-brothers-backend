package com.boltblazers.rkbrothers.core.masters.party.dto;

import com.boltblazers.rkbrothers.core.masters.party.Party;

public record PartyResponse(
        Long id,
        String name,
        String contactPerson,
        String phone,
        String email
) {
    public static PartyResponse from(Party party) {
        return new PartyResponse(
                party.getId(),
                party.getName(),
                party.getContactPerson(),
                party.getPhone(),
                party.getEmail()
        );
    }
}
