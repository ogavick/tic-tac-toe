package com.game.tictactoe;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
public class GameController {
    private final TicTacToeGame game = new TicTacToeGame();

    @GetMapping("/state")
    public Map<String, Object> getState() {
        return game.getState();
    }

    @PostMapping("/move")
    public ResponseEntity<Map<String, Object>> move(@RequestBody MoveRequest request) {
        if (request == null) {
            return error(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        TicTacToeGame.MoveResult result = game.makeMove(request.index(), request.player());
        if (result == TicTacToeGame.MoveResult.ACCEPTED) {
            return ResponseEntity.ok(getState());
        }

        HttpStatus status = switch (result) {
            case INVALID_INDEX, INVALID_PLAYER -> HttpStatus.BAD_REQUEST;
            case GAME_OVER, WRONG_TURN, CELL_OCCUPIED -> HttpStatus.CONFLICT;
            case ACCEPTED -> throw new IllegalStateException("Accepted moves are handled above");
        };
        return error(status, switch (result) {
            case INVALID_INDEX -> "Move index must be between 0 and 8";
            case INVALID_PLAYER -> "Player must be X or O";
            case GAME_OVER -> "The game is already over";
            case WRONG_TURN -> "It is not that player's turn";
            case CELL_OCCUPIED -> "That cell is already occupied";
            case ACCEPTED -> throw new IllegalStateException("Accepted moves are handled above");
        });
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        game.reset();
        return getState();
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    public record MoveRequest(Integer index, String player) {}
}