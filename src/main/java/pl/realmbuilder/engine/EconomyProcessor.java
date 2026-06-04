package pl.realmbuilder.engine;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

import java.util.function.Consumer;

public class EconomyProcessor {

    // Nasz kanał komunikacyjny z oknem UI
    private Consumer<String> uiLogger;

    public void setLogger(Consumer<String> logger) {
        this.uiLogger = logger;
    }

    private void log(String message) {
        if (uiLogger != null) {
            uiLogger.accept(message);
        } else {
            System.out.println(message); // Awaryjnie do konsoli
        }
    }

    // wywołaj po produkcji budynków, przed końcem tury
    public void process(City city) {
        consumeFood(city);
        collectTaxes(city);
        updateMorale(city);
        city.incrementHighMoraleTurns();
    }

    // populacja konsumuje żywność każdą turę
    // 1 partia żywności na 10 mieszkańców
    private void consumeFood(City city) {
        int population = city.getResource(ResourceType.POPULATION);
        int consumed = Math.max(1, population / 10);
        int foodBefore = city.getResource(ResourceType.FOOD);

        city.subtractResource(ResourceType.FOOD, consumed);

        // jeśli zabrakło jedzenia - populacja spada i morale też
        if (foodBefore < consumed) {
            int shortage = consumed - foodBefore;
            int populationLoss = shortage / 2;
            city.subtractResource(ResourceType.POPULATION, populationLoss);
            city.subtractResource(ResourceType.MORALE, 5);

            // Bezpieczne emoji i wysyłka do okna gry
            log("❗ Głód! Brakuje " + shortage
                    + " żywności. Populacja spada o " + populationLoss + ".");
        }
    }

    // każdy mieszkaniec płaci podatek
    // 1 złoto na 20 mieszkańców na turę
    private void collectTaxes(City city) {
        int population = city.getResource(ResourceType.POPULATION);
        int taxes = Math.max(0, population / 20);
        if (taxes > 0) {
            city.addResource(ResourceType.GOLD, taxes);
        }
    }

    // morale dążą do 50 - spadają jeśli za wysokie,
    // rosną jeśli za niskie
    private void updateMorale(City city) {
        int morale = city.getResource(ResourceType.MORALE);
        if (morale > 80) {
            // bardzo wysokie morale są niestabilne
            city.subtractResource(ResourceType.MORALE, 1);
        } else if (morale < 30) {
            // bardzo niskie morale powoli się regenerują
            city.addResource(ResourceType.MORALE, 2);
        }
    }
}