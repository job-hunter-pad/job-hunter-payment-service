package jobhunter.payment.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    @GetMapping("/")
    public String root() {
        return "It Works!";
    }

    @GetMapping("/test")
    public String test() {
        return "Test Works!";
    }
}
