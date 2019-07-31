package ch.keepcalm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
class KotlinReactiveServiceApplication

fun main(args: Array<String>) {
    runApplication<KotlinReactiveServiceApplication>(*args)
}

@RestController
@RequestMapping(value = ["/api/pizzas"])
class PizzaController(private val service: PizzaService){

    //  http :8080/api/pizzas
    @GetMapping
    fun all() = service.getAllPizzas()

    //  http :8080/api/pizzas/f162dd0b-81af-4bbe-a4fc-a9e97181e201
    @GetMapping(value = ["/{id}"])
    fun byId(@PathVariable id: String) = service.getPizzaById(id = id)

    // http -S  :8080/api/pizzas/f162dd0b-81af-4bbe-a4fc-a9e97181e201/orders
    @GetMapping(value = ["/{id}/orders"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun orders(@PathVariable id: String) = service.getOrdersForPizzaId(pizzaId = id)
}

@Service
class PizzaService(private val repo: PizzaRepository) {

    fun getAllPizzas() = repo.findAll()

    fun getPizzaById(id: String) = repo.findById(id)

    fun getOrdersForPizzaId(pizzaId: String) = Flux.interval(Duration.ofSeconds(1))
            .onBackpressureDrop()
            .map { PizzaOrder(pizzaId = pizzaId, whenOrdered = Instant.now()) }
}

@Component
class DataLoader(private val repository: PizzaRepository) {

    @PostConstruct
    fun load() =
            repository.deleteAll().thenMany(
                    listOf("Margherita", "Fungi", "Proscuttio", "Napoli", "Quattro Formaggio", "Calzone", "Rustica")
                            .toFlux()
                            .map { Pizza(name = it) })
                    .flatMap { repository.save(it) }
                    .thenMany(repository.findAll())
                    .subscribe { println(it) }
}


interface PizzaRepository : ReactiveCrudRepository<Pizza, String>

data class PizzaOrder(val pizzaId: String, val whenOrdered: Instant)

@Document
data class Pizza(@Id val id: String = UUID.randomUUID().toString(), val name: String)

