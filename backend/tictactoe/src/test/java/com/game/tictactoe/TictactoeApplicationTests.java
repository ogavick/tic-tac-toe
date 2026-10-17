package com.game.tictactoe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TictactoeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void resetGame() throws Exception {
		mockMvc.perform(post("/reset"));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void servesFrontendAtRoot() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(forwardedUrl("index.html"));

		mockMvc.perform(get("/index.html"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Online TicTacToe")));
	}

	@Test
	void returnsInitialGameState() throws Exception {
		mockMvc.perform(get("/state"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.board", hasSize(9)))
			.andExpect(jsonPath("$.turn").value("X"))
			.andExpect(jsonPath("$.winner").value(""));
	}

	@Test
	void acceptsValidMoveAndRejectsWrongTurnAndOccupiedCell() throws Exception {
		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":0,\"player\":\"X\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.board[0]").value("X"))
			.andExpect(jsonPath("$.turn").value("O"));

		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":1,\"player\":\"X\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("It is not that player's turn"));

		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":0,\"player\":\"O\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("That cell is already occupied"));
	}

	@Test
	void rejectsInvalidMoveData() throws Exception {
		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":9,\"player\":\"X\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Move index must be between 0 and 8"));

		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":0,\"player\":\"Y\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Player must be X or O"));

		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"player\":\"X\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Move index must be between 0 and 8"));
	}

	@Test
	void detectsWinnerAndPreventsFurtherMoves() throws Exception {
		play(0, "X");
		play(3, "O");
		play(1, "X");
		play(4, "O");
		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":2,\"player\":\"X\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.winner").value("X"));

		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":5,\"player\":\"O\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("The game is already over"));
	}

	@Test
	void detectsDraw() throws Exception {
		play(0, "X");
		play(1, "O");
		play(2, "X");
		play(4, "O");
		play(3, "X");
		play(5, "O");
		play(7, "X");
		play(6, "O");
		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":8,\"player\":\"X\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.winner").value("DRAW"));
	}

	@Test
	void resetClearsBoardAndWinner() throws Exception {
		play(0, "X");

		mockMvc.perform(post("/reset"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.board[0]").value(""))
			.andExpect(jsonPath("$.turn").value("X"))
			.andExpect(jsonPath("$.winner").value(""));
	}

	private void play(int index, String player) throws Exception {
		mockMvc.perform(post("/move").contentType("application/json")
				.content("{\"index\":" + index + ",\"player\":\"" + player + "\"}"))
			.andExpect(status().isOk());
	}
}
