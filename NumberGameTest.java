public class NumberGameTest {
    public static void main(String[] args) {
        System.out.println("Running comprehensive tests for NumberGame...");
        int passed = 0;
        int failed = 0;

        try {
            NumberGame game = new NumberGame();
            game.setVisible(false); // Don't show window during test

            // Test 1: Initial Game State
            if (game.attempts == 10 && game.MAX_ATTEMPTS == 10) {
                System.out.println("[PASS] Initial attempts initialized to 10");
                passed++;
            } else {
                System.err.println("[FAIL] Initial attempts incorrect");
                failed++;
            }

            if (game.secretNumber >= 1 && game.secretNumber <= 100) {
                System.out.println("[PASS] Secret number is within [1, 100]: " + game.secretNumber);
                passed++;
            } else {
                System.err.println("[FAIL] Secret number out of range");
                failed++;
            }

            // Test 2: Empty input validation
            game.guessField.setText("   ");
            game.checkGuess();
            if (game.attempts == 10) {
                System.out.println("[PASS] Empty input rejected without consuming attempts");
                passed++;
            } else {
                System.err.println("[FAIL] Empty input decremented attempts");
                failed++;
            }

            // Test 3: Non-numeric input validation
            game.guessField.setText("abc");
            game.checkGuess();
            if (game.attempts == 10) {
                System.out.println("[PASS] Non-numeric input rejected without consuming attempts");
                passed++;
            } else {
                System.err.println("[FAIL] Non-numeric input decremented attempts");
                failed++;
            }

            // Test 4: Out of bounds input validation (< 1 or > 100)
            game.guessField.setText("0");
            game.checkGuess();
            game.guessField.setText("101");
            game.checkGuess();
            if (game.attempts == 10) {
                System.out.println("[PASS] Out-of-bounds input rejected without consuming attempts");
                passed++;
            } else {
                System.err.println("[FAIL] Out-of-bounds input decremented attempts");
                failed++;
            }

            // Test 5: Lower guess produces Too Low
            game.secretNumber = 50;
            game.attempts = 10;
            game.guessField.setText("25");
            game.checkGuess();
            if (game.attempts == 9) {
                System.out.println("[PASS] Valid guess decremented attempts from 10 to 9");
                passed++;
            } else {
                System.err.println("[FAIL] Attempts not decremented properly: " + game.attempts);
                failed++;
            }

            // Test 6: Higher guess produces Too High
            game.guessField.setText("75");
            game.checkGuess();
            if (game.attempts == 8) {
                System.out.println("[PASS] Higher guess decremented attempts from 9 to 8");
                passed++;
            } else {
                System.err.println("[FAIL] Attempts not decremented properly: " + game.attempts);
                failed++;
            }

            // Test 7: Correct guess awards exact score (attempts + 1) * 10
            // Current attempts before check is 8. After decrementing in checkGuess, attempts = 7.
            // Expected score = (7 + 1) * 10 = 80.
            game.guessField.setText("50");
            game.checkGuess();
            if (game.totalGames == 1 && game.gamesWon == 1 && game.totalScore == 80) {
                System.out.println("[PASS] Correct guess logic verified: totalGames=1, gamesWon=1, score=80");
                passed++;
            } else {
                System.err.println("[FAIL] Score calculation incorrect: totalGames=" + game.totalGames + ", gamesWon=" + game.gamesWon + ", totalScore=" + game.totalScore);
                failed++;
            }

            // Test 8: Game Over after 10 attempts
            game.startNewGame();
            game.secretNumber = 42;
            for (int i = 0; i < 10; i++) {
                game.guessField.setText("1");
                game.checkGuess();
            }
            if (game.attempts == 0 && game.totalGames == 2 && game.gamesWon == 1 && !game.submitButton.isEnabled()) {
                System.out.println("[PASS] Game Over logic verified: attempts=0, submit disabled, totalGames=2");
                passed++;
            } else {
                System.err.println("[FAIL] Game Over state invalid");
                failed++;
            }

            game.dispose();

            System.out.println("\n-------------------------------------------");
            System.out.println("TEST SUMMARY: " + passed + " Passed, " + failed + " Failed.");
            System.out.println("-------------------------------------------");
            if (failed > 0) {
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
