package com.example.rph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Risk {

/*    "name": "Maria Sanchez",
            "nationality": "Iranian",
            "risk_score": 75,
            "reason": "Alleged involvement in sanctions evasion schemes"*/

    private String name, nationality, reason;
    private int riskScore;
}
