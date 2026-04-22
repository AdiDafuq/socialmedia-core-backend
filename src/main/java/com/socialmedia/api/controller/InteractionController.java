package com.socialmedia.api.controller;

import com.socialmedia.api.service.InteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/bot-reply")
    public ResponseEntity<String> botReply(
            @RequestParam Long postId,
            @RequestParam Long botId,
            @RequestParam Long humanId,
            @RequestParam int depth) {

        boolean allowed = interactionService.handleBotReply(postId, botId, humanId, depth);

        if (!allowed) {
            return ResponseEntity.badRequest().body("Blocked by guardrails");
        }

        return ResponseEntity.ok("Bot reply accepted");
    }

    @PostMapping("/like")
    public ResponseEntity<String> like(@RequestParam Long postId) {
        interactionService.handleHumanLike(postId);
        return ResponseEntity.ok("Like counted");
    }

    @PostMapping("/comment")
    public ResponseEntity<String> comment(@RequestParam Long postId) {
        interactionService.handleHumanComment(postId);
        return ResponseEntity.ok("Comment counted");
    }
}