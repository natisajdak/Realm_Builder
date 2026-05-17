package pl.realmbuilder.engine;

import pl.realmbuilder.annotations.Building;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BuildingLoader {

    private static final String BUILDINGS_PACKAGE = "pl.realmbuilder.buildings";

    public List<pl.realmbuilder.interfaces.Building> loadBuildings() {
        List<pl.realmbuilder.interfaces.Building> buildings = new ArrayList<>();

        try {
            List<Class<?>> classes = getClassesFromPackage(BUILDINGS_PACKAGE);
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotationPresent(Building.class)) continue;
                if (!pl.realmbuilder.interfaces.Building.class
                        .isAssignableFrom(clazz)) continue;
                if (clazz.isInterface()) continue;

                pl.realmbuilder.interfaces.Building instance =
                        (pl.realmbuilder.interfaces.Building)
                                clazz.getDeclaredConstructor().newInstance();
                buildings.add(instance);
                System.out.println("[BuildingLoader] Załadowano: "
                        + clazz.getAnnotation(Building.class).name());
            }
        } catch (Exception e) {
            System.err.println("[BuildingLoader] Błąd: " + e.getMessage());
            e.printStackTrace();
        }

        return buildings;
    }

    private List<Class<?>> getClassesFromPackage(String packageName)
            throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL resource = classLoader.getResource(path);
        if (resource == null) {
            System.err.println("[BuildingLoader] Nie znaleziono pakietu: " + path);
            return classes;
        }

        File dir;
        try {
            dir = new File(resource.toURI());
        } catch (URISyntaxException e) {
            dir = new File(resource.getPath());
        }

        if (!dir.exists()) {
            System.err.println("[BuildingLoader] Folder nie istnieje: "
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
                    System.err.println("[BuildingLoader] Nie można załadować: "
                            + className);
                }
            }
        }
        return classes;
    }
}