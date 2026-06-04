package pl.realmbuilder.engine;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

import java.util.Map;
import java.util.function.Consumer;

public class TurnNarrator {

    private Consumer<String> uiLogger;

    public void setLogger(Consumer<String> logger) {
        this.uiLogger = logger;
    }

    private void log(String message) {
        if (uiLogger != null) {
            uiLogger.accept(message);
        } else {
            System.out.println(message);
        }
    }

    // narracja co 10 tur — klimat i fabuła
    private static final Map<Integer, String> TURN_NARRATIVES = Map.of(
            10, "📜 Pierwsze domy stoją, a w tawernie słychać śmiechy. Wędrowcy pytają o drogę do Twojego miasta.",
            20, "📜 Twoje miasto rozrosło się. Okoliczne wioski słyszą o jego chwale i wysyłają poselstwo z darami.",
            30, "📜 Kupcy z dalekich krain zaczęli odwiedzać Twój rynek. Sława Twojego miasta rośnie.",
            40, "📜 Połowa drogi za Tobą, władco. Rada Miasta składa Ci hołd — jesteś prawdziwym przywódcą.",
            50, "📜 Kronikarze zapisują Twoje czyny. Historycy będą o Tobie pisać przez wieki.",
            60, "📜 Trzy czwarte panowania za Tobą. Miasto tętni życiem, a mury są mocne jak Twoja wola.",
            70, "📜 Ostatnia prosta, władco. Legendy o Twoim mieście docierają do odległych królestw.",
            80, "📜 Dwadzieścia lat minęło. Twoje miasto przetrwało wszystkie próby. Historia zapamięta Twoje imię."
    );

    // narracja przy specjalnych warunkach zasobów
    public void narrateTurn(City city) {
        int turn = city.getCurrentTurn();

        // narracja co 10 tur
        if (TURN_NARRATIVES.containsKey(turn)) {
            log(TURN_NARRATIVES.get(turn));
        }

        // ostrzeżenia o zasobach
        narrateResourceWarnings(city);
    }

    private void narrateResourceWarnings(City city) {
        int gold = city.getResource(ResourceType.GOLD);
        int food = city.getResource(ResourceType.FOOD);
        int morale = city.getResource(ResourceType.MORALE);
        int population = city.getResource(ResourceType.POPULATION);

        if (gold < 50 && gold > 0) {
            log("⚠️  Skarbiec świeci pustkami. Zbuduj Rynek lub podbij podatki.");
        }
        if (food < 30) {
            log("⚠️  Magazyny żywności są prawie puste. Zbuduj Farmę zanim zacznie się głód.");
        }
        if (morale < 25) {
            log("⚠️  Mieszkańcy są niezadowoleni. Grozi bunt! Zbuduj Kościół lub zorganizuj festiwal.");
        }
        if (population < 20) {
            log("⚠️  Miasto wyludnia się. Zbuduj Domy żeby przyciągnąć nowych mieszkańców.");
        }
    }

    // narracja po zdarzeniu
    public void narrateEvent(String eventName, boolean negative) {
        if (negative) {
            log("💬 Rada Miasta: \"Władco, musimy działać! " + eventName + " zagraża naszemu miastu!\"");
        } else {
            log("💬 Rada Miasta: \"" + eventName + " — to dobry omen dla naszego miasta!\"");
        }
    }

    // narracja po użyciu doradcy
    public void narrateAdvisorAction(String advisorName, String quote) {
        log("💬 " + advisorName + ": \"" + quote + "\"");
    }
}