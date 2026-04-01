package com.splendor.data;

import com.splendor.config.ConfigKeys;
import com.splendor.config.IConfigProvider;
import com.splendor.exception.DataLoadException;
import com.splendor.model.Card;
import com.splendor.model.Gem;
import com.splendor.model.Noble;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses card and noble data from a CSV file.
 *
 * <p>Expected CSV schema (columnar cost format):
 * <pre>
 * type,id,tier,points,bonus_gem,cost_black,cost_white,cost_red,cost_blue,cost_green
 * CARD,1,1,0,BLACK,0,1,1,1,1
 * NOBLE,1,3,0,0,4,0,4
 * </pre>
 *
 * <p>The file path is read from {@link ConfigKeys#FILE_CARDS_DATA}.
 * Parsing is fail-fast: any malformed row throws a {@link DataLoadException}.
 */
public class CsvCardParser {
    
    private static final String CSV_TYPE_CARD = "CARD";
    private static final String CSV_TYPE_NOBLE = "NOBLE";
    private static final String CSV_HEADER_TYPE = "type";
    private static final String CSV_HEADER_ID = "id";
    private static final String CSV_HEADER_POINTS = "points";
    private static final Gem[] CARD_COST_COLUMNS  = { Gem.BLACK, Gem.WHITE, Gem.RED, Gem.BLUE, Gem.GREEN };
    private static final Gem[] NOBLE_REQ_COLUMNS  = { Gem.BLACK, Gem.WHITE, Gem.RED, Gem.BLUE, Gem.GREEN };
    
    private final IConfigProvider configProvider;
    private final List<Card> allCards;
    private final List<Noble> allNobles;
    
    /**
     * Creates a new CSV card parser with the specified configuration.
     * 
     * @param configProvider Configuration provider for file paths and settings
     * @throws DataLoadException if CSV data cannot be loaded or parsed
     */
    public CsvCardParser(final IConfigProvider configProvider) throws DataLoadException {
        this.configProvider = configProvider;
        this.allCards = new ArrayList<>();
        this.allNobles = new ArrayList<>();
        loadData();
    }
    
    /**
     * Returns a shuffled list of cards for the given tier, duplicating base cards if
     * needed to reach the configured target count.
     *
     * @param tier Card tier (1, 2, or 3)
     * @return Shuffled list of cards for the tier
     * @throws IllegalArgumentException if tier is not 1–3
     */
    public List<Card> loadCards(final int tier) {
        if (tier < 1 || tier > 3) {
            throw new IllegalArgumentException("Invalid tier: " + tier + ". Must be 1, 2, or 3.");
        }
        
        final List<Card> tierCards = new ArrayList<>();
        for (final Card card : allCards) {
            if (card.getTier() == tier) {
                tierCards.add(card);
            }
        }
        
        // Duplicate cards if needed to reach target count
        final int targetCount = getTargetCardCount(tier);
        if (tierCards.size() < targetCount && !tierCards.isEmpty()) {
            final List<Card> base = new ArrayList<>(tierCards);
            int nextId = tierCards.stream().mapToInt(Card::getId).max().orElse(0) + 1;
            for (int i = 0; tierCards.size() < targetCount; i++) {
                final Card baseCard = base.get(i % base.size());
                tierCards.add(new Card(nextId++, tier, baseCard.getPoints(), 
                                     baseCard.getBonusGem(), baseCard.getCost()));
            }
        }
        
        Collections.shuffle(tierCards);
        return tierCards;
    }
    
    /**
     * Returns a shuffled list of all nobles loaded from the CSV file.
     *
     * @return Shuffled list of nobles
     */
    public List<Noble> loadNobles() {
        final List<Noble> nobles = new ArrayList<>(allNobles);
        Collections.shuffle(nobles);
        return nobles;
    }
    
    /**
     * Loads all card and noble data from the CSV file.
     * 
     * @throws DataLoadException if the file cannot be read or parsed
     */
    private void loadData() throws DataLoadException {
        final String csvFilePath = configProvider.getStringProperty(ConfigKeys.FILE_CARDS_DATA, null);
        if (csvFilePath == null) {
            throw new DataLoadException("Missing config key: " + ConfigKeys.FILE_CARDS_DATA);
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int lineNumber = 0;
            boolean headerProcessed = false;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Skip header line
                if (!headerProcessed && line.toLowerCase().startsWith("type")) {
                    headerProcessed = true;
                    validateHeader(line);
                    continue;
                }
                
                parseCsvRow(line, lineNumber);
            }
        } catch (final IOException e) {
            throw new DataLoadException("Failed to read CSV file: " + csvFilePath, e);
        }
        
        if (allCards.isEmpty()) {
            throw new DataLoadException("No cards found in CSV file: " + csvFilePath);
        }
    }
    
    /**
     * Validates the CSV header format.
     * 
     * @param headerLine The first line of the CSV file
     * @throws DataLoadException if required headers are missing
     */
    private void validateHeader(final String headerLine) throws DataLoadException {
        final String[] headers = headerLine.split(",");
        final Map<String, Boolean> requiredHeaders = new HashMap<>();
        requiredHeaders.put(CSV_HEADER_TYPE, false);
        requiredHeaders.put(CSV_HEADER_ID, false);
        requiredHeaders.put(CSV_HEADER_POINTS, false);
        
        for (final String header : headers) {
            final String normalized = header.trim().toLowerCase();
            if (requiredHeaders.containsKey(normalized)) {
                requiredHeaders.put(normalized, true);
            }
        }
        
        for (final Map.Entry<String, Boolean> entry : requiredHeaders.entrySet()) {
            if (!entry.getValue()) {
                throw new DataLoadException("Missing required CSV header: " + entry.getKey());
            }
        }
    }
    
    /**
     * Parses a single CSV row into a Card or Noble.
     * 
     * @param line The CSV line to parse
     * @param lineNumber The line number for error reporting
     * @throws DataLoadException if the row is malformed or contains invalid data
     */
    private void parseCsvRow(final String line, final int lineNumber) throws DataLoadException {
        final String[] parts = line.split(",");
        
        if (parts.length < 4) {
            throw new DataLoadException(String.format(
                "Line %d: Too few columns (%d minimum required). Line: %s", 
                lineNumber, 4, line));
        }
        
        final String type = parts[0].trim().toUpperCase();
        
        try {
            switch (type) {
                case CSV_TYPE_CARD:
                    allCards.add(parseCardRow(parts, lineNumber));
                    break;
                case CSV_TYPE_NOBLE:
                    allNobles.add(parseNobleRow(parts, lineNumber));
                    break;
                default:
                    throw new DataLoadException(String.format(
                        "Line %d: Unknown type '%s'. Expected CARD or NOBLE.", lineNumber, type));
            }
        } catch (final NumberFormatException e) {
            throw new DataLoadException(String.format(
                "Line %d: Invalid numeric value. %s", lineNumber, e.getMessage()), e);
        } catch (final IllegalArgumentException e) {
            throw new DataLoadException(String.format(
                "Line %d: %s", lineNumber, e.getMessage()), e);
        }
    }
    
    /**
     * Parses a CSV row into a Card object.
     * 
     * @param parts CSV column values
     * @param lineNumber Line number for error reporting
     * @return Parsed Card object
     * @throws DataLoadException if parsing fails
     */
    private Card parseCardRow(final String[] parts, final int lineNumber) throws DataLoadException {
        final int minCols = 5 + CARD_COST_COLUMNS.length;
        if (parts.length < minCols) {
            throw new DataLoadException(String.format(
                "Line %d: CARD requires at least %d columns, found %d", lineNumber, minCols, parts.length));
        }

        final int id = Integer.parseInt(parts[1].trim());
        final int tier = Integer.parseInt(parts[2].trim());
        final int points = Integer.parseInt(parts[3].trim());
        final String bonusGemStr = parts[4].trim();

        if (tier < 1 || tier > 3) {
            throw new DataLoadException(String.format(
                "Line %d: Invalid tier %d. Must be 1, 2, or 3.", lineNumber, tier));
        }

        if (points < 0 || points > 5) {
            throw new DataLoadException(String.format(
                "Line %d: Invalid points %d. Must be 0-5.", lineNumber, points));
        }

        final Gem bonusGem = bonusGemStr.isEmpty() ? null : Gem.valueOf(bonusGemStr.toUpperCase());
        final Map<Gem, Integer> cost = parseCostColumns(parts, 5, CARD_COST_COLUMNS, lineNumber);

        return new Card(id, tier, points, bonusGem, cost);
    }
    
    /**
     * Parses a CSV row into a Noble object.
     * 
     * @param parts CSV column values
     * @param lineNumber Line number for error reporting
     * @return Parsed Noble object
     * @throws DataLoadException if parsing fails
     */
    private Noble parseNobleRow(final String[] parts, final int lineNumber) throws DataLoadException {
        final int minCols = 3 + NOBLE_REQ_COLUMNS.length;
        if (parts.length < minCols) {
            throw new DataLoadException(String.format(
                "Line %d: NOBLE requires at least %d columns, found %d", lineNumber, minCols, parts.length));
        }

        final int id = Integer.parseInt(parts[1].trim());
        final int points = Integer.parseInt(parts[2].trim());

        if (points != 3) {
            throw new DataLoadException(String.format(
                "Line %d: Noble points must be 3, found %d.", lineNumber, points));
        }

        final Map<Gem, Integer> requirements = parseCostColumns(parts, 3, NOBLE_REQ_COLUMNS, lineNumber);

        return new Noble(id, points, requirements);
    }

    private Map<Gem, Integer> parseCostColumns(final String[] parts, final int startCol,
            final Gem[] gems, final int lineNumber) throws DataLoadException {
        final Map<Gem, Integer> cost = new HashMap<>();
        for (int i = 0; i < gems.length; i++) {
            final int col = startCol + i;
            if (col >= parts.length) break;
            final String val = parts[col].trim();
            if (val.isEmpty()) continue;
            final int amount = Integer.parseInt(val);
            if (amount < 0) {
                throw new DataLoadException(String.format(
                    "Line %d: Negative gem amount for %s: %d", lineNumber, gems[i], amount));
            }
            if (amount > 0) {
                cost.put(gems[i], amount);
            }
        }
        return cost;
    }

    /**
     * Gets the target card count for a specific tier from configuration.
     * 
     * @param tier The card tier (1, 2, or 3)
     * @return Target number of cards for this tier
     */
    private int getTargetCardCount(final int tier) {
        return switch (tier) {
            case 1 -> configProvider.getIntProperty(ConfigKeys.TIER1_CARD_COUNT, 40);
            case 2 -> configProvider.getIntProperty(ConfigKeys.TIER2_CARD_COUNT, 30);
            case 3 -> configProvider.getIntProperty(ConfigKeys.TIER3_CARD_COUNT, 20);
            default -> 0;
        };
    }
}
