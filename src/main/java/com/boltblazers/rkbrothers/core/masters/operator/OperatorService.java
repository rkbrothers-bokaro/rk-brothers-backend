package com.boltblazers.rkbrothers.core.masters.operator;

import com.boltblazers.rkbrothers.core.audit.AuditAction;
import com.boltblazers.rkbrothers.core.audit.Auditable;
import com.boltblazers.rkbrothers.core.common.DuplicateResourceException;
import com.boltblazers.rkbrothers.core.common.ResourceNotFoundException;
import com.boltblazers.rkbrothers.core.masters.operator.dto.OperatorRequest;
import com.boltblazers.rkbrothers.core.masters.operator.dto.OperatorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OperatorService {

    private static final String ENTITY_NAME = "Operator";

    private final OperatorRepository operatorRepository;

    @Transactional(readOnly = true)
    public Page<OperatorResponse> getAllOperators(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Operator> operators = (search == null || search.isBlank())
                ? operatorRepository.findAllByIsActiveTrue(pageable)
                : operatorRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        return operators.map(OperatorResponse::from);
    }

    @Transactional(readOnly = true)
    public OperatorResponse getOperatorById(Long id) {
        return OperatorResponse.from(findActiveById(id));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.CREATE)
    public OperatorResponse createOperator(OperatorRequest request) {
        if (operatorRepository.existsByLicenceNo(request.licenceNo())) {
            throw new DuplicateResourceException("Licence number already exists: " + request.licenceNo());
        }

        Operator operator = Operator.builder()
                .name(request.name())
                .phone(request.phone())
                .licenceNo(request.licenceNo())
                .licenceExpiry(request.licenceExpiry())
                .category(request.category())
                .build();
        return OperatorResponse.from(operatorRepository.save(operator));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.UPDATE)
    public OperatorResponse updateOperator(Long id, OperatorRequest request) {
        Operator operator = findActiveById(id);

        if (!operator.getLicenceNo().equalsIgnoreCase(request.licenceNo())
                && operatorRepository.existsByLicenceNo(request.licenceNo())) {
            throw new DuplicateResourceException("Licence number already exists: " + request.licenceNo());
        }

        operator.setName(request.name());
        operator.setPhone(request.phone());
        operator.setLicenceNo(request.licenceNo());
        operator.setLicenceExpiry(request.licenceExpiry());
        operator.setCategory(request.category());
        return OperatorResponse.from(operatorRepository.save(operator));
    }

    @Auditable(entityName = ENTITY_NAME, action = AuditAction.DELETE)
    public void deleteOperator(Long id) {
        Operator operator = findActiveById(id);
        operator.setActive(false);
        operatorRepository.save(operator);
    }

    private Operator findActiveById(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of(ENTITY_NAME, id));
        if (!operator.isActive()) {
            throw ResourceNotFoundException.of(ENTITY_NAME, id);
        }
        return operator;
    }
}
