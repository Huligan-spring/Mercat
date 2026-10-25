package com.curso.mercat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requiere base de datos PostgreSQL activa con variables de entorno configuradas")
class MercatApplicationTests {

    @Test
    void contextLoads() {
    }

}
