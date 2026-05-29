package pl.realmbuilder.engine;

import pl.realmbuilder.model.City;
import pl.realmbuilder.model.ResourceType;

import java.util.Map;

public class TurnNarrator {

    // narracja co 10 tur — klimat i fabuła
    private static final Map<Integer, String> TURN_NARRATIVES = Map.of(
            10, "📜 Pierwsze domy stoją, a w tawernie słychać śmiechy. " +
                    "Wędrowcy pytają o drogę do Twojego miasta.",
            20, "📜 Twoje miasto rozrosło się. Okoliczne wioski słyszą " +
                    "o jego chwale i wysyłają poselstwo z darami.",
            30, "📜 Kupcy z dalekich krain zaczęli odwiedzać Twój rynek. " +
                    "Sława Twojego miasta rośnie.",
            40, "📜 Połowa drogi za Tobą, władco. Rada Miasta składa Ci " +
                    "hołd — jesteś prawdziwym przywódcą.",
            50, "📜 Kronikarze zapisują Twoje czyny. Historycy będą " +
                    "o Tobie pisać przez wieki.",
            60, "📜 Trzy czwarte panowania za Tobą. Miasto tętni życiem, " +
                    "a mury są mocne jak Twoja wola.",
            70, "📜 Ostatnia prosta, władco. Legendy o Twoim mieście " +
                    "docierają do odległych królestw.",
            80, "📜 Dwadzieścia lat minęło. Twoje miasto przetrwało " +
                    "wszystkie próby. Historia zapamięta Twoje imię."
    );

    // narracja przy specjalnych warunkach zasobów
    public void narrateTurn(City city) {
        int turn = city.getCurrentTurn();

        // narracja co 10 tur
        if (TURN_NARRATIVES.containsKey(turn)) {
            System.out.println();
            System.out.println(TURN_NARRATIVES.get(turn));
            System.out.println();
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
            System.out.println("⚠️  Skarbiec świeci pustkami. " +
                    "Zbuduj Rynek lub podbij podatki.");
        }
        if (food < 30) {
            System.out.println("⚠️  Magazyny żywności są prawie puste. " +
                    "Zbuduj Farmę zanim zacznie się głód.");
        }
        if (morale < 25) {
            System.out.println("⚠️  Mieszkańcy są niezadowoleni. " +
                    "Grozi bunt! Zbuduj Kościół lub zorganizuj festiwal.");
        }
        if (population < 20) {
            System.out.println("⚠️  Miasto wyludnia się. " +
                    "Zbuduj Domy żeby przyciągnąć nowych mieszkańców.");
        }
    }

    // narracja po zdarzeniu
    public void narrateEvent(String eventName, boolean negative) {
        if (negative) {
            System.out.println("💬 Rada Miasta: \"Władco, musimy działać! "
                    + eventName + " zagraża naszemu miastu!\"");
        } else {
            System.out.println("💬 Rada Miasta: \""
                    + eventName + " — to dobry omen dla naszego miasta!\"");
        }
    }

    // narracja po użyciu doradcy
    public void narrateAdvisorAction(String advisorName, String quote) {
        System.out.println("💬 " + advisorName + ": \"" + quote + "\"");
    }
}