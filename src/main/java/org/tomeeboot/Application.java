package org.tomeeboot;

import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import java.io.File;
import java.io.PrintWriter;
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

    public static void run(String packagebase, String appName, int port, boolean enableCdi) {
        Reflections reflections = new Reflections(packagebase,
                ClasspathHelper.forClass(Object.class),
                new SubTypesScanner(false));

        Set<Class<?>> typesOf = reflections.getSubTypesOf(Object.class);
        Class[] classes = typesOf.toArray(new Class[typesOf.size()]);
        WebArchive archive = getWebArchive(classes, enableCdi);
        run(archive, port, appName);
    }

    public static void run(Class<?> application) {
        ApplicationBoot applicationBoot = application.getAnnotation(ApplicationBoot.class);
        run(applicationBoot.basePackage(), applicationBoot.applicationName(), applicationBoot.port(), application.isAnnotationPresent(EnableCDI.class));
    }

    private static WebArchive getWebArchive(Class<?>[] classes, boolean enableCdi) {
        WebArchive webArchive = ShrinkWrap
                .create(WebArchive.class)
                .addClasses(classes);
        if (enableCdi) {
            webArchive.addAsWebInfResource(generateBeansDotXml());
        }

        return webArchive;
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
