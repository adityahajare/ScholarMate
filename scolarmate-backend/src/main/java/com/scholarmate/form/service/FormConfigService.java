package com.scholarmate.form.service;

import com.scholarmate.form.model.ConditionalRule;
import com.scholarmate.form.model.FieldOption;
import com.scholarmate.form.model.MasterField;
import com.scholarmate.form.repository.ConditionalRuleRepository;
import com.scholarmate.form.repository.FieldOptionRepository;
import com.scholarmate.form.repository.FieldOptionSetRepository;
import com.scholarmate.form.repository.MasterFieldRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FormConfigService {

    private final MasterFieldRepository masterFieldRepo;
    private final FieldOptionSetRepository optionSetRepo;
    private final FieldOptionRepository optionRepo;
    private final ConditionalRuleRepository ruleRepo;

    public FormConfigService(MasterFieldRepository masterFieldRepo,
                             FieldOptionSetRepository optionSetRepo,
                             FieldOptionRepository optionRepo,
                             ConditionalRuleRepository ruleRepo) {
        this.masterFieldRepo = masterFieldRepo;
        this.optionSetRepo = optionSetRepo;
        this.optionRepo = optionRepo;
        this.ruleRepo = ruleRepo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormDefinition {
        @Builder.Default
        private List<FieldDto> fields = new ArrayList<>();
        @Builder.Default
        private List<RuleDto> rules = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldDto {
        private Long id;
        private String fieldKey;
        private String label;
        private String section;
        private String dataType;
        private String optionsKey;
        private Boolean defaultRequired;
        private String displayCondition;
        private String validationRule;
        private String dataPurpose;
        @Builder.Default
        private List<OptionDto> options = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionDto {
        private String value;
        private String label;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RuleDto {
        private String fieldKey;
        private String conditionJson;
        private String action;
        private String ruleCode;
        private String description;
    }

    public FormDefinition getFormDefinition() {
        FormDefinition def = new FormDefinition();

        List<MasterField> fields = masterFieldRepo.findAll();
        Map<String, FieldDto> keyToDto = new HashMap<>();

        for (MasterField f : fields) {
            FieldDto dto = FieldDto.builder()
                    .id(f.getId())
                    .fieldKey(f.getFieldKey())
                    .label(f.getLabel())
                    .section(f.getSection())
                    .dataType(f.getDataType())
                    .optionsKey(f.getOptionsKey())
                    .defaultRequired(f.getDefaultRequired())
                    .displayCondition(f.getDisplayCondition())
                    .validationRule(f.getValidationRule())
                    .dataPurpose(f.getDataPurpose())
                    .options(new ArrayList<>())
                    .build();
            keyToDto.put(dto.getFieldKey(), dto);
            def.getFields().add(dto);
        }

        Set<String> optionKeys = fields.stream()
                .map(MasterField::getOptionsKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (String optKey : optionKeys) {
            optionSetRepo.findByOptionKey(optKey).ifPresent(set -> {
                List<FieldOption> opts = optionRepo.findAllByOptionSetAndActiveTrueOrderBySortOrderAsc(set);
                fields.stream()
                        .filter(f -> optKey.equals(f.getOptionsKey()))
                        .forEach(f -> {
                            FieldDto fdto = keyToDto.get(f.getFieldKey());
                            if (fdto != null) {
                                for (FieldOption o : opts) {
                                    fdto.getOptions().add(new OptionDto(o.getOptionValue(), o.getOptionLabel()));
                                }
                            }
                        });
            });
        }

        List<ConditionalRule> rules = ruleRepo.findAll();
        for (ConditionalRule r : rules) {
            def.getRules().add(new RuleDto(
                    r.getFieldKey(),
                    r.getConditionJson(),
                    r.getAction(),
                    r.getRuleCode(),
                    r.getDescription()
            ));
        }

        return def;
    }
}
