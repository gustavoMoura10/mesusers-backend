package com.br.mesusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App {
    public static void main(String... args) {
        SpringApplication.run(App.class, args);
    }

}
@RestController
class IndexController {

    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }
}