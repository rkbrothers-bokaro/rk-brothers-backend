import re

with open(r"d:\BB Client Projects\rk-brothers\rk-brothers-backend\src\main\java\com\boltblazers\rkbrothers\core\masters\workorder\WorkOrderService.java", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("request.partyId()", "request.partyName()")

old_resolve = """    private Party resolveParty(Long partyId) {
        if (partyId == null) {
            return null;
        }
        return partyRepository.findById(partyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Party", partyId));
    }"""
    
new_resolve = """    private Party resolveParty(String partyName) {
        if (partyName == null || partyName.isBlank()) {
            return null;
        }
        return partyRepository.findByNameIgnoreCase(partyName.trim())
                .orElseGet(() -> {
                    Party newParty = Party.builder()
                            .name(partyName.trim())
                            .isActive(true)
                            .build();
                    return partyRepository.save(newParty);
                });
    }"""
content = content.replace(old_resolve, new_resolve)

with open(r"d:\BB Client Projects\rk-brothers\rk-brothers-backend\src\main\java\com\boltblazers\rkbrothers\core\masters\workorder\WorkOrderService.java", "w", encoding="utf-8") as f:
    f.write(content)
