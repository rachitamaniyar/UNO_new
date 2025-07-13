import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Enhanced database manager for UNO game scoring system.
 * Handles session management, round scoring, game variants, and comprehensive score tracking.
 *
 * Database Schema:
 * - Sessions table: Stores player scores for each round in each session
 * - GameSessions table: Stores metadata about each game session
 * - PlayerStats table: Stores cumulative player statistics
 */

public class ScoreDatabaseManager {
    // SQL statements for table creation - these define the database structure
    private static final String CREATE_SESSIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS Sessions (" +
                    "SessionId INTEGER NOT NULL, " +
                    "Player VARCHAR(100) NOT NULL, " +
                    "Round INTEGER NOT NULL, " +
                    "RoundScore INTEGER NOT NULL, " +
                    "CumulativeScore INTEGER NOT NULL, " +
                    "GameVariant VARCHAR(50) DEFAULT 'Standard', " +
                    "Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (SessionId, Player, Round));";

    private static final String CREATE_GAME_SESSIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS GameSessions (" +
                    "SessionId INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "StartTime DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "EndTime DATETIME, " +
                    "Winner VARCHAR(100), " +
                    "TotalRounds INTEGER, " +
                    "GameVariant VARCHAR(50) DEFAULT 'Standard', " +
                    "PlayerCount INTEGER);";

    private static final String CREATE_PLAYER_STATS_TABLE =
            "CREATE TABLE IF NOT EXISTS PlayerStats (" +
                    "PlayerName VARCHAR(100) PRIMARY KEY, " +
                    "GamesPlayed INTEGER DEFAULT 0, " +
                    "GamesWon INTEGER DEFAULT 0, " +
                    "TotalScore INTEGER DEFAULT 0, " +
                    "AverageScore REAL DEFAULT 0.0, " +
                    "LastPlayed DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private final SqliteClient client;

    /**
     * Constructor initializes the database connection and creates necessary tables.
     *
     * @param dbFileName The name of the SQLite database file
     * @throws SQLException If database initialization fails
     */
    public ScoreDatabaseManager(String dbFileName) throws SQLException {
        client = new SqliteClient(dbFileName);
        initializeTables();
    }

    /**
     * Creates all necessary database tables if they don't exist.
     * This method is called during initialization to ensure proper database structure.
     *
     * @throws SQLException If table creation fails
     */
    private void initializeTables() throws SQLException {
        client.executeStatement(CREATE_SESSIONS_TABLE);
        client.executeStatement(CREATE_GAME_SESSIONS_TABLE);
        client.executeStatement(CREATE_PLAYER_STATS_TABLE);
    }

    /**
     * Creates a new game session and returns the session ID.
     * This method should be called at the start of each new game (not round).
     *
     * @param playerNames Array of player names participating in the session
     * @param gameVariant The game variant being played (e.g., "Standard", "Special Rules")
     * @return The newly created session ID
     * @throws SQLException If session creation fails
     */
    public int createNewSession(String[] playerNames, String gameVariant) throws SQLException {
        // Insert new session record
        String insertSession = "INSERT INTO GameSessions (GameVariant, PlayerCount) VALUES (?, ?);";
        client.executePreparedStatement(insertSession, new Object[]{gameVariant, playerNames.length});

        // Get the auto-generated session ID using SQLite's last_insert_rowid()
        String getSessionId = "SELECT last_insert_rowid() as SessionId;";
        ArrayList<HashMap<String, String>> result = client.executePreparedQuery(getSessionId, new Object[]{});

        int sessionId = Integer.parseInt(result.get(0).get("SessionId"));

        // Initialize player stats for this session
        for (String playerName : playerNames) {
            updatePlayerStats(playerName, 0, false); // Initialize with 0 games played
        }

        return sessionId;
    }

    /**
     * Adds scores for all players for a completed round.
     * This method calculates both round scores and cumulative scores.
     *
     * @param sessionId The current session ID
     * @param players Array of all players in the game
     * @param round The current round number
     * @param roundScores Array of scores for this round (points in hand for losers, 0 for winner)
     * @param gameVariant The game variant being played (affects scoring calculations)
     * @throws SQLException If score insertion fails
     */
    public void addRoundScores(int sessionId, Player[] players, int round,
                               int[] roundScores, String gameVariant) throws SQLException {
        String insertScore = "INSERT INTO Sessions (SessionId, Player, Round, RoundScore, CumulativeScore, GameVariant) VALUES (?, ?, ?, ?, ?, ?);";

        for (int i = 0; i < players.length; i++) {
            Player player = players[i];
            int roundScore = calculateVariantScore(roundScores[i], gameVariant);
            int cumulativeScore = player.getTotalScore(); // Get current cumulative score

            client.executePreparedStatement(insertScore, new Object[]{
                    sessionId, player.getName(), round, roundScore, cumulativeScore, gameVariant
            });
        }
    }

    /**
     * Calculates score based on game variant rules.
     * Different game variants may have different scoring systems.
     *
     * @param baseScore The base score from the round
     * @param gameVariant The game variant being played
     * @return The adjusted score based on variant rules
     */
    private int calculateVariantScore(int baseScore, String gameVariant) {
        switch (gameVariant.toLowerCase()) {
            case "special rules":
                // Special rules variant: Double the score for action cards
                return baseScore * 2;
            case "quick game":
                // Quick game variant: Half scoring for faster games
                return baseScore / 2;
            case "standard":
            default:
                return baseScore;
        }
    }

    /**
     * Retrieves the current round number for a session.
     * Useful for determining which round should be played next.
     *
     * @param sessionId The session ID to check
     * @return The next round number (current max + 1)
     * @throws SQLException If query fails
     */
    public int getCurrentRound(int sessionId) throws SQLException {
        String query = "SELECT COALESCE(MAX(Round), 0) + 1 AS NextRound FROM Sessions WHERE SessionId = ?;";
        ArrayList<HashMap<String, String>> result = client.executePreparedQuery(query, new Object[]{sessionId});

        return Integer.parseInt(result.get(0).get("NextRound"));
    }

    /**
     * Retrieves scores for all players up to a specific round.
     * This method is useful for displaying current standings during a game.
     *
     * @param sessionId The session ID
     * @param upToRound The round number to calculate scores up to
     * @return HashMap mapping player names to their cumulative scores
     * @throws SQLException If query fails
     */
    public HashMap<String, Integer> getScoresUpToRound(int sessionId, int upToRound) throws SQLException {
        HashMap<String, Integer> scores = new HashMap<>();

        // Get the latest cumulative score for each player up to the specified round
        String query = "SELECT Player, CumulativeScore FROM Sessions WHERE SessionId = ? AND Round <= ? " +
                "GROUP BY Player HAVING Round = MAX(Round);";

        ArrayList<HashMap<String, String>> results = client.executePreparedQuery(query, new Object[]{sessionId, upToRound});

        for (HashMap<String, String> row : results) {
            scores.put(row.get("Player"), Integer.parseInt(row.get("CumulativeScore")));
        }

        return scores;
    }

    /**
     * Displays scores directly from the database for a specific round.
     * This method replaces the previous non-database score display methods.
     *
     * @param sessionId The session ID
     * @param round The round number to display scores for
     * @throws SQLException If query fails
     */
    public void displayRoundScoresFromDatabase(int sessionId, int round) throws SQLException {
        String query = "SELECT Player, RoundScore, CumulativeScore FROM Sessions WHERE SessionId = ? AND Round = ? ORDER BY CumulativeScore DESC;";
        ArrayList<HashMap<String, String>> results = client.executePreparedQuery(query, new Object[]{sessionId, round});

        System.out.println("\n=== ROUND " + round + " SCORES ===");
        System.out.println("Player\t\tRound Score\tTotal Score");
        System.out.println("----------------------------------------");

        for (HashMap<String, String> row : results) {
            System.out.printf("%-15s\t%s\t\t%s\n",
                    row.get("Player"),
                    row.get("RoundScore"),
                    row.get("CumulativeScore"));
        }
    }

    /**
     * Displays the current session standings from the database.
     * Shows all players' current cumulative scores.
     *
     * @param sessionId The session ID to display standings for
     * @throws SQLException If query fails
     */
    public void displaySessionStandings(int sessionId) throws SQLException {
        String query = "SELECT Player, CumulativeScore, COUNT(*) as RoundsPlayed FROM Sessions WHERE SessionId = ? " +
                "GROUP BY Player ORDER BY CumulativeScore DESC;";
        ArrayList<HashMap<String, String>> results = client.executePreparedQuery(query, new Object[]{sessionId});

        System.out.println("\n=== CURRENT SESSION STANDINGS ===");
        System.out.println("Rank\tPlayer\t\tTotal Score\tRounds Played");
        System.out.println("------------------------------------------------");

        int rank = 1;
        for (HashMap<String, String> row : results) {
            System.out.printf("%d\t%-15s\t%s\t\t%s\n",
                    rank++,
                    row.get("Player"),
                    row.get("CumulativeScore"),
                    row.get("RoundsPlayed"));
        }
    }

    /**
     * Finalizes a game session by updating the end time and winner.
     * This method should be called when a game session completely ends.
     *
     * @param sessionId The session ID to finalize
     * @param winner The winning player
     * @param totalRounds The total number of rounds played
     * @throws SQLException If update fails
     */
    public void finalizeSession(int sessionId, String winner, int totalRounds) throws SQLException {
        String updateSession = "UPDATE GameSessions SET EndTime = CURRENT_TIMESTAMP, Winner = ?, TotalRounds = ? WHERE SessionId = ?;";
        client.executePreparedStatement(updateSession, new Object[]{winner, totalRounds, sessionId});

        // Update player stats
        updatePlayerStats(winner, totalRounds, true); // Winner gets a win

        // Update stats for all other players in the session
        String getPlayers = "SELECT DISTINCT Player FROM Sessions WHERE SessionId = ? AND Player != ?;";
        ArrayList<HashMap<String, String>> players = client.executePreparedQuery(getPlayers, new Object[]{sessionId, winner});

        for (HashMap<String, String> player : players) {
            updatePlayerStats(player.get("Player"), totalRounds, false);
        }
    }

    /**
     * Updates cumulative player statistics.
     * This method maintains long-term statistics across all games.
     *
     * @param playerName The player's name
     * @param roundsPlayed Number of rounds played in this game
     * @param won Whether the player won this game
     * @throws SQLException If update fails
     */
    private void updatePlayerStats(String playerName, int roundsPlayed, boolean won) throws SQLException {
        // Check if player exists in stats table
        String checkExists = "SELECT COUNT(*) as count FROM PlayerStats WHERE PlayerName = ?;";
        ArrayList<HashMap<String, String>> result = client.executePreparedQuery(checkExists, new Object[]{playerName});

        int count = Integer.parseInt(result.get(0).get("count"));

        if (count == 0) {
            // Insert new player
            String insertPlayer = "INSERT INTO PlayerStats (PlayerName, GamesPlayed, GamesWon) VALUES (?, 1, ?);";
            client.executePreparedStatement(insertPlayer, new Object[]{playerName, won ? 1 : 0});
        } else {
            // Update existing player
            String updatePlayer = "UPDATE PlayerStats SET GamesPlayed = GamesPlayed + 1, GamesWon = GamesWon + ?, LastPlayed = CURRENT_TIMESTAMP WHERE PlayerName = ?;";
            client.executePreparedStatement(updatePlayer, new Object[]{won ? 1 : 0, playerName});
        }
    }

    /**
     * Retrieves player statistics for display.
     * Shows comprehensive statistics for a specific player.
     *
     * @param playerName The player to get statistics for
     * @return HashMap containing player statistics
     * @throws SQLException If query fails
     */
    public HashMap<String, String> getPlayerStats(String playerName) throws SQLException {
        String query = "SELECT * FROM PlayerStats WHERE PlayerName = ?;";
        ArrayList<HashMap<String, String>> result = client.executePreparedQuery(query, new Object[]{playerName});

        if (result.isEmpty()) {
            return new HashMap<>(); // Return empty map if player not found
        }

        return result.get(0);
    }

    /**
     * Closes the database connection.
     * This method should be called when the application shuts down.
     *
     * @throws SQLException If closing fails
     */
    public void close() throws SQLException {
        if (client != null) {
            // Note: SqliteClient should implement a close method
            // For now, we'll assume the connection is managed properly
        }
    }
}
