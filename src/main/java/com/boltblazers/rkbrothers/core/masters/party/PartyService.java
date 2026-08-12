package com.boltblazers.rkbrothers.core.masters.party;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.party.dto.PartyRequest;
import com.boltblazers.rkbrothers.core.masters.party.dto.PartyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyService {

    private static final String ENTITY_NAME = "Party";

    private final PartyRepository partyRepository;

    @Transactional(readOnly = true)
    public Page<PartyResponse> getAllParties(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Party> parties = (search == null || search.isBlank())
                ? partyRepository.findAllByIsActiveTrue(pageable)
                : partyRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        return parties.map(PartyResponse::from);
    }

    @Transactional(readOnly = true)
    public PartyResponse getPartyById(Long id) {
        return PartyResponse.from(findActiveById(id));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public PartyResponse createParty(PartyRequest request) {
        Party party = Party.builder()
                .name(request.name())
                .contactPerson(request.contactPerson())
                .phone(request.phone())
                .email(request.email())
                .build();
        return PartyResponse.from(partyRepository.save(party));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public PartyResponse updateParty(Long id, PartyRequest request) {
        Party party = findActiveById(id);
        party.setName(request.name());
        party.setContactPerson(request.contactPerson());
        party.setPhone(request.phone());
        party.setEmail(request.email());
        return PartyResponse.from(partyRepository.save(party));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteParty(Long id) {
        Party party = findActiveById(id);
        party.setActive(false);
        partyRepository.save(party);
    }

    private Party findActiveById(Long id) {
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        if (!party.isActive()) {
            throw ResourceNotFoundException.of(ENTITY_NAME, id);
        }
        return party;
    }
}
