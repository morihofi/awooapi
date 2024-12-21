# AwooAPI
![AwooAPI Logo](./media/AwooAPI-Logo.svg "Awoo APILogo")


AwooAPI is a modular and extensible framework for building Java applications with support for Java 21 and beyond. Inspired by the Javalin project, AwooAPI offers flexibility with interchangeable server implementations, not limited to Jetty—opening the door to potential support for servers like Netty and others.

## 🚨 ALPHA NOTICE 
THIS PROJECT IS CURRENTLY IN THE ALPHA STAGE OF DEVELOPMENT 🚧. 

THE API IS SUBJECT TO CHANGE AND IS NOT YET STABLE ⚠️. WE STRONGLY ADVISE AGAINST USING THIS PROJECT IN PRODUCTION ENVIRONMENTS 🚫. IT IS RECOMMENDED FOR EXPERIENCED DEVELOPERS AND TESTERS WHO ARE AWARE OF THE POTENTIAL RISKS AND LIMITATIONS 🤔. USE AT YOUR OWN RISK 🚨.

WE ARE AWARE THAT THE CURRENT DOCUMENTATION IS INCOMPLETE

## Key Features

- **Modular Design**: Multiple Maven modules for streamlined functionality.
- **Extensible Architecture**: Easily integrate custom servers and plugins.
- **Secure Applications**: Built-in cryptography tools for enhanced security.
- **Task Scheduling**: Scheduler plugin for executing functions using Crontab expressions.

## Maven Modules

AwooAPI consists of the following Maven modules, each serving a specific purpose:

| Module                           | Description                                                                                               |
|----------------------------------|-----------------------------------------------------------------------------------------------------------|
| **awooapi-annotation-processor** | Annotation processor for a quick start of the application.                                                |
| **awooapi-annotations**          | Collection of annotations used across the framework.                                                      |
| **awooapi-core**                 | Core framework components. This is the main component, include it always.                                 |
| **awooapi-cryptography**         | Cryptographic utility library, including X.509 certificates, key management, generators, and JWT support. |
| **awooapi-plugin-intf**          | General plugin interface for extending AwooAPI functionality.                                             |
| **awooapi-plugin-scheduler**     | Scheduler plugin for executing functions in classes based on Crontab expressions.                         |
| **awooapi-server-intf**          | General server interface to enable flexibility in server implementations.                                 |
| **awooapi-server-jetty**         | Jetty server module that implements the server interface.                                                 |

⚠️ Always use the same version across all modules

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven (for dependency management and building the project)

### Adding AwooAPI to Your Project

Include the necessary AwooAPI modules in your Maven `pom.xml` file. For example:

```xml
<dependency>
    <groupId>com.awooapi</groupId>
    <artifactId>awooapi-core</artifactId>
    <version>VERSION</version>
</dependency>
```

Add additional modules as required, such as `awooapi-server-jetty` or `awooapi-plugin-scheduler`.

## Example Usage

### Setting Up a Jetty Server

Here is a simple example of setting up a Jetty server using AwooAPI:

```java
import com.awooapi.server.jetty.JettyServer;
import com.awooapi.core.AwooApplication;

public class Main {
    public static void main(String[] args) {
        // Set our runtime configuration
        RuntimeConfiguration configuration = new RuntimeConfiguration();
        configuration.getSearchConfig().setClassLoader(MethodHandles.lookup().lookupClass().getClassLoader()); // classloader to search
        configuration.getSearchConfig().setPackagePrefix("net.fuxle.awooapi.sample"); // Your app package
        configuration.getDebugConfig().setDebugEnabled(Config.DEBUG); // Toggles on/off endpoint classes, that are only active when running in debug. Should be false in production
        configuration.getApiConfig().setGraphQLEnabled(false); // Enable GraphQL API
        configuration.getApiConfig().setRestEnabled(true); // Enable REST Endpoint registration

        AwooApplication application = new AwooApplication(new JettyWebServer(), configuration);
        configureCronJobs(application); // may enable some plugins, see below
        application.initialize(); // Initialize it
        
        application.start(8080); // Start on HTTP Port 8080
    }
}
```

## Configuring Cron Jobs (Scheduler Module)

The `awooapi-plugin-scheduler` module allows scheduling of tasks using Crontab expressions. To configure a cron job:

```java
private static void configureCronJobs(AwooApplication application) {
    // Register the CronJob Plugin
    application.getPluginManager().registerPlugin(CronJobPlugin.class, new CronJobPluginConfig(5).getParameter());
    CronJobPlugin pluginInstance = application.getPluginManager().getPluginInstanceByClass(CronJobPlugin.class).get();
    pluginInstance.addJob("*/1 * * * *", new CleanUserTableJob());
}

// ... 

public class CleanUserTableJob extends AbstractCronJob {
    @Override
    public void run() {
       System.out.println("IT WORKS!!! " + new Date());
    }
}
```

This example demonstrates registering a cron job plugin, configuring it with a job to run every minute, and implementing a custom job that prints a message to the console.

## Extensibility

AwooAPI allows developers to integrate custom server implementations by adhering to the `awooapi-server-intf`. Similarly, plugins can be developed by following the `awooapi-plugin-intf` guidelines, enabling developers to extend the framework's functionality.

## Cryptography Module

The `awooapi-cryptography` module provides a robust set of tools for secure application development, including:

- X.509 certificate management
- Key generation and management
- JSON Web Token (JWT) creation and validation

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for review.
