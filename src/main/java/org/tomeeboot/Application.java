package org.tomeeboot;

import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.tomeeboot.cdi.EnableCDI;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

/**
 * @author Gabriel Francisco - gabfssilva@gmail.com
 */
public class Application {
    private static void run(Archive<?> archive, int port, String appName) {
        Container container;

        try {
            Configuration configuration = new Configuration();
            String tomeeDir = Files.createTempDirectory("apache-tomee").toFile().getAbsolutePath();
            configuration.setDir(tomeeDir);
            configuration.setHttpPort(port);

            container = new Container();
            container.setup(configuration);

            final File app = new File(Files.createTempDirectory(appName).toFile().getAbsolutePath());
            app.deleteOnExit();

            File target = new File(app, appName + ".war");
            archive.as(ZipExporter.class).exportTo(target, true);
            container.start();

            container.deploy(appName, target);
            container.await();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        addShutdownHook(container);
    }

    private static void addShutdownHook(final Container container) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if (container != null) {
                        container.stop();
                    }
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    public static void run(String packagebase, String appName, int port, boolean enableCdi, Class<?> application) {
        Reflections reflections = new Reflections(packagebase,
                ClasspathHelper.forClass(Object.class),
                new SubTypesScanner(false), new TypeAnnotationsScanner());

        Set<Class<?>> typesOf = reflections.getSubTypesOf(Object.class);

        Class[] classes = typesOf.toArray(new Class[typesOf.size()]);
        WebArchive archive = getWebArchive(classes, enableCdi, application);
        run(archive, port, appName);
    }

    public static void run(Class<?> application) {
        ApplicationBoot applicationBoot = application.getAnnotation(ApplicationBoot.class);
        run(applicationBoot.basePackage(),
                applicationBoot.applicationName(),
                applicationBoot.port(),
                application.isAnnotationPresent(EnableCDI.class),
                application);
    }

    private static WebArchive getWebArchive(Class<?>[] classes, boolean enableCdi, Class<?> application) {
        WebArchive webArchive = ShrinkWrap
                .create(WebArchive.class)
                .addClasses(classes);

        loadResources(webArchive);
        loadWebResources(webArchive);
        enableCdi(enableCdi, webArchive);

        return webArchive;
    }

    private static void enableCdi(boolean enableCdi, WebArchive webArchive) {
        if (enableCdi) {
            webArchive.addAsWebInfResource(generateBeansDotXml());
        }
    }

    private static void loadWebResources(WebArchive webArchive) {
        File webapp = new File("src/main/webapp");

        if (webapp != null) {
            for (File f : webapp.listFiles()) {
                if (f.getName().equals("WEB-INF")) {
                    for (File file : f.listFiles()) {
                        webArchive.addAsWebInfResource(file);
                    }
                    continue;
                }

                webArchive.addAsWebResource(f);
            }
        }
    }

    private static void loadResources(WebArchive webArchive) {
        File resources = new File("src/main/resources");

        if (resources != null) {
            for (File f : resources.listFiles()) {
                webArchive.addAsResource(f);
            }
        }
    }

    private static File generateBeansDotXml() {
        try {
            File beans = new File("/tmp/beans.xml");
            beans.createNewFile();
            PrintWriter out = new PrintWriter(beans);
            out.print("<beans/>");
            out.close();
            return beans;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
