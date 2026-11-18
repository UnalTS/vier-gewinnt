import java.util.ArrayList;
import java.util.List;

final class Game {
    static final int ROWS = 6, COLUMNS = 7;
    record Cell(int row, int column) {}
    private final int[][] board = new int[ROWS][COLUMNS];
    private int player = 1;
    private int moves;
    private List<Cell> line = List.of();

    int player() { return player; }
    int at(int row, int column) { return board[row][column]; }
    List<Cell> winningLine() { return line; }
    boolean finished() { return !line.isEmpty() || moves == ROWS * COLUMNS; }

    // Row zero is at the top. A rejected move leaves the turn unchanged.
    int drop(int column) {
        if (finished() || column < 0 || column >= COLUMNS) return -1;
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] != 0) continue;
            board[row][column] = player;
            moves++;
            line = findLine(row, column);
            if (!finished()) player = 3 - player;
            return row;
        }
        return -1;
    }

    private List<Cell> findLine(int row, int column) {
        int[][] directions = {{0,1}, {1,0}, {1,1}, {1,-1}};
        for (int[] direction : directions) {
            List<Cell> cells = new ArrayList<>();
            cells.add(new Cell(row, column));
            for (int sign : new int[]{-1, 1}) {
                int r = row + sign * direction[0], c = column + sign * direction[1];
                while (r >= 0 && r < ROWS && c >= 0 && c < COLUMNS && board[r][c] == player) {
                    cells.add(new Cell(r, c));
                    r += sign * direction[0];
                    c += sign * direction[1];
                }
            }
            if (cells.size() >= 4) return List.copyOf(cells);
        }
        return List.of();
    }
}
