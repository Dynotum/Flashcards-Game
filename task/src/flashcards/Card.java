package flashcards;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Card {

    final Scanner sc = new Scanner(System.in);
    Map<String, String> card = new HashMap<>();
    final Map<String, Integer> cardMap = new HashMap<>();
    final List<String> logger = new LinkedList<>();
    String exporterName = "";

    public Card(String[] args) {

        if (args.length > 1) {
            Map<String, String> mapArgs = getArgs(args);

            if (mapArgs.containsKey("-import")) {
                importFile(mapArgs.get("-import"), false);
            }

            if (mapArgs.containsKey("-export")) {
                this.exporterName = mapArgs.get("-export");
            }
        }
    }

    private boolean containsDefCard(String defCard) {
        return card.containsValue(defCard);
    }

    private boolean containsNameCard(String nameCard) {
        return card.containsKey(nameCard);
    }

    private void replaceDefCard(String nameCard, String newDefCard, int hc) {
        if (containsNameCard(nameCard)) {
            card.replace(nameCard, newDefCard);
            cardMap.replace(nameCard, hc);
        }
    }

    private Map<String, String> getArgs(String[] args) {
        // -import 1arg and/or -export 1arg
        Map<String, String> mapArgs = new HashMap();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-import") || args[i].equals("-export")) {
                mapArgs.put(args[i], args[++i]);
            }
        }
        return mapArgs;
    }


    public void startGame() {
        String action;

        do {
            final String init = "\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):";
            logger.add(init);
            System.out.println(init);
            action = sc.nextLine();

            doAction(action);


        } while (!action.equalsIgnoreCase("exit"));
        bye();


    }

    private void doAction(String action) {

        switch (action) {
            case "add":
                addCard();
                break;
            case "remove":
                removeCard();
                break;
            case "import":
                importFile();
                break;
            case "export":
                exportFile();
                break;
            case "ask":
                askInit();
                break;
            case "log":
                log();
                break;
            case "hardest card":
                hardestCard();
                break;
            case "reset stats":
                resetStats();
                break;
            default:
                break;

        }
    }

    private void log() {
        logger.add("File name:");
        System.out.println("File name:");
        final String nameFile = sc.nextLine();

        try (var file = new FileWriter(nameFile)) {
            file.write(logger.toString());
            System.out.println("The log has been saved.");
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void resetStats() {
        for (Map.Entry<String, Integer> maps : cardMap.entrySet()) {
            cardMap.replace(maps.getKey(), 0);
        }
        logger.add("Cards statistics has been reset.");
        System.out.println("Cards statistics has been reset.");
    }

    private void hardestCard() {
        if (cardMap.isEmpty()) {
            logger.add("There are no cards with errors.");
            System.out.println("There are no cards with errors.");
            return;
        }

        final Optional<Map.Entry<String, Integer>> maxN = cardMap.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue));
        final int maxNUmber = maxN.get().getValue();

        if (maxNUmber > 0) {
            final List sameKeyCards = cardMap.entrySet().stream()
                    .filter(f -> f.getValue() == maxNUmber)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (sameKeyCards.size() > 1) {
                final List<String> moreHC = new ArrayList<>();
                for (Object key : sameKeyCards) {
                    moreHC.add("\"" + key + "\"");
                }
                final String namesCards = String.join(",", moreHC);
                final String print = "The hardest cards are " + namesCards + ". You have " + maxNUmber + " errors answering them.";
                logger.add(print);
                System.out.println(print);

            } else {
                final String print = "The hardest card is \"" + sameKeyCards.get(0) + "\". You have " + maxNUmber + " errors answering it.";
                logger.add(print);
                System.out.println(print);
            }
        } else {
            final String print = "There are no cards with errors.";
            logger.add(print);
            System.out.println(print);
        }
    }

    private void importFile(String nameFile, boolean print) {
        final File file = new File(nameFile);
        if (file.exists()) {
            try (var isr = new InputStreamReader(new FileInputStream(file));
                 var br = new BufferedReader(isr)) {

                String line;
                int numberLines = 0;
                while ((line = br.readLine()) != null) {
                    numberLines++;
                    // Paris:France:numberHC
                    final String[] splitLine = line.split(":");
                    final String key = splitLine[0];
                    final String value = splitLine[1];
                    final int hardestCard = Integer.parseInt(splitLine[2]);
                    // check if the key exists
                    // if true - take new value
                    // otherwise add to the map
                    if (containsNameCard(key)) {
                        replaceDefCard(key, value, hardestCard);
//                        replaceDefCard(key, value);
                    } else {
                        card.put(key, value);
                        cardMap.put(key, hardestCard);
                    }
                    if (print) {
                        System.out.println(line);
                    }
                }
                logger.add(numberLines + " cards have been loaded.");
                System.out.println(numberLines + " cards have been loaded");
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        } else {
            logger.add("File not found.");
            System.out.println("File not found.");
        }
    }

    private void importFile() {
        logger.add("File name:");
        System.out.println("File name:");
        final String nameFile = sc.nextLine();

        importFile(nameFile, true);
    }

    private void exportFile() {
        logger.add("File name:");
        System.out.println("File name:");
        final String nameFile = sc.nextLine();
        exportFile(nameFile);
    }

    private void exportFile(String nameFile) {
        final StringBuilder content = new StringBuilder();

        try (var file = new FileWriter(nameFile)) {
            card.forEach((k, v) -> content.append(k).append(":").append(v).append(":").append(cardMap.get(k)).append("\n"));
            file.write(content.toString());
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        logger.add(card.size() + " cards have been saved.");
        System.out.println(card.size() + " cards have been saved.");
    }

    private void askInit() {
        final int timesAsk;
        logger.add("How many times to ask?");
        System.out.println("How many times to ask?");
        timesAsk = Integer.parseInt(sc.nextLine());

        for (int i = 0; i < timesAsk; i++) {
            Random random = new Random();
            final int randomCard = random.nextInt(card.size());
            final String keyCard = card.keySet().toArray()[randomCard].toString();

            logger.add("Print the definition of \"" + keyCard + "\":");
            System.out.println("Print the definition of \"" + keyCard + "\":");
            final String result = sc.nextLine();

            if (result.equals(card.get(keyCard))) {
                logger.add("Correct answer.");
                System.out.println("Correct answer.");
            } else {
                logger.add(getBadAnswer(card, result, card.get(keyCard)));
                System.out.println(getBadAnswer(card, result, card.get(keyCard)));
                cardMap.replace(keyCard, cardMap.get(keyCard) + 1);
            }
        }
    }

    private void removeCard() {
        logger.add("The card:");
        System.out.println("The card:");
        final String dropCard = sc.nextLine();

        if (card.containsKey(dropCard)) {
            card.remove(dropCard);
            logger.add("The card has been removed.");
            System.out.println("The card has been removed.");
            cardMap.remove(dropCard);
        } else {
            logger.add("Can't remove \"" + dropCard + "\": there is no such card.");
            System.out.println("Can't remove \"" + dropCard + "\": there is no such card.");
        }
    }

    private void addCard() {
        logger.add("The card:");
        System.out.println("The card:");
        final String nameCard = sc.nextLine();

        if (containsNameCard(nameCard)) {
            logger.add("The card \"" + nameCard + "\" already exists.");
            System.out.println("The card \"" + nameCard + "\" already exists.");
            return;
        }

        logger.add("The definition of the card:");
        System.out.println("The definition of the card:");
        final String defCard = sc.nextLine();

        if (containsDefCard(defCard)) {
            logger.add("The definition \"" + defCard + "\" already exists.");
            System.out.println("The definition \"" + defCard + "\" already exists.");
            return;
        }
        logger.add("The pair (\"" + nameCard + "\":\"" + defCard + "\") has been added.");
        System.out.println("The pair (\"" + nameCard + "\":\"" + defCard + "\") has been added.");
        card.put(nameCard, defCard);
        cardMap.put(nameCard, 0);

    }

    private void bye() {
        logger.add("Bye bye!");
        System.out.println("Bye bye!");
        if (!exporterName.equals("")) {
            exportFile(exporterName);
        }
        System.exit(1);
    }

    public String getBadAnswer(Map<String, String> flashcard, String result, String entryValue) {
        String value = flashcard.entrySet().stream().filter(x -> result.equals(x.getValue())).map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (value != null) {
            return "Wrong answer. The correct one is \"" + entryValue + "\", you've just written the definition of \"" + value + "\"";
        }
        return "Wrong answer. The correct one is \"" + entryValue + "\".";
    }

}