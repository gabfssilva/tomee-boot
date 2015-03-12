# tomee-boot
TomEE Boot

Run your applications in a Java EE embedded container!
TomEE boot just zip your classes into a war and deploy it into an embedded TomEE, you just need to run an Appplication.run(MyAppClass.class) and in five seconds your Java EE app is up!

Usage:

```java
@ApplicationBoot(basePackage = "com.your.package", port = 8888, applicationName = "your-app-name")
@EnableCDI
@Path("/rs/myendpoint")
@Stateless
public class MyEndpoint{

    @Inject
    @Named("message")
    private String message;

    @GET
    public Response response() {
        return Response.ok(message).build();
    }

    public static void main(String[] args) {
        Application.run(MyEndpoint.class);
    }
}
```

Maven:

```xml
<project>
   ...
   <repositories>
        <repository>
            <id>tomee-boot-repo</id>
            <name>TomEE boot</name>
            <url>http://dl.bintray.com/gabfssilva/maven</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.tomeeboot</groupId>
            <artifactId>tomee-boot</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
    ...
</project>
```

Gradle:

```groovy
repositories {
    ...

    maven{
        url 'http://dl.bintray.com/gabfssilva/maven'
    }
}

dependencies {
    compile 'org.tomeeboot:tomee-boot:1.0.0'
}
```