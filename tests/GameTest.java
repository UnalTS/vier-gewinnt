public final class GameTest {
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    static void win(int... columns) {
        Game game = new Game();
        for (int c : columns) check(game.drop(c) >= 0, "Move rejected");
        check(game.finished() && game.winningLine().size() == 4, "Expected four in a row");
        check(game.drop(6) == -1, "Move after win");
    }

    public static void main(String[] args) {
        win(0,0,1,1,2,2,3);
        win(6,0,6,0,5,0,5,0);
        win(0,1,1,2,5,2,2,3,5,3,6,3,3);
        win(6,5,5,4,1,4,4,3,1,3,0,3,3);
        Game game = new Game();
        for (int i = 0; i < 6; i++) check(game.drop(0) == 5-i, "Gravity");
        check(game.drop(0) == -1 && game.player() == 1, "Full column changed turn");
        game = new Game();
        // Alternating pairs of columns keep both diagonals broken.
        for (int pair = 0; pair < 3; pair++) {
            for (int c : new int[]{0,2,1,3,4,6,5,0,2,1,3,4,6,5})
                check(game.drop(c) >= 0, "Unexpected end during draw");
        }
        check(game.finished() && game.winningLine().isEmpty() && game.drop(2) == -1, "Draw");
        System.out.println("Alle Regeltests bestanden.");
    }
}
