package sarangit.semin5.worklog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping("/admin")
    public String admin() { return "forward:/index.html"; }
}
