package com.example.demo.base.risk;

import com.example.demo.base.base.entity.Client;
import com.example.demo.base.base.entity.RiskAssessment;
import com.example.demo.base.base.entity.RiskSignalEntity;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class RiskAssessmentMapper {

    public static RiskAssessment toEntity(
            Client client,
            RiskAssessmentResult result
    ) {

        RiskAssessment ra = new RiskAssessment.Builder(
                client,
                result.getScore(),
                result.getLevel(),
                LocalDateTime.now()
        )
                .reasons(result.getReasons())
                .recommendation(result.getRecommendation())
                .build();

        if (result.getSignals() != null && !result.getSignals().isEmpty()) {
            Set<RiskSignalEntity> signals = result.getSignals()
                    .stream()
                    .map(s -> new RiskSignalEntity(s, ra))
                    .collect(Collectors.toSet());

            ra.setSignals(signals);
        }

        return ra;
    }
}
