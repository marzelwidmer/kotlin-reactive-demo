package ch.keepcalm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

@WebFluxTest(PizzaService::class)
class KotlinReactiveServiceApplicationTests {

    @Autowired
    lateinit var service: PizzaService

    @MockBean
    lateinit var repo: PizzaRepository

    private val pizza1 = Pizza(id = "001-TEST-000", name = "Yummmi Yummmi Piazza")
    private val pizza2 = Pizza(id = "002-TEST-000", name = "Foo Piazza")
    private val pizza3 = Pizza(id = "003-TEST-000", name = "Bar Piazza")

    @BeforeEach
    fun testPizzas() {
        Mockito.`when`(repo.findAll()).thenReturn(Flux.just(pizza1, pizza2, pizza3))
        Mockito.`when`(repo.findById(pizza1.id)).thenReturn(Mono.just(pizza1))
        Mockito.`when`(repo.findById(pizza2.id)).thenReturn(Mono.just(pizza2))
        Mockito.`when`(repo.findById(pizza3.id)).thenReturn(Mono.just(pizza3))
    }

    @Test
    fun `Should throw an exception`() {
        val exception = assertThrows(UnsupportedOperationException::class.java) { throw UnsupportedOperationException("Not supported") }
        assertEquals(exception.message, "Not supported")
    }


    @RepeatedTest(value = 10)
    fun `Get all pizzas 🍕`() {
        StepVerifier.withVirtualTime { service.getAllPizzas() }
                .expectNext(pizza1)
                .expectNext(pizza2)
                .expectNext(pizza3)
                .verifyComplete()
    }

    @Test
    fun `Get a pizza 🍕 by its id`() {
        StepVerifier.withVirtualTime { service.getPizzaById(pizza1.id) }
                .expectNext(pizza1)
                .verifyComplete()
    }


    @Test
    fun `Get Orders for 🍕`() {
        StepVerifier.withVirtualTime { service.getOrdersForPizzaId(pizzaId = pizza1.id).take(10) }
                .thenAwait(Duration.ofHours(10))
                .expectNextCount(10)
                .verifyComplete()
    }

}
