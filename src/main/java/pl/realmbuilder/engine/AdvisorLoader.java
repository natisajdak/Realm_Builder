package pl.realmbuilder.engine;

import pl.realmbuilder.annotations.AdvisorPlugin;
import pl.realmbuilder.interfaces.Advisor;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdvisorLoader {

    private static final String ADVISORS_PACKAGE = "pl.realmbuilder.advisors";

    public List<Advisor> loadAdvisors() {
        List<Advisor> advisors = new ArrayList<>();

        try {
            List<Class<?>> classes = getClassesFromPackage(ADVISORS_PACKAGE);
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotationPresent(AdvisorPlugin.class)) continue;
                if (!Advisor.class.isAssignableFrom(clazz)) continue;
                if (clazz.isInterface()) continue;

                Advisor instance = (Advisor)
                        clazz.getDeclaredConstructor().newInstance();
                advisors.add(instance);
                System.out.println("[AdvisorLoader] Załadowano doradcę: "
                        + clazz.getAnnotation(AdvisorPlugin.class).name());
            }
        } catch (Exception e) {
            System.err.println("[AdvisorLoader] Błąd: " + e.getMessage());
            e.printStackTrace();
        }

        return advisors;
    }

    private List<Class<?>> getClassesFromPackage(String packageName)
            throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL resource = classLoader.getResource(path);
        if (resource == null) {
            System.err.println("[AdvisorLoader] Nie znaleziono pakietu: " + path);
            return classes;
        }

        File dir;
        try {
            dir = new File(resource.toURI());
        } catch (URISyntaxException e) {
            dir = new File(resource.getPath());
        }

        if (!dir.exists()) {
            System.err.println("[AdvisorLoader] Folder nie istnieje: "
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
                    System.err.println("[AdvisorLoader] Nie można załadować: "
                            + className);
                }
            }
        }
        return classes;
    }
}