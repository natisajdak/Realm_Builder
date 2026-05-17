package pl.realmbuilder.engine;

import pl.realmbuilder.annotations.Event;
import pl.realmbuilder.interfaces.GameEvent;
import pl.realmbuilder.model.Season;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EventLoader {

    private static final String EVENTS_PACKAGE = "pl.realmbuilder.events";

    public List<GameEvent> loadEventsForSeason(Season season) {
        List<GameEvent> events = new ArrayList<>();

        try {
            List<Class<?>> classes = getClassesFromPackage(EVENTS_PACKAGE);
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotationPresent(Event.class)) continue;
                if (!GameEvent.class.isAssignableFrom(clazz)) continue;
                if (clazz.isInterface()) continue;

                Event annotation = clazz.getAnnotation(Event.class);
                boolean seasonMatch = annotation.anyseason()
                        || annotation.season() == season;

                if (seasonMatch) {
                    GameEvent instance = (GameEvent)
                            clazz.getDeclaredConstructor().newInstance();
                    events.add(instance);
                }
            }
        } catch (Exception e) {
            System.err.println("[EventLoader] Błąd: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    public List<GameEvent> loadAllEvents() {
        List<GameEvent> events = new ArrayList<>();

        try {
            List<Class<?>> classes = getClassesFromPackage(EVENTS_PACKAGE);
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotationPresent(Event.class)) continue;
                if (!GameEvent.class.isAssignableFrom(clazz)) continue;
                if (clazz.isInterface()) continue;

                GameEvent instance = (GameEvent)
                        clazz.getDeclaredConstructor().newInstance();
                events.add(instance);
            }
        } catch (Exception e) {
            System.err.println("[EventLoader] Błąd: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    private List<Class<?>> getClassesFromPackage(String packageName)
            throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL resource = classLoader.getResource(path);
        if (resource == null) {
            System.err.println("[EventLoader] Nie znaleziono pakietu: " + path);
            return classes;
        }

        File dir;
        try {
            dir = new File(resource.toURI());
        } catch (URISyntaxException e) {
            dir = new File(resource.getPath());
        }

        if (!dir.exists()) {
            System.err.println("[EventLoader] Folder nie istnieje: "
                    + dir.getAbsolutePath());
            return classes;
        }

        File[] files = dir.listFiles();
        if (files == null) return classes;

        for (File file : files) {
            if (file.getName().endsWith(".class")
                    && !file.getName().contains("$")) {
                String className = packageName + '.'
                        + file.getName().replace(".class", "");
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    System.err.println("[EventLoader] Nie można załadować: "
                            + className);
                }
            }
        }
        return classes;
    }
}