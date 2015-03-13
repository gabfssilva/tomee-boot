# tomee-boot
TomEE Boot

Run your applications in a Java EE embedded container!
TomEE boot just zip your classes into a war and deploy it into an embedded TomEE, you just need to run an Appplication.run(MyAppClass.class) and in five seconds your Java EE app is up!

##Usage:

```java
@ApplicationBoot(basePackage = "com.your.package", port = 8888, applicationName = "your-app-name")
@EnableCDI
@Path("/rs/myendpoint")
@Stateless
public class MyEndpoint{

    @Inject
    @Named("message")
    private String message;

    @Inject
    private SomeEJB someEjb;

    @EJB
    private AnotherEJB anotherEjb;

    @GET
    public Response response() {
        someEjb.execute();
        anotherEjb.do();

        return Response.ok(message).build();
    }

    public static void main(String[] args) {
        Application.run(MyEndpoint.class);
    }
}
```


##Creating resources

You can create resources using the TomEE resources.xml file:

e.g data source creation

src/main/webapp/WEB-INF/resources.xml
```xml
<resources>
    <Resource id="myDataSource" type="javax.sql.DataSource">
        jdbcDriver = org.h2.Driver
        jdbcUrl = jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
        jtaManaged = true
        maxActive = 20
        maxIdle = 20
        maxOpenPreparedStatements = 0
        maxWaitTime = -1 millisecond
        minEvictableIdleTime = 30 minutes
        minIdle = 0
        numTestsPerEvictionRun = 3
        password =
        testOnBorrow = true
        userName = sa
    </Resource>
</resources>
```

And, use it wherever you want, for instance, JPA.

src/main/resources/META-INF/persistence.xml:

```xml
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
             version="2.0">
    <persistence-unit name="sample" transaction-type="JTA">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <jta-data-source>myDataSource</jta-data-source>
        <properties>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
        </properties>
    </persistence-unit>
</persistence>
```

You can see more TomEE configurations at:

http://tomee.apache.org/examples-trunk/resources-declared-in-webapp/README.html
http://tomee.apache.org/examples-trunk/index.html
http://tomee.apache.org/containers-and-resources.html
http://tomee.apache.org/datasource-config.html


#Dependency

##Maven:

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

##Gradle:

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