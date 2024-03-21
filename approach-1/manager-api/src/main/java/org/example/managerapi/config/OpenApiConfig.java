package org.example.managerapi.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI openApiInformation() {
        Server localServer = new Server()
                .url("http://localhost:9999")
                .description("Localhost Server URL");

        Contact contact = new Contact()
                .name("Sriraj Kadimisetty");

        Info info = new Info()
                .contact(contact)
                .description("Manager API")
                .summary("Manager API")
                .title("Manager API")
                .version("V1.0.0");

        return new OpenAPI().info(info).addServersItem(localServer);
    }

}
