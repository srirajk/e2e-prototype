package org.example.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchModel implements Serializable, Cloneable {

    private String matchId;
    private String description;
    private String matchableItemId;
    private String decision;

    @Override
    public MatchModel clone() {
        try {
            return (MatchModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }

}
