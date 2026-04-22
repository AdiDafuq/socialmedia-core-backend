package com.socialmedia.api.service;

import org.springframework.stereotype.Service;

@Service
public class InteractionService {

    private final GuardrailService guardrailService;
    private final ViralityScoreService viralityScoreService;

    public InteractionService(GuardrailService guardrailService,
                              ViralityScoreService viralityScoreService) {
        this.guardrailService = guardrailService;
        this.viralityScoreService = viralityScoreService;
    }

    public boolean handleBotReply(Long postId, Long botId, Long humanId, int depth) {

        if (!guardrailService.allowDepth(depth)) {
            return false;
        }

        if (!guardrailService.allowCooldown(botId, humanId)) {
            return false;
        }

        if (!guardrailService.allowBotReply(postId)) {
            return false;
        }


        viralityScoreService.incrementBotReply(postId);

        return true;
    }

    public void handleHumanLike(Long postId) {
        viralityScoreService.incrementHumanLike(postId);
    }

    public void handleHumanComment(Long postId) {
        viralityScoreService.incrementHumanComment(postId);
    }
}