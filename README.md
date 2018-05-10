# Tablut AI

AI agent built to play Tablut, a variant of the classic Viking boardgame Hnefatafl.

Initially implemented as the final project for COMP 424 - Artificial Intelligence at McGill Univeristy

Currently extending the AI to play in the [Hnefatafl Open Tournament in December 2018](https://soapbox.manywords.press/2016-opentafl-tafl-open-ai-tournament/).

For a full description of the project, see the ["tablut-ai-report.pdf" file](https://github.com/ibratanov/tablut.ai/blob/master/tablut-ai-report.pdf).

## Game Description
Tablut is a two-player board game - a Finnish variant of the classic Viking game Hnefatafl, where the each side has a different goal. The Swedes (white pieces) must move their king piece to a corner without it being captured. The Muscovites (black pieces) must capture the king by surrounding him and prevent him from reaching any of the corners. The full game rules can be found [here](http://www.ludoteka.com/tablut-en.html).

## Strategy
The strategy implemented uses the minimax algorithm with alpha-beta pruning to traverse the game state tree. The evaluation function used takes into account several variables such as both players’ current piece count, and king distance from the goal. The agent achieves a 100% win rate against the baseline agents (random and greedy) as well as simpler agents with other heuristics.
