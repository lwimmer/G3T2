package at.ac.tuwien.infosys.aic2016.g3t2;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    @RequestMapping("/")
    public String index() {
        return "redirect:/index.html";
    }
}
