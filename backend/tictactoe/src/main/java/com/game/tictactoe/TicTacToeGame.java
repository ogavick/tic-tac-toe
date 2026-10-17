package com.game.tictactoe;
import java.util.Arrays;
import java.util.Map;

public class TicTacToeGame {
    private final String[] board = new String[9];
    private String currentTurn = "X";
    private String winner;

    public enum MoveResult {
        ACCEPTED,
        INVALID_INDEX,
        INVALID_PLAYER,
        GAME_OVER,
        WRONG_TURN,
        CELL_OCCUPIED
    }

    public TicTacToeGame() { Arrays.fill(board, ""); }

    public synchronized MoveResult makeMove(Integer index, String player) {
        if (index == null || index < 0 || index >= board.length) return MoveResult.INVALID_INDEX;
        if (!"X".equals(player) && !"O".equals(player)) return MoveResult.INVALID_PLAYER;
        if (winner != null) return MoveResult.GAME_OVER;
        if (!player.equals(currentTurn)) return MoveResult.WRONG_TURN;
        if (!board[index].isEmpty()) return MoveResult.CELL_OCCUPIED;

        board[index] = player;
        checkWinner();
        currentTurn = currentTurn.equals("X") ? "O" : "X";
        return MoveResult.ACCEPTED;
    }

    public synchronized Map<String, Object> getState() {
        return Map.of(
            "board", board.clone(),
            "turn", currentTurn,
            "winner", winner == null ? "" : winner
        );
    }

    private void checkWinner() {
        int[][] wins = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};
        for (int[] c : wins) {
            if (!board[c[0]].equals("") && board[c[0]].equals(board[c[1]]) && board[c[1]].equals(board[c[2]])) {
                winner = board[c[0]]; return;
            }
        }
        boolean draw = true;
        for (String s : board) if (s.equals("")) draw = false;
        if (draw) winner = "DRAW";
    }

    public synchronized void reset() {
        Arrays.fill(board, "");
        winner = null;
        currentTurn = "X";
    }
}