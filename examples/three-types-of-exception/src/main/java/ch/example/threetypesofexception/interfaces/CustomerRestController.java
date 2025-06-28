package ch.example.threetypesofexception.interfaces;

import ch.example.threetypesofexception.application.FindCustomersByNameUseCase;
import ch.example.threetypesofexception.domain.CustomerName;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerRestController {

    private final FindCustomersByNameUseCase findCustomersByNameUseCase;

    @GetMapping("/customers")
    public List<CustomerDto> findByName(@RequestParam String name) {
        return findCustomersByNameUseCase
            .invoke(new CustomerName(name))
            .stream()
            .map(customer ->
                new CustomerDto(
                    customer.getTitle().name(),
                    customer.getCustomerName().value()
                )
            )
            .collect(Collectors.toList());
    }
}
