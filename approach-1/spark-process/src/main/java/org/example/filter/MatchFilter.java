package org.example.filter;

import org.example.common.model.FileRequestLineEvent;
import org.example.common.model.MatchModel;

import java.util.List;
import java.util.stream.Collectors;

public class MatchFilter {

    public static FileRequestLineEvent executeFilterChain(final FileRequestLineEvent fileRequestLineEvent) {
        FileRequestLineEvent response = fileRequestLineEvent.clone();
        List<MatchModel> matchModelList = response.getHits().parallelStream().map(
                matchModel -> {
                    matchModel.setDescription("Description of the match" + Math.random());
                    matchModel.setMatchId(String.valueOf(Math.random()));
                    matchModel.setMatchableItemId(String.valueOf(Math.random()));
                    matchModel.setDecision("risk");
                    return matchModel;
                }
        ).collect(Collectors.toList());
        response.setHits(matchModelList);
        String fileRequestString = fileRequestLineEvent.getFileRequest().entrySet().stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.joining("|"));
        response.setRawText(fileRequestString);
        return response;
    }
}
